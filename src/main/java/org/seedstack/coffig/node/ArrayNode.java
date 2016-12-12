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
import org.seedstack.coffig.PropertyNotFoundException;
import org.seedstack.coffig.TreeNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class ArrayNode extends AbstractTreeNode {
    final List<TreeNode> childNodes;

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
        throw ConfigurationException.createNew(ConfigurationErrorCode.CANNOT_ACCESS_ARRAY_AS_SINGLE_VALUE);
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
    public Collection<TreeNode> items() {
        return Collections.unmodifiableList(childNodes);
    }

    @Override
    public Optional<TreeNode> get(String path) {
        Path _path = new Path(path);
        Optional<TreeNode> treeNode = Optional.empty();

        if (_path.isArray()) {
            int index = _path.getIndex();
            if (index >= 0 && index < childNodes.size()) {
                treeNode = Optional.ofNullable(childNodes.get(index));
            }
            if (treeNode.isPresent() && _path.hasTail()) {
                return treeNode.get().get(_path.getTail());
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
    public ArrayNode freeze() {
        return this;
    }

    @Override
    public MutableArrayNode unfreeze() {
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
