/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data.mutable;

import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.data.TreeNode;

public interface MutableTreeNode extends TreeNode {

    default void assertMutable(TreeNode treeNode) {
        if (!MutableTreeNode.class.isAssignableFrom(treeNode.getClass())) {
            throw new ConfigurationException(ConfigurationException.ILLEGAL_MUTATION.apply(treeNode));
        }
    }

    void set(String prefix, TreeNode value);

    void remove(String prefix);

    boolean isEmpty();
}
