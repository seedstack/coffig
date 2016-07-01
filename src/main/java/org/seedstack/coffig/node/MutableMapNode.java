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
        Path path = new Path(name);
        TreeNode treeNode = value.unfreeze();
        if (path.hasTail()) {
            MutableTreeNode nexNode = getOrCreateNode(path);
            MutableTreeNode finalNode = nexNode.set(path.getTail(), treeNode);
            children.put(path.getHead(), nexNode);
            return finalNode;
        } else {
            children.put(path.getHead(), treeNode);
            return (MutableTreeNode) treeNode;
        }
    }

    @Override
    public MutableTreeNode remove(String name) {
        Path path = new Path(name);
        if (path.hasTail()) {
            if (children.containsKey(path.getHead())) {
                TreeNode treeNode = children.get(path.getHead());
                MutableTreeNode mutableTreeNode = (MutableTreeNode) treeNode;
                try {
                    MutableTreeNode removedNode = mutableTreeNode.remove(path.getTail());
                    removeEmptyIntermediateNode(path, mutableTreeNode);
                    return removedNode;
                } catch (PropertyNotFoundException e) {
                    throw new PropertyNotFoundException(e, path.getHead());
                }
            } else {
                throw new PropertyNotFoundException(name);
            }
        } else {
            return (MutableTreeNode) children.remove(path.getHead());
        }
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    public MapNode freeze() {
        return new MapNode(children);
    }

    @Override
    public MutableMapNode unfreeze() {
        return this;
    }

    private MutableTreeNode getOrCreateNode(Path path) {
        MutableTreeNode mutableTreeNode;
        if (children.containsKey(path.getHead())) {
            TreeNode treeNode = children.get(path.getHead());
            mutableTreeNode = (MutableTreeNode) treeNode;
        } else {
            mutableTreeNode = MutableTreeNodeFactory.createFromPath(path.getTail());
        }
        return mutableTreeNode;
    }

    private void removeEmptyIntermediateNode(Path path, MutableTreeNode mutableTreeNode) {
        if (mutableTreeNode.isEmpty()) {
            children.remove(path.getHead());
        }
    }
}
