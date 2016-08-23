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

import java.lang.reflect.AnnotatedElement;
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

        String joinedPath;
        if (path != null && path.length > 0) {
            joinedPath = String.join(".", (CharSequence[]) path);
        } else {
            joinedPath = resolvePath(configurationClass);
        }

        if (joinedPath == null || joinedPath.isEmpty()) {
            return Optional.of(configurationTree)
                    .map(treeNode -> (T) mapperFactory.map(treeNode, configurationClass));
        } else {
            return configurationTree
                    .get(joinedPath)
                    .map(treeNode -> (T) mapperFactory.map(treeNode, configurationClass));
        }
    }

    public static String resolvePath(AnnotatedElement annotatedElement) {
        Config annotation;
        StringBuilder path = new StringBuilder();
        if (annotatedElement instanceof Class) {
            Class<?> currentClass = (Class) annotatedElement;
            while (currentClass != null && (annotation = currentClass.getAnnotation(Config.class)) != null) {
                if (!annotation.value().isEmpty()) {
                    if (path.length() > 0) {
                        path.insert(0, ".");
                    }
                    path.insert(0, annotation.value());
                }
                currentClass = currentClass.getDeclaringClass();
            }
            return path.toString();
        } else {
            annotation = annotatedElement.getAnnotation(Config.class);
            if (annotation != null) {
                return annotation.value();
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T instantiateDefault(Class<T> configurationClass) {
        if (Boolean.class.equals(configurationClass)) {
            return (T) Boolean.FALSE;
        } else if (Integer.class.equals(configurationClass)) {
            return (T) new Integer(0);
        } else if (Long.class.equals(configurationClass)) {
            return (T) new Long(0L);
        } else if (Short.class.equals(configurationClass)) {
            return (T) new Short((short) 0);
        } else if (Float.class.equals(configurationClass)) {
            return (T) new Float(0f);
        } else if (Double.class.equals(configurationClass)) {
            return (T) new Double(0d);
        } else if (Byte.class.equals(configurationClass)) {
            return (T) new Byte((byte) 0);
        } else if (Character.class.equals(configurationClass)) {
            return (T) new Character(' ');
        } else {
            try {
                return configurationClass.newInstance();
            } catch (Exception e) {
                throw new ConfigurationException("Cannot instantiate default value", e);
            }
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
}
