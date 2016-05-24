/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import java.util.Objects;

public class ValueNode extends AbstractTreeNode {
    protected String value;

    public ValueNode(String value) {
        this.value = value;
    }

    @Override
    public TreeNode[] values() {
        return new TreeNode[]{new ValueNode(value)};
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueNode valueNode = (ValueNode) o;
        return Objects.equals(value, valueNode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public TreeNode freeze() {
        return this;
    }

    @Override
    public MutableTreeNode unfreeze() {
        return new MutableValueNode(value);
    }
}
