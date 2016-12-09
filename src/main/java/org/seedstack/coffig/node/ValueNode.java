/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import org.seedstack.coffig.TreeNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ValueNode extends AbstractTreeNode {
    String value;

    public ValueNode(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public TreeNode item(String key) {
        return this;
    }

    @Override
    public Collection<TreeNode> items() {
        ArrayList<TreeNode> treeNodes = new ArrayList<>(1);
        treeNodes.add(this);
        return treeNodes;
    }

    @Override
    public Optional<TreeNode> get(String path) {
        return Optional.empty();
    }

    @Override
    public Stream<TreeNode> stream() {
        return Stream.of(this);
    }

    @Override
    public TreeNode merge(TreeNode otherNode) {
        return otherNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !getClass().isAssignableFrom(o.getClass()) && !o.getClass().isAssignableFrom(getClass()))
            return false;
        ValueNode valueNode = (ValueNode) o;
        return Objects.equals(value, valueNode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public ValueNode freeze() {
        return this;
    }

    @Override
    public MutableValueNode unfreeze() {
        return new MutableValueNode(value);
    }
}
