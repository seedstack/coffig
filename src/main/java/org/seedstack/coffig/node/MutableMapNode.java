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

import java.util.Map;

public class MutableMapNode extends MapNode implements MutableTreeNode {
    public MutableMapNode() {
    }

    public MutableMapNode(NamedNode... childNodes) {
        super.children.putAll(Freezer.unfreeze(childNodes));
    }

    public MutableMapNode(Map<String, TreeNode> newChildNodes) {
        super.children.putAll(Freezer.unfreeze(newChildNodes));
    }

    @Override
    public MutableNodeAttributes attributes() {
        return (MutableNodeAttributes) super.attributes();
    }

    public TreeNode put(String key, TreeNode value) {
        return children.put(key, value.unfreeze());
    }

    public void putAll(Map<String, TreeNode> m) {
        children.putAll(Freezer.unfreeze(m));
    }

    public TreeNode remove(TreeNode treeNode) {
        return children.remove(treeNode);
    }

    public void clear() {
        children.clear();
    }

    @Override
    public MutableTreeNode set(String name, TreeNode value) {
        Prefix prefix = new Prefix(name);
        TreeNode treeNode = value.unfreeze();
        if (prefix.hasTail()) {
            MutableTreeNode nexNode = getOrCreateNode(prefix);
            MutableTreeNode finalNode = nexNode.set(prefix.getTail(), treeNode);
            children.put(prefix.getHead(), nexNode);
            return finalNode;
        } else {
            children.put(prefix.getHead(), treeNode);
            return (MutableTreeNode) treeNode;
        }
    }

    @Override
    public MutableTreeNode remove(String name) {
        Prefix prefix = new Prefix(name);
        if (prefix.hasTail()) {
            if (children.containsKey(prefix.getHead())) {
                TreeNode treeNode = children.get(prefix.getHead());
                MutableTreeNode mutableTreeNode = (MutableTreeNode) treeNode;
                try {
                    MutableTreeNode removedNode = mutableTreeNode.remove(prefix.getTail());
                    removeEmptyIntermediateNode(prefix, mutableTreeNode);
                    return removedNode;
                } catch (PropertyNotFoundException e) {
                    throw new PropertyNotFoundException(e, prefix.getHead());
                }
            } else {
                throw new PropertyNotFoundException(name);
            }
        } else {
            return (MutableTreeNode) children.remove(prefix.getHead());
        }
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    public TreeNode freeze() {
        return new MapNode(children);
    }

    @Override
    public MutableTreeNode unfreeze() {
        return this;
    }

    private MutableTreeNode getOrCreateNode(Prefix prefix) {
        MutableTreeNode mutableTreeNode;
        if (children.containsKey(prefix.getHead())) {
            TreeNode treeNode = children.get(prefix.getHead());
            mutableTreeNode = (MutableTreeNode) treeNode;
        } else {
            mutableTreeNode = MutableTreeNodeFactory.createFromPrefix(prefix.getTail());
        }
        return mutableTreeNode;
    }

    private void removeEmptyIntermediateNode(Prefix prefix, MutableTreeNode mutableTreeNode) {
        if (mutableTreeNode.isEmpty()) {
            children.remove(prefix.getHead());
        }
    }
}
