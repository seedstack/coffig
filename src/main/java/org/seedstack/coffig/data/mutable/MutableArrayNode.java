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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.seedstack.coffig.data.mutable.MutableTreeNodeFactory.createFromPrefix;

public class MutableArrayNode extends ArrayNode implements MutableTreeNode {
    public MutableArrayNode(TreeNode... childNodes) {
        super(Freezer.unfreeze(childNodes));
    }

    public MutableArrayNode(String... childNodes) {
        super(Freezer.unfreeze(childNodes));
    }

    @SuppressWarnings("unchecked")
    public MutableArrayNode(List<TreeNode> childNodes) {
        super((List)Freezer.unfreeze(childNodes));
    }

    public MutableArrayNode() {
        super(new ArrayList<>());
    }

    public void add(TreeNode treeNode) {
        childNodes.add(treeNode.unfreeze());
    }

    public void add(int index, TreeNode treeNode) {
        childNodes.add(index, treeNode.unfreeze());
    }

    public boolean addAll(Collection<? extends TreeNode> c) {
        return childNodes.addAll(c.stream().map(TreeNode::unfreeze).collect(toList()));
    }

    public void remove(TreeNode treeNode) {
        childNodes.remove(treeNode);
    }

    public void clear() {
        childNodes.clear();
    }

    @Override
    public MutableTreeNode set(String name, TreeNode value) {
        Prefix prefix = new Prefix(name);
        TreeNode treeNode = value.unfreeze();
        TreeNode newTreeNode;
        if (prefix.tail.isPresent()) {
            newTreeNode = getOrCreateTreeNode(prefix);
            ((MutableTreeNode) newTreeNode).set(prefix.tail.get(), treeNode);
        } else {
            newTreeNode = treeNode;
        }

        if (prefix.index == childNodes.size()) {
            childNodes.add(newTreeNode);
        } else {
            childNodes.set(prefix.index, newTreeNode);
        }
        return this;
    }

    private TreeNode getOrCreateTreeNode(Prefix prefix) {
        TreeNode treeNode;
        if (!isIndexPresent(prefix)) {
            treeNode = createFromPrefix(prefix.tail.get());
        } else {
            treeNode = childNodes.get(prefix.index);
        }
        return treeNode;
    }

    @Override
    public MutableTreeNode remove(String name) {
        Prefix prefix = new Prefix(name);
        if (prefix.tail.isPresent()) {
            if (isIndexPresent(prefix)) {
                TreeNode treeNode = childNodes.get(prefix.index);
                MutableTreeNode mutableTreeNode = (MutableTreeNode) treeNode;
                mutableTreeNode.remove(prefix.tail.get());
                if (mutableTreeNode.isEmpty()) {

                    childNodes.remove(prefix.index);
                }
            }
        } else {
            childNodes.remove(prefix.index);
        }
        return this;
    }

    private boolean isIndexPresent(Prefix prefix) {
        return childNodes.size() > prefix.index;
    }

    @Override
    public boolean isEmpty() {
        return childNodes.isEmpty();
    }

    @Override
    public MutableTreeNode unfreeze() {
        return this;
    }

    @Override
    public TreeNode freeze() {
        return new ArrayNode(childNodes.stream().map(treeNode -> ((MutableTreeNode) treeNode).freeze()).collect(toList()));
    }
}
