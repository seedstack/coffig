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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.seedstack.coffig.ConfigurationException.INCORRECT_MERGE;

public class MapNode extends TreeNode {
    private final Map<String, PairNode> childNodes;

    public MapNode(PairNode... childNodes) {
        this.childNodes = new HashMap<>();
        for (PairNode childNode : childNodes) {
            this.childNodes.put(childNode.name(), childNode);
        }
    }

    public MapNode(Map<String, PairNode> newChildNodes) {
        this.childNodes = newChildNodes;
    }

    public Set<String> keys() {
        return childNodes.keySet();
    }

    @Override
    protected TreeNode doSearch(String name) {
        return value(name);
    }

    @Override
    public TreeNode value(String name) {
        if (childNodes.containsKey(name))
            return childNodes.get(name).get();
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
        Map<String, PairNode> newChildNodes = childNodes;
        for (Map.Entry<String, PairNode> entry : otherNode.childNodes.entrySet()) {
            String nodeName = entry.getKey();
            PairNode node;
            if (childNodes.containsKey(nodeName)) {
                node = (PairNode) childNodes.get(nodeName).merge(entry.getValue());
            } else {
                node = entry.getValue();
            }
            newChildNodes.put(nodeName, node);
        }
        return new MapNode(newChildNodes);
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
}
