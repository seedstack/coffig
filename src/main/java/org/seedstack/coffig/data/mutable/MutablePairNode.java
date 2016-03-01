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
    public void set(String name, String value) {
        String[] split = name.split("\\.", 2);
        String head = split[0];
        setName(head);

        if (split.length == 2) {
        String tail = split[1];
        String[] splitTail = tail.split("\\.", 2);
        String newHead = splitTail[0];

            MutableTreeNode treeNode;

            if (this.value == null) {
                if (isArrayNode(newHead)) {
                    treeNode = new MutableArrayNode();
                } else {
                    treeNode = new MutableMapNode();
                }
            } else {
                // TODO handle exception
                treeNode = (MutableTreeNode) this.value;
            }
            treeNode.set(tail, value);
            this.value = (TreeNode) treeNode;
        } else {
            this.value = new MutableValueNode(value);
        }
    }

}
