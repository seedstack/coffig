/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.seedstack.coffig.node.ArrayNode;
import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JacksonProvider implements ConfigurationProvider {
    private final List<URL> sources = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private volatile boolean dirty = false;

    @Override
    public MapNode provide() {
        return sources
                .stream()
                .map(this::buildTreeFromSource)
                .reduce((conf1, conf2) -> (MapNode) conf1.merge(conf2))
                .orElse(new MapNode());
    }

    @Override
    public ConfigurationProvider fork() {
        JacksonProvider fork = new JacksonProvider();
        fork.sources.addAll(sources);
        return fork;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    private MapNode buildTreeFromSource(URL url) {
        try {
            return buildTreeFromFields(mapper.readTree(url));
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read configuration from " + url.toExternalForm(), e);
        }
    }

    private MapNode buildTreeFromFields(JsonNode node) {
        List<NamedNode> namedNodes = new ArrayList<>();
        node.fields().forEachRemaining(entry -> {
            String name = entry.getKey();
            TreeNode treeNode = buildTreeFromField(entry.getValue());
            namedNodes.add(new NamedNode(name, treeNode));
        });
        return new MapNode(namedNodes.toArray(new NamedNode[namedNodes.size()]));
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

    public JacksonProvider addSource(URL url) {
        if (url == null) {
            throw new NullPointerException("Source URL cannot be null");
        }

        try {
            this.sources.add(url);
            return this;
        } finally {
            dirty = true;
        }
    }

    private interface NodeBuilder {
        TreeNode build(JsonNode jsonNode);
    }

    private class ValueNodeBuilder implements NodeBuilder {
        @Override
        public TreeNode build(JsonNode jsonNode) {
            return new ValueNode(jsonNode.asText());
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
            TreeNode[] nodes = treeNodes.toArray(new TreeNode[treeNodes.size()]);
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
            NamedNode[] nodes = namedNodes.toArray(new NamedNode[namedNodes.size()]);
            return new MapNode(nodes);
        }
    }
}
