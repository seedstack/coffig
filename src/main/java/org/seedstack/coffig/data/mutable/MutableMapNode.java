/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data.mutable;

import org.seedstack.coffig.PropertyNotFoundException;
import org.seedstack.coffig.data.MapNode;
import org.seedstack.coffig.data.NamedNode;
import org.seedstack.coffig.data.TreeNode;

import java.util.HashMap;
import java.util.Map;

import static org.seedstack.coffig.data.mutable.MutableTreeNodeFactory.createFromPrefix;

public class MutableMapNode extends MapNode implements MutableTreeNode {

    public MutableMapNode(NamedNode... childNodes) {
        super(Freezer.unfreeze(childNodes));
    }

    @SuppressWarnings("unchecked")
    public MutableMapNode(Map<String, TreeNode> newChildNodes) {
        super((Map)Freezer.unfreeze(newChildNodes));
    }

    public MutableMapNode() {
        super(new HashMap<>());
    }

    public TreeNode put(String key, TreeNode value) {
        return childNodes.put(key, value.unfreeze());
    }

    @SuppressWarnings("unchecked")
    public void putAll(Map<String, TreeNode> m) {
        childNodes.putAll(((Map)Freezer.unfreeze(m)));
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

    private MutableTreeNode getOrCreateNode(Prefix prefix) {
        MutableTreeNode mutableTreeNode;
        if (childNodes.containsKey(prefix.head)) {
            TreeNode treeNode = childNodes.get(prefix.head);
            mutableTreeNode = (MutableTreeNode) treeNode;
        } else {
            mutableTreeNode = createFromPrefix(prefix.tail.get());
        }
        return mutableTreeNode;
    }

    @Override
    public MutableTreeNode remove(String name) {
        Prefix prefix = new Prefix(name);
        if (prefix.tail.isPresent()) {
            if (childNodes.containsKey(prefix.head)) {
                TreeNode treeNode = childNodes.get(prefix.head);
                MutableTreeNode mutableTreeNode = (MutableTreeNode) treeNode;
                MutableTreeNode removedNode = mutableTreeNode.remove(prefix.tail.get());
                removeEmptyIntermediateNode(prefix, mutableTreeNode);
                return removedNode;
            } else {
                throw new PropertyNotFoundException(name);
            }
        } else {
            return (MutableTreeNode) childNodes.remove(prefix.head);
        }
    }

    private void removeEmptyIntermediateNode(Prefix prefix, MutableTreeNode mutableTreeNode) {
        if (mutableTreeNode.isEmpty()) {
            childNodes.remove(prefix.head);
        }
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
        Map<String, TreeNode> nodes = new HashMap<>();
        childNodes.forEach((key, val) -> nodes.put(key, ((MutableTreeNode) val).freeze()));
        return new MapNode(nodes);
    }
}
