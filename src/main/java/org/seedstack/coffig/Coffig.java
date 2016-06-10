/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import org.seedstack.coffig.mapper.MapperFactory;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.MutableMapNode;
import org.seedstack.coffig.spi.ConfigurationProcessor;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class Coffig {
    private final List<ConfigurationProvider> providers = new CopyOnWriteArrayList<>();
    private final List<ConfigurationProcessor> processors = new CopyOnWriteArrayList<>();
    private final MapperFactory mapperFactory = new MapperFactory();
    private volatile MapNode configurationTree = new MapNode();
    private volatile boolean dirty = false;

    public MapperFactory getMapperFactory() {
        return mapperFactory;
    }

    public void addProvider(ConfigurationProvider configurationProvider) {
        try {
            providers.add(configurationProvider);
        } finally {
            dirty = true;
        }
    }

    public void addProcessor(ConfigurationProcessor configurationProcessor) {
        try {
            processors.add(configurationProcessor);
        } finally {
            dirty = true;
        }
    }

    public void compute() {
        try {
            if (providers.size() > 0) {
                ForkJoinPool forkJoinPool = new ForkJoinPool();
                try {
                    // Build mutable merged configuration from providers
                    MutableMapNode localConfigurationTree = (MutableMapNode) forkJoinPool.submit(() -> providers.parallelStream()
                            .map(ConfigurationProvider::provide)
                            .reduce((conf1, conf2) -> (MapNode) conf1.merge(conf2))
                            .orElse(new MapNode())
                    ).get().unfreeze();

                    // Apply configuration processors
                    for (ConfigurationProcessor processor : processors) {
                        processor.process(localConfigurationTree);
                    }

                    // Freeze and store the processed configuration
                    configurationTree = (MapNode) localConfigurationTree.freeze();
                } catch (InterruptedException | ExecutionException e) {
                    throw new ConfigurationException(e);
                } finally {
                    forkJoinPool.shutdown();
                }
            }
        } finally {
            dirty = false;
        }
    }

    public TreeNode dump() {
        return configurationTree;
    }

    public Coffig fork() {
        Coffig fork = new Coffig();
        for (ConfigurationProvider provider : providers) {
            fork.addProvider(provider.fork());
        }
        for (ConfigurationProcessor processor : processors) {
            fork.addProcessor(processor.fork());
        }
        return fork;
    }

    public <T> T get(Class<T> configurationClass, String... path) {
        return getOptional(configurationClass, path).orElseGet(() -> instantiateDefault(configurationClass));
    }

    public <T> T getMandatory(Class<T> configurationClass, String... path) {
        return getOptional(configurationClass, path).orElseThrow(() -> new ConfigurationException("Path not found: " + (path == null ? "null" : String.join(".", (CharSequence[]) path))));
    }

    public <T> Optional<T> getOptional(Class<T> configurationClass, String... path) {
        Optional<TreeNode> result;

        computeIfNecessary();

        if (path == null || path.length == 0) {
            result = Optional.of(configurationTree);
        } else {
            result = configurationTree.get(String.join(".", (CharSequence[]) path));
        }

        if (result.isPresent()) {
            return Optional.ofNullable(doGet(result.get(), configurationClass));
        } else {
            return Optional.empty();
        }
    }

    private <T> T instantiateDefault(Class<T> configurationClass) {
        try {
            return configurationClass.newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Cannot instantiate default value", e);
        }
    }

    private void computeIfNecessary() {
        if (dirty || providers.stream().filter(ConfigurationProvider::isDirty).count() > 0) {
            compute();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T doGet(TreeNode treeNode, Class<T> configurationClass) {
        return (T) mapperFactory.map(treeNode, configurationClass);
    }
}
