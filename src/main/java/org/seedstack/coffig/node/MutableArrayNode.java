/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import org.seedstack.coffig.MutableNodeAttributes;
import org.seedstack.coffig.MutableTreeNode;
import org.seedstack.coffig.PropertyNotFoundException;
import org.seedstack.coffig.TreeNode;

import java.util.List;

public class MutableArrayNode extends ArrayNode implements MutableTreeNode {
    public MutableArrayNode() {
    }

    public MutableArrayNode(TreeNode... childNodes) {
        super.childNodes.addAll(Freezer.unfreeze(childNodes));
    }

    public MutableArrayNode(String... childNodes) {
        super.childNodes.addAll(Freezer.unfreeze(childNodes));
    }

    public MutableArrayNode(List<TreeNode> childNodes) {
        super.childNodes.addAll(Freezer.unfreeze(childNodes));
    }

    @Override
    public MutableNodeAttributes attributes() {
        return (MutableNodeAttributes) super.attributes();
    }

    public void add(TreeNode treeNode) {
        childNodes.add(treeNode.unfreeze());
    }

    public void add(int index, TreeNode treeNode) {
        childNodes.add(index, treeNode.unfreeze());
    }

    public boolean addAll(List<TreeNode> c) {
        return childNodes.addAll(Freezer.unfreeze(c));
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
        if (prefix.hasTail()) {
            newTreeNode = getOrCreateTreeNode(prefix);
            ((MutableTreeNode) newTreeNode).set(prefix.getTail(), treeNode);
        } else {
            newTreeNode = treeNode;
        }

        if (prefix.getIndex() == childNodes.size()) {
            childNodes.add(newTreeNode);
        } else {
            childNodes.set(prefix.getIndex(), newTreeNode);
        }
        return this;
    }

    @Override
    public MutableTreeNode remove(String name) {
        Prefix prefix = new Prefix(name);
        if (prefix.hasTail()) {
            if (isIndexPresent(prefix)) {
                MutableTreeNode treeNode = (MutableTreeNode) childNodes.get(prefix.getIndex());
                try {
                    MutableTreeNode removedNode = treeNode.remove(prefix.getTail());
                    if (treeNode.isEmpty()) {
                        childNodes.remove(prefix.getIndex());
                    }
                    return removedNode;
                } catch (PropertyNotFoundException e) {
                    throw new PropertyNotFoundException(e, prefix.getHead());
                }
            } else {
                throw new PropertyNotFoundException(name);
            }
        } else {
            return (MutableTreeNode) childNodes.remove(prefix.getIndex());
        }
    }

    @Override
    public boolean isEmpty() {
        return childNodes.isEmpty();
    }

    @Override
    public TreeNode freeze() {
        return new ArrayNode(childNodes);
    }

    @Override
    public MutableTreeNode unfreeze() {
        return this;
    }

    private TreeNode getOrCreateTreeNode(Prefix prefix) {
        TreeNode treeNode;
        if (!isIndexPresent(prefix)) {
            treeNode = MutableTreeNodeFactory.createFromPrefix(prefix.getTail());
        } else {
            treeNode = childNodes.get(prefix.getIndex());
        }
        return treeNode;
    }

    private boolean isIndexPresent(Prefix prefix) {
        return childNodes.size() > prefix.getIndex();
    }
}
