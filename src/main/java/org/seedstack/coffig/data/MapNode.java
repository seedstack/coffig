/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data;

import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.PropertyNotFoundException;
import org.seedstack.coffig.data.mutable.MutableMapNode;
import org.seedstack.coffig.data.mutable.MutableTreeNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.seedstack.coffig.ConfigurationException.INCORRECT_MERGE;

public class MapNode extends AbstractTreeNode {
    protected final Map<String, TreeNode> childNodes;

    public MapNode(NamedNode... childNodes) {
        this.childNodes = new HashMap<>();
        for (NamedNode childNode : childNodes) {
            this.childNodes.put(childNode.name(), childNode.get());
        }
    }

    public MapNode(Map<String, TreeNode> newChildNodes) {
        this.childNodes = newChildNodes;
    }

    public Set<String> keys() {
        return childNodes.keySet();
    }

    @Override
    public TreeNode doSearch(String name) {
        return value(name);
    }

    @Override
    public TreeNode value(String name) {
        if (childNodes.containsKey(name))
            return childNodes.get(name);
        else
            throw new PropertyNotFoundException(name);
    }

    @Override
    public TreeNode merge(TreeNode otherNode) {
        if (!(otherNode instanceof MapNode)) {
            throw new ConfigurationException(INCORRECT_MERGE.apply(otherNode.getClass(), getClass()));
        }
        return mergeMapNode((MapNode) otherNode);
    }

    private TreeNode mergeMapNode(MapNode otherNode) {
        Map<String, TreeNode> newChildNodes = this.childNodes;

        otherNode.childNodes.forEach((nodeName, treeNode) -> {
            TreeNode node = this.childNodes.containsKey(nodeName) ? this.childNodes.get(nodeName).merge(treeNode) : treeNode;
            newChildNodes.put(nodeName, node);
        });
        return new MapNode(newChildNodes);
    }

    @Override
    public MutableTreeNode unfreeze() {
        Map<String, TreeNode> nodes = new HashMap<>();
        childNodes.forEach((key, val) -> nodes.put(key, val.unfreeze()));
        return new MutableMapNode(nodes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapNode mapNode = (MapNode) o;
        return Objects.equals(childNodes, mapNode.childNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(childNodes);
    }

    @Override
    public String toString() {
        return childNodes.entrySet().stream().map(entry -> {
            if (entry.getValue() instanceof ValueNode) {
                return entry.getKey() + ": " + entry.getValue().toString();
            } else {
                return entry.getKey() + ":\n" + indent(entry.getValue().toString());
            }
        }).collect(Collectors.joining("\n"));
    }
}
