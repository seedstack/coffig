/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import static org.seedstack.shed.reflect.Classes.instantiateDefault;
import static org.seedstack.shed.reflect.Types.rawClassOf;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.UnmodifiableTreeNode;
import org.seedstack.coffig.spi.ConfigurationMapper;
import org.seedstack.coffig.spi.ConfigurationProcessor;
import org.seedstack.coffig.spi.ConfigurationProvider;
import org.seedstack.coffig.spi.ConfigurationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Coffig {
    private static final Logger LOGGER = LoggerFactory.getLogger(Coffig.class);
    private final ConfigurationMapper mapper;
    private final ConfigurationProvider provider;
    private final ConfigurationProcessor processor;
    private final Map<String, List<ConfigChangeListener>> listeners = new TreeMap<>(Comparator.reverseOrder());
    private final Set<ConfigurationWatcher> configurationWatchers = new HashSet<>();
    private volatile boolean dirty = true;
    private volatile TreeNode configurationTree = new MapNode();

    Coffig(ConfigurationMapper mapper, ConfigurationProvider provider, ConfigurationProcessor processor) {
        LOGGER.debug("Creating new configuration");

        this.mapper = mapper;
        this.provider = provider;
        this.processor = processor;

        if (this.mapper != null) {
            this.mapper.initialize(this);
            configurationWatchers.addAll(this.mapper.watchers());
        }
        if (this.provider != null) {
            this.provider.initialize(this);
            configurationWatchers.addAll(this.provider.watchers());
        }
        if (this.processor != null) {
            this.processor.initialize(this);
            configurationWatchers.addAll(this.processor.watchers());
        }
    }

    public static CoffigBuilder builder() {
        return new CoffigBuilder();
    }

    public static Coffig basic() {
        return new CoffigBuilder().build();
    }

    public static String pathOf(AnnotatedElement annotatedElement) {
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

    public synchronized void startWatching() {
        configurationWatchers.forEach(ConfigurationWatcher::startWatching);
    }

    public synchronized void stopWatching() {
        configurationWatchers.forEach(ConfigurationWatcher::stopWatching);
    }

    public synchronized void registerListener(String path, ConfigChangeListener configChangeListener) {
        listeners.computeIfAbsent(path, key -> new ArrayList<>()).add(configChangeListener);
    }

    public synchronized void unregisterListener(ConfigChangeListener configChangeListener) {
        Iterator<Map.Entry<String, List<ConfigChangeListener>>> it = listeners.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<ConfigChangeListener>> listeners = it.next();
            if (listeners.getValue().remove(configChangeListener)) {
                if (listeners.getValue().isEmpty()) {
                    it.remove();
                }
                break;
            }
        }
    }

    public synchronized void refresh() {
        LOGGER.debug("Refreshing configuration");

        MapNode pendingConfigurationTree;
        if (provider != null) {
            pendingConfigurationTree = provider.provide();
        } else {
            pendingConfigurationTree = new MapNode();
        }

        if (processor != null) {
            processor.process(pendingConfigurationTree);
        }

        TreeNode oldConfigurationTree = configurationTree;
        configurationTree = UnmodifiableTreeNode.of(pendingConfigurationTree);
        dirty = false;

        String lastPath = null;
        for (Map.Entry<String, List<ConfigChangeListener>> entry : listeners.entrySet()) {
            String path = entry.getKey();
            if (lastPath != null && lastPath.startsWith(path)
                    || !Objects.equals(oldConfigurationTree.get(path), configurationTree.get(path))) {
                for (ConfigChangeListener listener : entry.getValue()) {
                    listener.onChange(this);
                }
                lastPath = path;
            } else {
                lastPath = null;
            }
        }
    }

    public Coffig fork() {
        LOGGER.debug("Forking configuration");

        return new Coffig(
                mapper == null ? null : (ConfigurationMapper) mapper.fork(),
                provider == null ? null : (ConfigurationProvider) provider.fork(),
                processor == null ? null : (ConfigurationProcessor) processor.fork()
        );
    }

    public boolean isDirty() {
        return dirty
                || mapper != null && mapper.isDirty()
                || provider != null && provider.isDirty()
                || processor != null && processor.isDirty();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> configurationClass, String... path) {
        return (T) get((Type) configurationClass, path);
    }

    public Object get(Type type, String... path) {
        return getOptional(type, path).orElseGet(() -> instantiateDefault(rawClassOf(type)));
    }

    @SuppressWarnings("unchecked")
    public <T> T getMandatory(Class<T> configurationClass, String... path) {
        return (T) getMandatory((Type) configurationClass, path);
    }

    public Object getMandatory(Type configurationClass, String... path) {
        return getOptional(configurationClass, path).orElseThrow(() ->
                (ConfigurationException) ConfigurationException.createNew(ConfigurationErrorCode.PATH_NOT_FOUND)
                        .put("path", path == null ? "null" : String.join(".", (CharSequence[]) path))
        );
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOptional(Class<T> configurationClass, String... path) {
        return (Optional<T>) getOptional((Type) configurationClass, path);
    }

    public Optional<Object> getOptional(Type configurationType, String... path) {
        LOGGER.trace(
                "Accessing configuration path '" + Arrays.toString(path) + "' and mapping it to '" + configurationType
                        .getTypeName() + "'");

        if (isDirty()) {
            refresh();
        }

        String joinedPath;
        if (path != null && path.length > 0) {
            joinedPath = String.join(".", (CharSequence[]) path);
        } else {
            joinedPath = pathOf(rawClassOf(configurationType));
        }

        Optional<TreeNode> resolvedTree;
        if (joinedPath == null || joinedPath.isEmpty()) {
            resolvedTree = Optional.of(this.configurationTree);
        } else {
            resolvedTree = this.configurationTree.get(joinedPath);
        }
        return resolvedTree.map(treeNode -> mapper.map(treeNode, configurationType));
    }

    public TreeNode getTree() {
        return configurationTree;
    }

    @Override
    public String toString() {
        return "---\n" + configurationTree.toString();
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
