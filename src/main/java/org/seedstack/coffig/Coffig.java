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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.seedstack.coffig.utils.Utils.instantiateDefault;
import static org.seedstack.coffig.utils.Utils.resolvePath;

public class Coffig {
    private final ConfigurationMapper mapper;
    private final ConfigurationProvider provider;
    private final ConfigurationProcessor processor;
    private final AtomicBoolean dirty = new AtomicBoolean(true);
    private volatile MapNode configurationTree = new MapNode();

    Coffig(ConfigurationMapper mapper, ConfigurationProvider provider, ConfigurationProcessor processor) {
        this.mapper = mapper;
        this.provider = provider;
        this.processor = processor;
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

    public static CoffigBuilder builder() {
        return new CoffigBuilder();
    }

    public boolean isDirty() {
        return dirty.get() ||
                mapper != null && mapper.isDirty() ||
                provider != null && provider.isDirty() ||
                processor != null && processor.isDirty();
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

        configurationTree = pendingConfigurationTree.freeze();
        dirty.set(false);
    }

    public Coffig fork() {
        return new Coffig(
                mapper == null ? null : (ConfigurationMapper) mapper.fork(),
                provider == null ? null : (ConfigurationProvider) provider.fork(),
                processor == null ? null : (ConfigurationProcessor) processor.fork()
        );
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

    public TreeNode getTree() {
        return configurationTree;
    }

    @Override
    public String toString() {
        return configurationTree.toString();
    }

    public ConfigurationMapper getMapper() {
        return mapper;
    }

    public ConfigurationProvider getProvider() {
        return provider;
    }

    public ConfigurationProcessor getProcessor() {
        return processor;
    }
}
