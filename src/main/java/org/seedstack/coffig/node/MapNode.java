/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.PropertyNotFoundException;
import org.seedstack.coffig.TreeNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapNode extends AbstractTreeNode {
    protected final Map<String, TreeNode> children;

    public MapNode() {
        this.children = new HashMap<>();
    }

    public MapNode(NamedNode... children) {
        this.children = Freezer.freeze(children);
    }

    public MapNode(Map<String, TreeNode> newChildNodes) {
        this.children = Freezer.freeze(newChildNodes);
    }

    public Set<String> keys() {
        return children.keySet();
    }

    @Override
    public String value() {
        throw new ConfigurationException("Cannot access map as single value");
    }

    @Override
    public TreeNode item(String key) {
        if (children.containsKey(key))
            return children.get(key);
        else
            throw new PropertyNotFoundException(key);
    }

    @Override
    public TreeNode[] items() {
        return children.values().toArray(new TreeNode[children.size()]);
    }

    @Override
    public Optional<TreeNode> get(String path) {
        Path _path = new Path(path);
        Optional<TreeNode> treeNode = Optional.empty();

        if (_path.hasHead()) {
            treeNode = Optional.ofNullable(children.get(_path.getHead()));
            if (treeNode.isPresent() && _path.hasTail()) {
                return treeNode.get().get(_path.getTail());
            }
        }

        return treeNode;
    }

    @Override
    public Stream<TreeNode> stream() {
        return Stream.concat(Stream.of(this), children.values().stream().flatMap(TreeNode::stream));
    }

    @Override
    public TreeNode merge(TreeNode otherNode) {
        if (!(otherNode instanceof MapNode)) {
            throw new ConfigurationException(String.format("Illegal attempt to merge %s with %s", otherNode.getClass().getCanonicalName(), getClass().getCanonicalName()));
        }
        return mergeMapNode((MapNode) otherNode);
    }

    private TreeNode mergeMapNode(MapNode otherNode) {
        Map<String, TreeNode> newChildNodes = this.children;

        otherNode.children.forEach((nodeName, treeNode) -> {
            TreeNode node = this.children.containsKey(nodeName) ? this.children.get(nodeName).merge(treeNode) : treeNode;
            newChildNodes.put(nodeName, node);
        });
        return new MapNode(newChildNodes);
    }

    @Override
    public MapNode freeze() {
        return this;
    }

    @Override
    public MutableMapNode unfreeze() {
        return new MutableMapNode(children);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !getClass().isAssignableFrom(o.getClass()) && !o.getClass().isAssignableFrom(getClass()))
            return false;
        MapNode mapNode = (MapNode) o;
        return Objects.equals(children, mapNode.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children);
    }

    @Override
    public String toString() {
        return children.entrySet().stream().map(entry -> {
            if (entry.getValue() instanceof ValueNode) {
                return entry.getKey() + ": " + entry.getValue().toString();
            } else {
                return entry.getKey() + ":\n" + indent(entry.getValue().toString());
            }
        }).collect(Collectors.joining("\n"));
    }
}
