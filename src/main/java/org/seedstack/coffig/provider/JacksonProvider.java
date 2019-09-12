/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.node.ArrayNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationProvider;
import org.seedstack.coffig.spi.ConfigurationWatcher;
import org.seedstack.coffig.watcher.FileConfigurationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JacksonProvider implements ConfigurationProvider, FileConfigurationWatcher.Listener {
    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonProvider.class);
    private final List<URL> sources = new ArrayList<>();
    private final AtomicBoolean dirty = new AtomicBoolean(true);
    private final ObjectMapper jacksonMapper = new ObjectMapper(new YAMLFactory());
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
    public synchronized JacksonProvider fork() {
        JacksonProvider fork = new JacksonProvider();
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

    public synchronized JacksonProvider addSource(URL url) {
        if (url == null) {
            throw new NullPointerException("Source URL cannot be null");
        }
        sources.add(url);
        if ("file".equalsIgnoreCase(url.getProtocol())) {
            try {
                fileWatcher.watchFile(Paths.get(url.toURI()), this);
            } catch (Exception e) {
                LOGGER.warn("Unable to watch source: {}", url.toExternalForm(), e);
            }
        }
        dirty.set(true);
        return this;
    }

    @Override
    public void fileChanged(Path path) {
        LOGGER.debug("Configuration file has changed: " + path);
        dirty.set(true);
    }

    private MapNode buildTreeFromSource(URL url) {
        try {
            LOGGER.debug("Reading configuration from " + url.toExternalForm());
            return buildTreeFromFields(jacksonMapper.readTree(url));
        } catch (IOException e) {
            throw ConfigurationException.wrap(e, ConfigurationErrorCode.FAILED_TO_READ_CONFIGURATION)
                    .put("url", url.toExternalForm());
        }
    }

    private MapNode buildTreeFromFields(JsonNode node) {
        List<NamedNode> namedNodes = new ArrayList<>();
        if (node != null) {
            node.fields().forEachRemaining(entry -> {
                String name = entry.getKey();
                TreeNode treeNode = buildTreeFromField(entry.getValue());
                namedNodes.add(new NamedNode(name, treeNode));
            });
        }
        return new MapNode(namedNodes.toArray(new NamedNode[0]));
    }

    private TreeNode buildTreeFromField(JsonNode jsonNode) {
        NodeBuilder nodeBuilder;
        if (jsonNode.isValueNode()) {
            nodeBuilder = new ValueNodeBuilder();
        } else if (jsonNode.isArray()) {
            nodeBuilder = new ArrayNodeBuilder();
        } else {
            nodeBuilder = new ObjectNodeBuilder();
        }
        return nodeBuilder.build(jsonNode);
    }

    private interface NodeBuilder {
        TreeNode build(JsonNode jsonNode);
    }

    private static class ValueNodeBuilder implements NodeBuilder {
        @Override
        public TreeNode build(JsonNode jsonNode) {
            return new ValueNode(jsonNode.asText(null));
        }
    }

    private class ArrayNodeBuilder implements NodeBuilder {
        @Override
        public TreeNode build(JsonNode jsonNode) {
            List<TreeNode> treeNodes = new ArrayList<>();
            for (int i = 0; i < jsonNode.size(); i++) {
                TreeNode treeNode = buildTreeFromField(jsonNode.get(i));
                treeNodes.add(treeNode);
            }
            TreeNode[] nodes = treeNodes.toArray(new TreeNode[0]);
            return new ArrayNode(nodes);
        }
    }

    private class ObjectNodeBuilder implements NodeBuilder {
        @Override
        public TreeNode build(JsonNode jsonNode) {
            List<NamedNode> namedNodes = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                TreeNode treeNode = buildTreeFromField(entry.getValue());
                namedNodes.add(new NamedNode(fieldName, treeNode));
            }
            NamedNode[] nodes = namedNodes.toArray(new NamedNode[0]);
            return new MapNode(nodes);
        }
    }
}
