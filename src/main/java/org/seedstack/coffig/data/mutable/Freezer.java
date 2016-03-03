/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data.mutable;

import org.seedstack.coffig.data.NamedNode;
import org.seedstack.coffig.data.TreeNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public final class Freezer {

    public static MutableTreeNode[] unfreeze(TreeNode... childNodes) {
        return Arrays.stream(childNodes).map(TreeNode::unfreeze).toArray(MutableTreeNode[]::new);
    }

    public static NamedNode[] unfreeze(NamedNode... childNodes) {
        return Arrays.stream(childNodes)
                .map(namedNode -> new NamedNode(namedNode.name(), namedNode.get().unfreeze()))
                .toArray(NamedNode[]::new);
    }

    public static MutableTreeNode[] unfreeze(String... childNodes) {
        return Arrays.stream(childNodes).map(MutableValueNode::new).toArray(MutableTreeNode[]::new);
    }

    public static List<MutableTreeNode> unfreeze(List<TreeNode> childNodes) {
        return childNodes.stream().map(TreeNode::unfreeze).collect(toList());
    }

    public static Map<String, MutableTreeNode> unfreeze(Map<String, TreeNode> newChildNodes) {
        Map<String, MutableTreeNode> newTreeNodes = new HashMap<>();
        newChildNodes.forEach((key, val) -> newChildNodes.put(key, val.unfreeze()));
        return newTreeNodes;
    }
}
