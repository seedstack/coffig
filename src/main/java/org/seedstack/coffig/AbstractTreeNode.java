/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Pierre THIROUIN (pierre.thirouin@ext.inetpsa.com)
 */
abstract class AbstractTreeNode implements TreeNode {

    public Optional<TreeNode> get(String prefix) {
        String[] split = prefix.split("\\.", 2);
        Optional<TreeNode> treeNode = doGet(split[0]);
        if (treeNode.isPresent() && split.length == 2) {
            treeNode = treeNode.get().get(split[1]);
        }
        return treeNode;
    }

    protected Optional<TreeNode> doGet(String name) {
        return Optional.empty();
    }

    public TreeNode value(String name) {
        throw new UnsupportedOperationException();
    }

    public String value() {
        throw new UnsupportedOperationException();
    }

    public TreeNode[] values() {
        throw new UnsupportedOperationException();
    }

    public TreeNode merge(TreeNode otherNode) {
        return otherNode;
    }

    protected String indent(String s) {
        return Arrays.stream(s.split("\n")).map(line -> "  " + line).collect(Collectors.joining("\n"));
    }
}
