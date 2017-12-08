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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.BaseWatchingProvider;
import org.seedstack.coffig.spi.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesProvider extends BaseWatchingProvider implements ConfigurationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesProvider.class);
    private final List<URL> sources = new ArrayList<>();
    private final AtomicBoolean dirty = new AtomicBoolean(true);

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
    public PropertiesProvider fork() {
        PropertiesProvider fork = new PropertiesProvider();
        for (URL source : sources) {
            watchSource(source);
        }
        return fork;
    }

    @Override
    public boolean isDirty() {
        return dirty.get();
    }

    public synchronized PropertiesProvider addSource(URL url) {
        if (url == null) {
            throw new NullPointerException("Source URL cannot be null");
        }

        this.sources.add(url);
        dirty.set(true);
        return this;
    }

    @Override
    protected void fileChanged(Path path) {
        LOGGER.info("Configuration file changed: " + path.toString());
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
