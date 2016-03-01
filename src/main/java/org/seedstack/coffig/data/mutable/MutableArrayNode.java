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
        Prefix prefix = new Prefix(name);
        TreeNode treeNode;
        if (prefix.tail.isPresent()) {
            treeNode = getOrCreateTreeNode(prefix);
            ((MutableTreeNode) treeNode).set(prefix.tail.get(), value);
        } else {
            treeNode = value;
        }

        if (prefix.index == childNodes.size()) {
            childNodes.add(treeNode);
        } else {
            childNodes.set(prefix.index, treeNode);
        }
    }

    private TreeNode getOrCreateTreeNode(Prefix prefix) {
        TreeNode treeNode;
        if (!isIndexPresent(prefix)) {
            treeNode = createTreeNode(prefix);
        } else {
            treeNode = childNodes.get(prefix.index);
            assertMutable(treeNode);
        }
        return treeNode;
    }

    private TreeNode createTreeNode(Prefix prefix) {
        TreeNode treeNode;
        if (new Prefix(prefix.tail.get()).isArray) {
            treeNode = new MutableArrayNode();
        } else {
            treeNode = new MutableMapNode();
        }
        return treeNode;
    }

    @Override
    public void remove(String name) {
        Prefix prefix = new Prefix(name);
        if (prefix.tail.isPresent()) {
            if (isIndexPresent(prefix)) {
                TreeNode treeNode = childNodes.get(prefix.index);
                assertMutable(treeNode);

                MutableTreeNode mutableTreeNode = (MutableTreeNode) treeNode;
                mutableTreeNode.remove(prefix.tail.get());
                if (mutableTreeNode.isEmpty()) {
                    childNodes.remove(prefix.index);
                }
            }
        } else {
            childNodes.remove(prefix.index);
        }
    }

    private boolean isIndexPresent(Prefix prefix) {
        return childNodes.size() > prefix.index;
    }

    @Override
    public boolean isEmpty() {
        return childNodes.isEmpty();
    }
}
