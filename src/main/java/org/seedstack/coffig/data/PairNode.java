/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data;

import org.seedstack.coffig.ConfigurationException;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.seedstack.coffig.ConfigurationException.INCORRECT_MERGE;

public class PairNode extends AbstractTreeNode {
    protected String name;
    protected TreeNode value;

    public PairNode(String name, TreeNode value) {
        this.name = name;
        this.value = value;
    }

    public PairNode(String name, String value) {
        this(name, new ValueNode(value));
    }

    public PairNode(String name, String... values) {
        this(name, new ArrayNode(values));
    }

    public String name() {
        return name;
    }

    public TreeNode get() {
        return value;
    }

    @Override
    public TreeNode merge(TreeNode otherNode) {
        if (otherNode instanceof PairNode) {
            return new PairNode(name, value.merge(((PairNode) otherNode).value));
        } else {
            throw new ConfigurationException(INCORRECT_MERGE.apply(otherNode.getClass(), getClass()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairNode pairNode = (PairNode) o;
        return Objects.equals(name, pairNode.name) &&
                Objects.equals(value, pairNode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        if (value instanceof ValueNode) {
            return name + ": " + value.toString();
        } else {
            return name + ":\n" + indent(value.toString());
        }
    }
}
