/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.MutableMapNode;
import org.seedstack.coffig.spi.ConfigurationMapper;
import org.seedstack.coffig.spi.ConfigurationProcessor;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.util.Optional;

import static org.seedstack.coffig.utils.Utils.instantiateDefault;
import static org.seedstack.coffig.utils.Utils.resolvePath;

public class Coffig {
    private static final int MINIMUM_DIRTY_POLL_INTERVAL = 2000;

    private boolean dirty = true;
    private long latestPollTime = Long.MAX_VALUE;
    private MapNode configurationTree = new MapNode();
    private ConfigurationMapper mapper;
    private ConfigurationProvider provider;
    private ConfigurationProcessor processor;

    protected Coffig() {
    }

    public static CoffigBuilder builder() {
        return new CoffigBuilder(new Coffig());
    }

    public void initialize() {
        if (mapper != null) {
            mapper.initialize(this);
        }
        if (provider != null) {
            provider.initialize(this);
        }
        if (processor != null) {
            processor.initialize(this);
        }
    }

    public synchronized boolean isDirty() {
        if (dirty) {
            return true;
        }

        long pollTime = System.currentTimeMillis();
        if (latestPollTime - pollTime > MINIMUM_DIRTY_POLL_INTERVAL) {
            latestPollTime = pollTime;
            return mapper != null && mapper.isDirty() ||
                    provider != null && provider.isDirty() ||
                    processor != null && processor.isDirty();
        } else {
            return false;
        }
    }

    public void refresh() {
        MapNode pendingConfigurationTree;
        if (provider != null) {
            pendingConfigurationTree = provider.provide();
        } else {
            pendingConfigurationTree = new MapNode();
        }

        if (processor != null) {
            pendingConfigurationTree = pendingConfigurationTree.unfreeze();
            processor.process((MutableMapNode) pendingConfigurationTree);
        }

        synchronized (this) {
            configurationTree = pendingConfigurationTree.freeze();
            dirty = false;
        }
    }

    public Coffig fork() {
        Coffig fork = new Coffig();
        if (mapper != null) {
            fork.setMapper((ConfigurationMapper) mapper.fork());
        }
        if (provider != null) {
            fork.setProvider((ConfigurationProvider) provider.fork());
        }
        if (processor != null) {
            fork.setProcessor((ConfigurationProcessor) processor.fork());
        }
        fork.initialize();
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
        if (isDirty()) {
            refresh();
        }

        String joinedPath;
        if (path != null && path.length > 0) {
            joinedPath = String.join(".", (CharSequence[]) path);
        } else {
            joinedPath = resolvePath(configurationClass);
        }

        if (joinedPath == null || joinedPath.isEmpty()) {
            return Optional.of(configurationTree)
                    .map(treeNode -> (T) mapper.map(treeNode, configurationClass));
        } else {
            return configurationTree
                    .get(joinedPath)
                    .map(treeNode -> (T) mapper.map(treeNode, configurationClass));
        }
    }

    @Override
    public String toString() {
        return configurationTree.toString();
    }

    public ConfigurationMapper getMapper() {
        return mapper;
    }

    public Coffig setMapper(ConfigurationMapper mapper) {
        this.mapper = mapper;
        dirty = true;
        return this;
    }

    public ConfigurationProvider getProvider() {
        return provider;
    }

    public Coffig setProvider(ConfigurationProvider configurationProvider) {
        this.provider = configurationProvider;
        dirty = true;
        return this;
    }

    public ConfigurationProcessor getProcessor() {
        return processor;
    }

    public Coffig setProcessor(ConfigurationProcessor configurationProcessor) {
        this.processor = configurationProcessor;
        dirty = true;
        return this;
    }

    public TreeNode getTree() {
        return configurationTree;
    }
}
