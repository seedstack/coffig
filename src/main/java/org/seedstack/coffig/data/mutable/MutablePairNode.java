/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data.mutable;

import org.seedstack.coffig.data.PairNode;
import org.seedstack.coffig.data.TreeNode;

import java.util.Optional;

public class MutablePairNode extends PairNode implements MutableTreeNode {
    public MutablePairNode(String name, TreeNode value) {
        super(name, value);
    }

    public MutablePairNode(String name, String value) {
        super(name, value);
    }

    public MutablePairNode(String name, String... values) {
        super(name, values);
    }

    public MutablePairNode() {
        super(null, (TreeNode) null);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(TreeNode value) {
        this.value = value;
    }

    @Override
    public void set(String name, TreeNode value) {
        Prefix prefix = new Prefix(name);
        setName(prefix.head);

        if (prefix.tail.isPresent()) {
            TreeNode treeNode = getOrCreateTreeNode(prefix);
            ((MutableTreeNode) treeNode).set(prefix.tail.get(), value);
            this.value = treeNode;
        } else {
            this.value = value;
        }
    }

    private TreeNode getOrCreateTreeNode(Prefix prefix) {
        TreeNode treeNode;
        if (this.value == null) {
            treeNode = createTreeNode(prefix);
        } else {
            treeNode = this.value;
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
    public void remove(String prefix) {
        ((MutableTreeNode) this.value).remove(prefix);
    }

    @Override
    public boolean isEmpty() {
        return ((MutableTreeNode) this.value).isEmpty();
    }
}
