/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data;

import org.seedstack.coffig.PropertyNotFoundException;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ArrayNode extends TreeNode {
    private final TreeNode[] childNodes;

    public ArrayNode(TreeNode... childNodes) {
        this.childNodes = childNodes;
    }

    public ArrayNode(String... childNodes) {
        List<ValueNode> valueNodes = Arrays.stream(childNodes).map(ValueNode::new).collect(toList());
        this.childNodes = valueNodes.toArray(new TreeNode[valueNodes.size()]);
    }

    @Override
    public TreeNode doSearch(String name) {
        try {
            Integer integer = Integer.valueOf(name);
            return childNodes[integer];
        } catch (NumberFormatException e) {
            throw new PropertyNotFoundException("Configuration array node is expected a number as index, but found: " + name);
        } catch (ArrayIndexOutOfBoundsException e2) {
            throw new PropertyNotFoundException(e2, name);
        }
    }

    @Override
    public TreeNode[] values() {
        return childNodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayNode arrayNode = (ArrayNode) o;
        return Arrays.equals(childNodes, arrayNode.childNodes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(childNodes);
    }
}
