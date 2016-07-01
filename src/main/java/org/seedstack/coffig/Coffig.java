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
import org.seedstack.coffig.spi.ConfigurationProcessor;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.util.Optional;

public class Coffig {
    private final MapperFactory mapperFactory = new MapperFactory();
    private final ConfigurationProvider configurationProvider;
    private volatile boolean dirty = true;
    private volatile MapNode configurationTree = new MapNode();
    private volatile ConfigurationProcessor configurationProcessor;

    public Coffig(ConfigurationProvider configurationProvider) {
        if (configurationProvider == null) {
            throw new IllegalArgumentException("Configuration provider cannot be null");
        }
        this.configurationProvider = configurationProvider;
    }

    public void setConfigurationProcessor(ConfigurationProcessor configurationProcessor) {
        this.configurationProcessor = configurationProcessor;
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
        Coffig fork = new Coffig(configurationProvider.fork());
        if (configurationProcessor != null) {
            fork.setConfigurationProcessor(configurationProcessor.fork());
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

    private void computeIfNecessary() {
        try {
            if (dirty || configurationProvider.isDirty()) {
                configurationTree = configurationProvider.provide();
                if (configurationProcessor != null) {
                    configurationProcessor.process(configurationTree.unfreeze());
                }
                configurationTree = configurationTree.freeze();
            }
        } finally {
            dirty = false;
        }
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
