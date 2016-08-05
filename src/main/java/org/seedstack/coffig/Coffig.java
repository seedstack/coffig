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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Coffig {
    private final MapperFactory mapperFactory = new MapperFactory();
    private final Map<Integer, ConfigurationProvider> providers = new ConcurrentHashMap<>();
    private volatile boolean dirty = true;
    private volatile MapNode configurationTree = new MapNode();
    private volatile ConfigurationProcessor configurationProcessor;

    public void registerProvider(ConfigurationProvider configurationProvider) {
        registerProvider(configurationProvider, 0);
    }

    public void registerProvider(ConfigurationProvider configurationProvider, int priority) {
        if (providers.putIfAbsent(priority, configurationProvider) != null) {
            throw new IllegalStateException("A provider is already register for priority " + priority);
        } else {
            dirty = true;
        }
    }

    public void registerProcessor(ConfigurationProcessor configurationProcessor) {
        this.configurationProcessor = configurationProcessor;
        dirty = true;
    }

    public void invalidate() {
        this.dirty = true;
    }

    public MapperFactory getMapperFactory() {
        return mapperFactory;
    }

    public TreeNode dump() {
        return configurationTree;
    }

    public Coffig fork() {
        Coffig fork = new Coffig();
        for (Map.Entry<Integer, ConfigurationProvider> providerEntry : providers.entrySet()) {
            fork.registerProvider(providerEntry.getValue().fork(), providerEntry.getKey());
        }
        if (configurationProcessor != null) {
            fork.registerProcessor(configurationProcessor.fork());
        }
        return fork;
    }

    public <T> T get(Class<T> configurationClass, String... path) {
        return getOptional(configurationClass, path).orElseGet(() -> instantiateDefault(configurationClass));
    }

    public <T> T getMandatory(Class<T> configurationClass, String... path) {
        return getOptional(configurationClass, path).orElseThrow(() -> new ConfigurationException("Path not found: " + (path == null ? "null" : String.join(".", (CharSequence[]) path))));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOptional(Class<T> configurationClass, String... path) {
        computeIfNecessary();

        if (path == null || path.length == 0) {
            return Optional.of(configurationTree).map(treeNode -> (T) mapperFactory.map(treeNode, configurationClass));
        } else {
            return configurationTree
                    .get(String.join(".", (CharSequence[]) path))
                    .map(treeNode -> (T) mapperFactory.map(treeNode, configurationClass));
        }
    }

    private void computeIfNecessary() {
        if (isDirty()) {
            MapNode pendingConfigurationTree = providers.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .map(ConfigurationProvider::provide)
                    .reduce((conf1, conf2) -> (MapNode) conf1.merge(conf2))
                    .orElse(new MapNode());

            if (configurationProcessor != null) {
                pendingConfigurationTree = pendingConfigurationTree.unfreeze();
                configurationProcessor.process((MutableMapNode) pendingConfigurationTree);
            }

            synchronized (this) {
                configurationTree = pendingConfigurationTree.freeze();
                dirty = false;
            }
        }
    }

    private synchronized boolean isDirty() {
        return dirty || providers.values().stream().filter(ConfigurationProvider::isDirty).count() > 0;
    }

    private <T> T instantiateDefault(Class<T> configurationClass) {
        try {
            return configurationClass.newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Cannot instantiate default value", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T doGet(TreeNode treeNode, Class<T> configurationClass) {
        return (T) mapperFactory.map(treeNode, configurationClass);
    }
}
