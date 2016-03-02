/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data;

public interface TreeNode {

    TreeNode search(String prefix);

    TreeNode value(String name);

    String value();

    TreeNode[] values();

    TreeNode merge(TreeNode otherNode);
}
