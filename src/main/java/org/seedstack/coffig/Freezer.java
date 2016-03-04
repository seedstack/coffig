/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

final class Freezer {

    static List<TreeNode> freeze(TreeNode... childNodes) {
        return Arrays.stream(childNodes).map(TreeNode::freeze).collect(toList());
    }

    static List<MutableTreeNode> unfreeze(TreeNode... childNodes) {
        return Arrays.stream(childNodes).map(TreeNode::unfreeze).collect(toList());
    }

    static List<TreeNode> freeze(String... childNodes) {
        return Arrays.stream(childNodes).map(ValueNode::new).collect(toList());
    }

    static List<MutableTreeNode> unfreeze(String... childNodes) {
        return Arrays.stream(childNodes).map(MutableValueNode::new).collect(toList());
    }

    static List<TreeNode> freeze(List<TreeNode> childNodes) {
        return childNodes.stream().map(TreeNode::freeze).collect(toList());
    }

    static List<MutableTreeNode> unfreeze(List<TreeNode> childNodes) {
        return childNodes.stream().map(TreeNode::unfreeze).collect(toList());
    }

    static Map<String, TreeNode> freeze(Map<String, TreeNode> newChildNodes) {
        return newChildNodes.keySet().stream().collect(toMap(Function.identity(), name -> newChildNodes.get(name).freeze()));
    }

    static Map<String, MutableTreeNode> unfreeze(Map<String, TreeNode> newChildNodes) {
        return newChildNodes.keySet().stream().collect(toMap(Function.identity(), name -> newChildNodes.get(name).unfreeze()));
    }

    static Map<String, TreeNode> freeze(NamedNode... childNodes) {
        return Arrays.stream(childNodes).collect(toMap(NamedNode::name, namedNode -> namedNode.get().freeze()));
    }

    static Map<String, MutableTreeNode> unfreeze(NamedNode... childNodes) {
        return Arrays.stream(childNodes).collect(toMap(NamedNode::name, namedNode -> namedNode.get().unfreeze()));
    }
}
