/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data.mutable;

import org.seedstack.coffig.ConfigurationException;
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

    public PairNode remove(String key) {
        return childNodes.remove(key);
    }

    public void clear() {
        childNodes.clear();
    }

    @Override
    public void set(String name, TreeNode value) {
        String[] split = name.split("\\.", 2);
        String head = split[0];

        if (split.length == 2) {
            MutablePairNode mutablePairNode;
            if (childNodes.containsKey(head)) {
                PairNode pairNode = childNodes.get(head);
                assertMutable(pairNode);
                mutablePairNode = (MutablePairNode) pairNode;
            } else {
                mutablePairNode = new MutablePairNode();
            }
            mutablePairNode.set(name, value);
            childNodes.put(head, mutablePairNode);
        } else {
            childNodes.put(head, new MutablePairNode(head, value));
        }
    }
}
