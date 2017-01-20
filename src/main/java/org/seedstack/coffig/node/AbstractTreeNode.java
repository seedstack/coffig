/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import org.seedstack.coffig.NodeAttributes;
import org.seedstack.coffig.TreeNode;

import java.util.Arrays;
import java.util.stream.Collectors;

abstract class AbstractTreeNode implements TreeNode {
    static String HIDDEN_PLACEHOLDER = "***";

    private final NodeAttributesImpl attributes;
    private boolean hidden = false;

    AbstractTreeNode() {
        this.attributes = new NodeAttributesImpl();
    }

    AbstractTreeNode(AbstractTreeNode other) {
        this.attributes = new NodeAttributesImpl(other.attributes);
    }

    @Override
    public NodeAttributes attributes() {
        return attributes;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public void hide() {
        this.hidden = true;
    }

    String indent(String s) {
        return Arrays.stream(s.split("\n")).map(line -> "  " + line).collect(Collectors.joining("\n"));
    }
}
