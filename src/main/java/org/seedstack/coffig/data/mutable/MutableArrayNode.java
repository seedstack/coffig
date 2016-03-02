/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data.mutable;

import org.seedstack.coffig.data.ArrayNode;
import org.seedstack.coffig.data.TreeNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MutableArrayNode extends ArrayNode implements MutableTreeNode {
    public MutableArrayNode(TreeNode... childNodes) {
        super(childNodes);
    }

    public MutableArrayNode(String... childNodes) {
        super(childNodes);
    }

    public MutableArrayNode(List<TreeNode> childNodes) {
        super(childNodes);
    }

    public MutableArrayNode() {
        super(new ArrayList<>());
    }

    public void add(TreeNode treeNode) {
        childNodes.add(treeNode);
    }

    public void add(int index, TreeNode treeNode) {
        childNodes.add(index, treeNode);
    }

    public boolean addAll(Collection<? extends TreeNode> c) {
        return childNodes.addAll(c);
    }

    public void remove(TreeNode treeNode) {
        childNodes.remove(treeNode);
    }

    public void clear() {
        childNodes.clear();
    }

    @Override
    public void set(String name, TreeNode value) {
        String[] split = name.split("\\.", 2);
        String head = split[0];
        int index = Integer.valueOf(head);
        TreeNode treeNode;
        if (split.length == 2) {
            String tail = split[1];

            if (childNodes.size() > index) {
                treeNode = childNodes.get(index);
            } else {
                String newHead = tail.split("\\.", 2)[0];
                if (isArrayNode(newHead)) {
                    treeNode = new MutableArrayNode();
                } else {
                    treeNode = new MutableMapNode();
                }
            }
            assertMutable(treeNode);
            ((MutableTreeNode) treeNode).set(tail, value);
        } else {
            treeNode = value;
        }
        if (index == childNodes.size()) {
            childNodes.add(treeNode);
        } else {
            childNodes.set(index, treeNode);
        }
    }
}
