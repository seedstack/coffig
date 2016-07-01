/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.MutableNodeAttributes;
import org.seedstack.coffig.MutableTreeNode;
import org.seedstack.coffig.TreeNode;

public class MutableValueNode extends ValueNode implements MutableTreeNode {
    public MutableValueNode(String value) {
        super(value);
    }

    public MutableValueNode() {
        super(null);
    }

    @Override
    public MutableNodeAttributes attributes() {
        return (MutableNodeAttributes) super.attributes();
    }

    public void value(String value) {
        this.value = value;
    }

    @Override
    public MutableTreeNode set(String path, TreeNode value) {
        throw new ConfigurationException("Illegal attempt to set inner path " + path);
    }

    @Override
    public MutableTreeNode remove(String path) {
        throw new ConfigurationException("Illegal attempt to remove inner path " + path);
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ValueNode freeze() {
        return new ValueNode(value);
    }

    @Override
    public MutableValueNode unfreeze() {
        return this;
    }
}
