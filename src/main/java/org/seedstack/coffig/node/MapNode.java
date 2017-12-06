/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.node;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.internal.PropertyNotFoundException;

public class MapNode extends AbstractTreeNode {
    private final Map<String, TreeNode> children;

    public MapNode() {
        this.children = new HashMap<>();
    }

    public MapNode(MapNode other) {
        this.children = new HashMap<>(other.children);
    }

    public MapNode(Map<String, TreeNode> children) {
        this.children = new HashMap<>(children);
    }

    public MapNode(NamedNode... children) {
        this.children = Arrays.stream(children).collect(Collectors.toMap(NamedNode::name, NamedNode::node));
    }

    @Override
    public Type type() {
        return Type.MAP_NODE;
    }

    @Override
    public String value() {
        throw ConfigurationException.createNew(ConfigurationErrorCode.CANNOT_ACCESS_MAP_AS_SINGLE_VALUE);
    }

    @Override
    public Stream<TreeNode> nodes() {
        return children.values().stream();
    }

    public Stream<NamedNode> namedNodes() {
        return children.entrySet().stream().map(entry -> new NamedNode(entry.getKey(), entry.getValue()));
    }

    @Override
    public TreeNode node(String key) {
        if (children.containsKey(key))
            return children.get(key);
        else
            throw new PropertyNotFoundException(key);
    }

    @Override
    public Optional<TreeNode> get(String path) {
        if (path.isEmpty()) {
            return Optional.of(this);
        }

        Path _path = new Path(path);
        if (_path.hasHead()) {
            TreeNode child = children.get(_path.getHead());
            if (child != null) {
                if (_path.hasTail()) {
                    return child.get(_path.getTail());
                } else {
                    return Optional.of(child);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Stream<TreeNode> walk() {
        return Stream.concat(Stream.of(this), children.values().stream().flatMap(TreeNode::walk));
    }

    @Override
    public boolean isEmpty() {
        return this.children.isEmpty();
    }

    @Override
    public TreeNode merge(TreeNode otherNode) {
        if (otherNode instanceof MapNode) {
            ((MapNode) otherNode).children.forEach((nodeName, treeNode) -> {
                TreeNode node = this.children.containsKey(nodeName) ? this.children.get(nodeName)
                        .merge(treeNode) : treeNode;
                this.children.put(nodeName, node);
            });
            return this;
        } else {
            throw ConfigurationException.createNew(ConfigurationErrorCode.ILLEGAL_TREE_MERGE)
                    .put("firstNodeType", otherNode.getClass().getSimpleName())
                    .put("secondNodeType", getClass().getSimpleName());
        }
    }

    @Override
    public TreeNode set(String name, TreeNode treeNode) {
        Path path = new Path(name);
        if (path.hasTail()) {
            TreeNode nexNode = getOrCreateNode(path);
            TreeNode finalNode = nexNode.set(path.getTail(), treeNode);
            children.put(path.getHead(), nexNode);
            return finalNode;
        } else {
            children.put(path.getHead(), treeNode);
            return treeNode;
        }
    }

    @Override
    public TreeNode remove(String name) {
        Path path = new Path(name);
        if (path.hasTail()) {
            if (children.containsKey(path.getHead())) {
                TreeNode treeNode = children.get(path.getHead());
                try {
                    TreeNode removedNode = treeNode.remove(path.getTail());
                    removeEmptyIntermediateNode(path, treeNode);
                    return removedNode;
                } catch (PropertyNotFoundException e) {
                    throw new PropertyNotFoundException(e, path.getHead());
                }
            } else {
                throw new PropertyNotFoundException(name);
            }
        } else {
            return children.remove(path.getHead());
        }
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
        if (isHidden()) {
            return "\"" + HIDDEN_PLACEHOLDER + "\"";
        } else {
            return children.entrySet().stream().map(entry -> {
                if (entry.getValue().type() == Type.VALUE_NODE) {
                    return entry.getKey() + ": " + entry.getValue().toString();
                } else {
                    return entry.getKey() + ":\n" + indent(entry.getValue().toString());
                }
            }).collect(Collectors.joining("\n"));
        }
    }

    private TreeNode getOrCreateNode(Path path) {
        TreeNode treeNode;
        if (children.containsKey(path.getHead())) {
            treeNode = children.get(path.getHead());
        } else {
            treeNode = new Path(path.getTail()).createNode();
        }
        return treeNode;
    }

    private void removeEmptyIntermediateNode(Path path, TreeNode treeNode) {
        if (treeNode.isEmpty()) {
            children.remove(path.getHead());
        }
    }
}
