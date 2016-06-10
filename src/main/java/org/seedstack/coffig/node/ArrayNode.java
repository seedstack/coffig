/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.MutableTreeNode;
import org.seedstack.coffig.PropertyNotFoundException;
import org.seedstack.coffig.TreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class ArrayNode extends AbstractTreeNode {
    protected final List<TreeNode> childNodes;

    public ArrayNode() {
        childNodes = new ArrayList<>();
    }

    public ArrayNode(TreeNode... childNodes) {
        this.childNodes = new ArrayList<>(Freezer.freeze(childNodes));
    }

    public ArrayNode(String... childNodes) {
        this.childNodes = Freezer.freeze(childNodes);
    }

    public ArrayNode(List<TreeNode> childNodes) {
        this.childNodes = new ArrayList<>(Freezer.freeze(childNodes));
    }

    @Override
    public String value() {
        throw new ConfigurationException("Cannot access array as single value");
    }

    @Override
    public TreeNode item(String key) {
        try {
            return childNodes.get(Integer.parseInt(key));
        } catch (Exception e) {
            throw new PropertyNotFoundException(key, e);
        }
    }

    @Override
    public TreeNode[] items() {
        return childNodes.toArray(new TreeNode[childNodes.size()]);
    }

    @Override
    public Optional<TreeNode> get(String path) {
        Prefix prefix = new Prefix(path);
        Optional<TreeNode> treeNode = Optional.empty();

        if (prefix.isArray()) {
            int index = prefix.getIndex();
            if (index >= 0 && index < childNodes.size()) {
                treeNode = Optional.ofNullable(childNodes.get(index));
            }
            if (treeNode.isPresent() && prefix.hasTail()) {
                return treeNode.get().get(prefix.getTail());
            }
        }

        return treeNode;
    }

    @Override
    public Stream<TreeNode> stream() {
        return Stream.concat(Stream.of(this), childNodes.stream().flatMap(TreeNode::stream));
    }

    @Override
    public TreeNode merge(TreeNode otherNode) {
        return otherNode;
    }

    @Override
    public TreeNode freeze() {
        return this;
    }

    @Override
    public MutableTreeNode unfreeze() {
        return new MutableArrayNode(childNodes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !getClass().isAssignableFrom(o.getClass()) && !o.getClass().isAssignableFrom(getClass()))
            return false;
        ArrayNode arrayNode = (ArrayNode) o;
        return childNodes.equals(arrayNode.childNodes);
    }

    @Override
    public int hashCode() {
        return childNodes.hashCode();
    }

    @Override
    public String toString() {
        if (childNodes.size() > 0 && childNodes.get(0) instanceof ValueNode) {
            return childNodes.stream().map(item -> "- " + item.toString()).collect(joining("\n"));
        } else {
            return childNodes.stream().map(item -> "-\n" + indent(item.toString())).collect(joining("\n"));
        }
    }
}
