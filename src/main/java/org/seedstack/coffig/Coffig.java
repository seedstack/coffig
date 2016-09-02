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
import org.seedstack.coffig.spi.ConfigurationEvaluator;
import org.seedstack.coffig.spi.ConfigurationMapper;
import org.seedstack.coffig.spi.ConfigurationProcessor;
import org.seedstack.coffig.spi.ConfigurationProvider;
import org.seedstack.coffig.utils.EvaluatingMapper;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

public class Coffig {
    private volatile boolean dirty = true;
    private volatile MapNode configurationTree = new MapNode();
    private volatile EvaluatingMapper mapper = new EvaluatingMapper();
    private volatile ConfigurationProvider provider;
    private volatile ConfigurationProcessor processor;

    public ConfigurationMapper getMapper() {
        return mapper.getMapper();
    }

    public Coffig setMapper(ConfigurationMapper mapper) {
        this.mapper.setMapper(mapper);
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

    public ConfigurationEvaluator getEvaluator() {
        return mapper.getEvaluator();
    }

    public Coffig setEvaluator(ConfigurationEvaluator evaluator) {
        mapper.setEvaluator(evaluator);
        dirty = true;
        return this;
    }

    public void invalidate() {
        this.dirty = true;
    }

    public TreeNode dump() {
        return configurationTree;
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
                    .map(treeNode -> (T) mapper.map(treeNode, configurationClass));
        } else {
            return configurationTree
                    .get(joinedPath)
                    .map(treeNode -> (T) mapper.map(treeNode, configurationClass));
        }
    }

    @Override
    public String toString() {
        computeIfNecessary();
        return configurationTree.toString();
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
            return (T) new Character((char) 0);
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
                mapper.setRootNode(configurationTree);
                dirty = false;
            }
        }
    }

    private synchronized boolean isDirty() {
        return dirty || provider.isDirty();
    }
}
