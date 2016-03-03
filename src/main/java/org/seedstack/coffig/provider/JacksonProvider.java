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
import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.data.*;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JacksonProvider implements ConfigurationProvider {

    private List<InputStream> sources = new ArrayList<>();

    @Override
    public MapNode provide() {
        return sources.stream().map(this::buildTreeFromSource)
                .reduce((conf1, conf2) -> (MapNode) conf1.merge(conf2)).orElse(new MapNode());
    }

    private MapNode buildTreeFromSource(InputStream inputStream) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            JsonNode jsonNode = mapper.readTree(inputStream);
            return buildTreeFromFields(jsonNode);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read configuration", e);
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

    public JacksonProvider addSource(InputStream inputStream) {
        this.sources.add(inputStream);
        return this;
    }

    interface NodeBuilder {
        TreeNode build(JsonNode jsonNode);
    }

    class ValueNodeBuilder implements NodeBuilder {

        @Override
        public TreeNode build(JsonNode jsonNode) {
            return new ValueNode(jsonNode.asText());
        }
    }

    class ArrayNodeBuilder implements NodeBuilder {

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

    class ObjectNodeBuilder implements NodeBuilder {

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
