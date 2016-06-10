/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

public interface MutableTreeNode extends TreeNode {

    MutableNodeAttributes attributes();

    MutableTreeNode set(String prefix, TreeNode value);

    MutableTreeNode remove(String prefix);

    default MutableTreeNode move(String source, String destination) {
        this.set(destination, this.remove(source));
        return this;
    }

    boolean isEmpty();
}
