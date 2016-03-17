/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Pierre THIROUIN (pierre.thirouin@ext.inetpsa.com)
 */
abstract class AbstractTreeNode implements TreeNode {

    public TreeNode get(String name) {
        Prefix prefix = new Prefix(name);
        TreeNode treeNode;
        try {
            treeNode = doGet(prefix.head);
        } catch (PropertyNotFoundException e) {
            throw new PropertyNotFoundException("[" + name + "]");
        }
        if (prefix.tail.isPresent()) {
            try {
                treeNode = treeNode.get(prefix.tail.get());
            } catch (PropertyNotFoundException e) {
                throw new PropertyNotFoundException(e, prefix);
            }
        }
        return treeNode;
    }

    protected TreeNode doGet(String name) {
        throw new PropertyNotFoundException("[" + name + "]");
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
