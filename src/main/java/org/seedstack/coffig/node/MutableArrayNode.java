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
        Path path = new Path(name);
        TreeNode treeNode = value.unfreeze();
        TreeNode newTreeNode;
        if (path.hasTail()) {
            newTreeNode = getOrCreateTreeNode(path);
            ((MutableTreeNode) newTreeNode).set(path.getTail(), treeNode);
        } else {
            newTreeNode = treeNode;
        }

        if (path.getIndex() == childNodes.size()) {
            childNodes.add(newTreeNode);
        } else {
            childNodes.set(path.getIndex(), newTreeNode);
        }
        return this;
    }

    @Override
    public MutableTreeNode remove(String name) {
        Path path = new Path(name);
        if (path.hasTail()) {
            if (isIndexPresent(path)) {
                MutableTreeNode treeNode = (MutableTreeNode) childNodes.get(path.getIndex());
                try {
                    MutableTreeNode removedNode = treeNode.remove(path.getTail());
                    if (treeNode.isEmpty()) {
                        childNodes.remove(path.getIndex());
                    }
                    return removedNode;
                } catch (PropertyNotFoundException e) {
                    throw new PropertyNotFoundException(e, path.getHead());
                }
            } else {
                throw new PropertyNotFoundException(name);
            }
        } else {
            return (MutableTreeNode) childNodes.remove(path.getIndex());
        }
    }

    @Override
    public boolean isEmpty() {
        return childNodes.isEmpty();
    }

    @Override
    public ArrayNode freeze() {
        return new ArrayNode(childNodes);
    }

    @Override
    public MutableArrayNode unfreeze() {
        return this;
    }

    private TreeNode getOrCreateTreeNode(Path path) {
        TreeNode treeNode;
        if (!isIndexPresent(path)) {
            treeNode = MutableTreeNodeFactory.createFromPath(path.getTail());
        } else {
            treeNode = childNodes.get(path.getIndex());
        }
        return treeNode;
    }

    private boolean isIndexPresent(Path path) {
        return childNodes.size() > path.getIndex();
    }
}
