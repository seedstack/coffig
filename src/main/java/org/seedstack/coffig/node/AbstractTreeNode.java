/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.seedstack.coffig.TreeNode;

abstract class AbstractTreeNode implements TreeNode {
    static String HIDDEN_PLACEHOLDER = "***";
    private boolean hidden = false;

    @Override
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

    String quote(String s) {
        return s == null ? null : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
