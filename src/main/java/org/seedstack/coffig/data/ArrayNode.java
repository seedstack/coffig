/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data;

import org.seedstack.coffig.PropertyNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ArrayNode extends TreeNode {
    protected final List<TreeNode> childNodes;

    public ArrayNode(TreeNode... childNodes) {
        this.childNodes = new ArrayList<>(Arrays.asList(childNodes));
    }

    public ArrayNode(String... childNodes) {
        this.childNodes = Arrays.stream(childNodes).map(ValueNode::new).collect(toList());
    }

    public ArrayNode(List<TreeNode> childNodes) {
        this.childNodes = new ArrayList<>(childNodes);
    }

    @Override
    public TreeNode doSearch(String name) {
        try {
            Integer integer = Integer.valueOf(name);
            return childNodes.get(integer);
        } catch (NumberFormatException e) {
            throw new PropertyNotFoundException("Configuration array node is expected a number as index, but found: " + name);
        } catch (IndexOutOfBoundsException e2) {
            throw new PropertyNotFoundException(e2, name);
        }
    }

    @Override
    public TreeNode[] values() {
        return childNodes.toArray(new TreeNode[childNodes.size()]);
    }

    public TreeNode value(int index) {
        return childNodes.get(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayNode arrayNode = (ArrayNode) o;
        return childNodes.equals(arrayNode.childNodes);
    }

    @Override
    public int hashCode() {
        return childNodes.hashCode();
    }
}
