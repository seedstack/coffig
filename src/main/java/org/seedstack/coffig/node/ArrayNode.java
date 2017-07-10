/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import org.seedstack.coffig.ConfigurationErrorCode;
import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.NamedNode;
import org.seedstack.coffig.PropertyNotFoundException;
import org.seedstack.coffig.TreeNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class ArrayNode extends AbstractTreeNode {
    private final List<TreeNode> children;

    public ArrayNode() {
        children = new ArrayList<>();
    }

    public ArrayNode(ArrayNode other) {
        super(other);
        this.children = new ArrayList<>(other.children);
    }

    public ArrayNode(TreeNode... children) {
        this.children = Arrays.stream(children).collect(toList());
    }

    public ArrayNode(List<TreeNode> children) {
        this.children = new ArrayList<>(children);
    }

    public ArrayNode(String... children) {
        this.children = Arrays.stream(children).map(ValueNode::new).collect(toList());
    }

    @Override
    public Type type() {
        return Type.ARRAY_NODE;
    }

    @Override
    public String value() {
        throw ConfigurationException.createNew(ConfigurationErrorCode.CANNOT_ACCESS_ARRAY_AS_SINGLE_VALUE);
    }

    @Override
    public Stream<TreeNode> nodes() {
        return children.stream();
    }

    @Override
    public Stream<NamedNode> namedNodes() {
        return children.stream().map(child -> new NamedNode(child.value(), (String) null));
    }

    @Override
    public TreeNode node(String key) {
        try {
            return children.get(Integer.parseInt(key));
        } catch (Exception e) {
            throw new PropertyNotFoundException(key, e);
        }
    }

    @Override
    public Optional<TreeNode> get(String path) {
        Path _path = new Path(path);
        Optional<TreeNode> treeNode = Optional.empty();

        if (_path.isArray()) {
            int index = _path.getIndex();
            if (index >= 0 && index < children.size()) {
                treeNode = Optional.ofNullable(children.get(index));
            }
            if (treeNode.isPresent() && _path.hasTail()) {
                return treeNode.get().get(_path.getTail());
            }
        }

        return treeNode;
    }

    @Override
    public Stream<TreeNode> walk() {
        return Stream.concat(Stream.of(this), children.stream().flatMap(TreeNode::walk));
    }

    @Override
    public boolean isEmpty() {
        return this.children.isEmpty();
    }

    @Override
    public TreeNode merge(TreeNode otherNode) {
        return otherNode;
    }


    @Override
    public TreeNode set(String name, TreeNode treeNode) {
        if (name == null) {
            children.add(treeNode);
        } else {
            Path path = new Path(name);
            TreeNode newTreeNode;
            if (path.hasTail()) {
                newTreeNode = getOrCreateTreeNode(path);
                newTreeNode.set(path.getTail(), treeNode);
            } else {
                newTreeNode = treeNode;
            }

            if (path.getIndex() == children.size()) {
                children.add(newTreeNode);
            } else {
                children.set(path.getIndex(), newTreeNode);
            }
        }
        return this;
    }

    @Override
    public TreeNode remove(String name) {
        Path path = new Path(name);
        if (path.hasTail()) {
            if (children.size() > path.getIndex()) {
                TreeNode treeNode = children.get(path.getIndex());
                try {
                    TreeNode removedNode = treeNode.remove(path.getTail());
                    if (treeNode.isEmpty()) {
                        children.remove(path.getIndex());
                    }
                    return removedNode;
                } catch (PropertyNotFoundException e) {
                    throw new PropertyNotFoundException(e, path.getHead());
                }
            } else {
                throw new PropertyNotFoundException(name);
            }
        } else {
            return children.remove(path.getIndex());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !getClass().isAssignableFrom(o.getClass()) && !o.getClass().isAssignableFrom(getClass()))
            return false;
        ArrayNode arrayNode = (ArrayNode) o;
        return children.equals(arrayNode.children);
    }

    @Override
    public int hashCode() {
        return children.hashCode();
    }

    @Override
    public String toString() {
        if (isHidden()) {
            return "\"" + HIDDEN_PLACEHOLDER + "\"";
        } else if (children.size() > 0 && children.get(0).type() == Type.VALUE_NODE) {
            return children.stream().map(item -> "- " + item.toString()).collect(joining("\n"));
        } else {
            return children.stream().map(item -> "-\n" + indent(item.toString())).collect(joining("\n"));
        }
    }

    private TreeNode getOrCreateTreeNode(Path path) {
        TreeNode treeNode;
        int index = path.getIndex();
        if (children.size() > index) {
            treeNode = children.get(index);
        } else {
            treeNode = new Path(path.getTail()).createNode();
        }
        return treeNode;
    }
}
