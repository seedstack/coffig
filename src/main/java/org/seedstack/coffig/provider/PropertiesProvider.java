/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationProvider;
import org.seedstack.coffig.spi.ConfigurationWatcher;
import org.seedstack.coffig.watcher.FileConfigurationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesProvider implements ConfigurationProvider, Consumer<Path> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesProvider.class);
    private final List<URL> sources = new ArrayList<>();
    private final AtomicBoolean dirty = new AtomicBoolean(true);
    private final FileConfigurationWatcher fileWatcher = FileConfigurationWatcher.getInstance();

    @Override
    public synchronized MapNode provide() {
        MapNode mapNode = sources
                .stream()
                .map(this::buildTreeFromSource)
                .reduce((conf1, conf2) -> (MapNode) conf1.merge(conf2))
                .orElse(new MapNode());
        dirty.set(false);
        return mapNode;
    }

    @Override
    public synchronized PropertiesProvider fork() {
        PropertiesProvider fork = new PropertiesProvider();
        for (URL source : sources) {
            fork.addSource(source);
        }
        return fork;
    }

    @Override
    public boolean isDirty() {
        return dirty.get();
    }

    @Override
    public Set<ConfigurationWatcher> watchers() {
        HashSet<ConfigurationWatcher> configurationWatchers = new HashSet<>();
        configurationWatchers.add(fileWatcher);
        return configurationWatchers;
    }

    public synchronized PropertiesProvider addSource(URL url) {
        if (url == null) {
            throw new NullPointerException("Source URL cannot be null");
        }

        sources.add(url);
        try {
            fileWatcher.watchFile(Paths.get(url.toURI()), this);
        } catch (Exception e) {
            LOGGER.warn("Unable to watch source: {}", url.toExternalForm(), e);
        }
        dirty.set(true);
        return this;
    }

    @Override
    public void accept(Path path) {
        dirty.set(true);
    }

    private MapNode buildTreeFromSource(URL url) {
        try (InputStream inputStream = url.openStream()) {
            MapNode mapNode = new MapNode();
            Properties properties = new Properties();
            properties.load(inputStream);
            properties.forEach((key, value) -> mapNode.set((String) key, new ValueNode((String) value)));
            return mapNode;
        } catch (IOException e) {
            throw ConfigurationException.wrap(e, ConfigurationErrorCode.FAILED_TO_READ_CONFIGURATION)
                    .put("url", url.toExternalForm());
        }
    }
}
