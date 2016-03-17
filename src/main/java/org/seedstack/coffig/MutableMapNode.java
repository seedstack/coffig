/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import java.util.Map;

public class MutableMapNode extends MapNode implements MutableTreeNode {
    public MutableMapNode() {
    }

    public MutableMapNode(NamedNode... childNodes) {
        super.childNodes.putAll(Freezer.unfreeze(childNodes));
    }

    public MutableMapNode(Map<String, TreeNode> newChildNodes) {
        super.childNodes.putAll(Freezer.unfreeze(newChildNodes));
    }

    public TreeNode put(String key, TreeNode value) {
        return childNodes.put(key, value.unfreeze());
    }

    public void putAll(Map<String, TreeNode> m) {
        childNodes.putAll(Freezer.unfreeze(m));
    }

    public TreeNode remove(TreeNode treeNode) {
        return childNodes.remove(treeNode);
    }

    public void clear() {
        childNodes.clear();
    }

    @Override
    public MutableTreeNode set(String name, TreeNode value) {
        Prefix prefix = new Prefix(name);
        TreeNode treeNode = value.unfreeze();
        if (prefix.tail.isPresent()) {
            MutableTreeNode nexNode = getOrCreateNode(prefix);
            MutableTreeNode finalNode = nexNode.set(prefix.tail.get(), treeNode);
            childNodes.put(prefix.head, nexNode);
            return finalNode;
        } else {
            childNodes.put(prefix.head, treeNode);
            return (MutableTreeNode) treeNode;
        }
    }

    @Override
    public MutableTreeNode remove(String name) {
        Prefix prefix = new Prefix(name);
        if (prefix.tail.isPresent()) {
            if (childNodes.containsKey(prefix.head)) {
                TreeNode treeNode = childNodes.get(prefix.head);
                MutableTreeNode mutableTreeNode = (MutableTreeNode) treeNode;
                try {
                    MutableTreeNode removedNode = mutableTreeNode.remove(prefix.tail.get());
                    removeEmptyIntermediateNode(prefix, mutableTreeNode);
                    return removedNode;
                } catch (PropertyNotFoundException e) {
                    throw new PropertyNotFoundException(e, prefix);
                }
            } else {
                throw new PropertyNotFoundException("[" + name + "]");
            }
        } else {
            return (MutableTreeNode) childNodes.remove(prefix.head);
        }
    }

    @Override
    public boolean isEmpty() {
        return childNodes.isEmpty();
    }

    @Override
    public TreeNode freeze() {
        return new MapNode(childNodes);
    }

    @Override
    public MutableTreeNode unfreeze() {
        return this;
    }

    private MutableTreeNode getOrCreateNode(Prefix prefix) {
        MutableTreeNode mutableTreeNode;
        if (childNodes.containsKey(prefix.head)) {
            TreeNode treeNode = childNodes.get(prefix.head);
            mutableTreeNode = (MutableTreeNode) treeNode;
        } else {
            mutableTreeNode = MutableTreeNodeFactory.createFromPrefix(prefix.tail.get());
        }
        return mutableTreeNode;
    }

    private void removeEmptyIntermediateNode(Prefix prefix, MutableTreeNode mutableTreeNode) {
        if (mutableTreeNode.isEmpty()) {
            childNodes.remove(prefix.head);
        }
    }
}
