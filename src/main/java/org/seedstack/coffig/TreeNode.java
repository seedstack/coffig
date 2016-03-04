/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

public interface TreeNode {

    TreeNode get(String prefix);

    default boolean exist(String prefix) {
        try {
            get(prefix);
            return true;
        } catch (PropertyNotFoundException e) {
            return false;
        }
    }

    TreeNode value(String name);

    String value();

    TreeNode[] values();

    TreeNode merge(TreeNode otherNode);

    TreeNode freeze();

    MutableTreeNode unfreeze();
}
