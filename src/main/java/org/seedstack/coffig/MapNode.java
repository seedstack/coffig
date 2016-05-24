/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.seedstack.coffig.ConfigurationException.INCORRECT_MERGE;

public class MapNode extends AbstractTreeNode {
    protected final Map<String, TreeNode> childNodes;

    /**
     * Used by mutable subclass to avoid auto-freezing nodes.
     */
    protected MapNode() {
        this.childNodes = new HashMap<>();
    }

    public MapNode(NamedNode... childNodes) {
        this.childNodes = Freezer.freeze(childNodes);
    }

    public MapNode(Map<String, TreeNode> newChildNodes) {
        this.childNodes = Freezer.freeze(newChildNodes);
    }

    public Set<String> keys() {
        return childNodes.keySet();
    }

    @Override
    protected Optional<TreeNode> doGet(String name) {
        return Optional.ofNullable(childNodes.get(name));
    }

    @Override
    public TreeNode value(String name) {
        if (childNodes.containsKey(name))
            return childNodes.get(name);
        else
            throw new PropertyNotFoundException(name);
    }

    @Override
    public TreeNode[] values() {
        return childNodes.values().toArray(new TreeNode[childNodes.size()]);
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
    public TreeNode freeze() {
        return this;
    }

    @Override
    public MutableTreeNode unfreeze() {
        return new MutableMapNode(childNodes);
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
