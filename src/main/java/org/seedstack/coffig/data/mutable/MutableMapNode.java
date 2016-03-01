/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data.mutable;

import org.seedstack.coffig.data.MapNode;
import org.seedstack.coffig.data.PairNode;
import org.seedstack.coffig.data.TreeNode;

import java.util.HashMap;
import java.util.Map;

public class MutableMapNode extends MapNode implements MutableTreeNode {

    public MutableMapNode(PairNode... childNodes) {
        super(childNodes);
    }

    public MutableMapNode(Map<String, PairNode> newChildNodes) {
        super(newChildNodes);
    }

    public MutableMapNode() {
        super(new HashMap<>());
    }

    public PairNode put(String key, PairNode value) {
        return childNodes.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends PairNode> m) {
        childNodes.putAll(m);
    }

    public void clear() {
        childNodes.clear();
    }

    @Override
    public void set(String name, TreeNode value) {
        Prefix prefix = new Prefix(name);
        if (prefix.tail.isPresent()) {
            MutablePairNode mutablePairNode;
            if (childNodes.containsKey(prefix.head)) {
                PairNode pairNode = childNodes.get(prefix.head);
                assertMutable(pairNode);
                mutablePairNode = (MutablePairNode) pairNode;
            } else {
                mutablePairNode = new MutablePairNode();
            }
            mutablePairNode.set(name, value);
            childNodes.put(prefix.head, mutablePairNode);
        } else {
            childNodes.put(prefix.head, new MutablePairNode(prefix.head, value));
        }
    }

    @Override
    public void remove(String name) {
        Prefix prefix = new Prefix(name);
        if (prefix.tail.isPresent()) {
            if (childNodes.containsKey(prefix.head)) {
                TreeNode treeNode = childNodes.get(prefix.head);
                assertMutable(treeNode);
                MutableTreeNode mutableTreeNode = (MutableTreeNode) treeNode;
                mutableTreeNode.remove(prefix.tail.get());
                if (mutableTreeNode.isEmpty()) {
                    childNodes.remove(prefix.head);
                }
            }
        } else {
            childNodes.remove(prefix.head);
        }
    }

    @Override
    public boolean isEmpty() {
        return childNodes.isEmpty();
    }
}
