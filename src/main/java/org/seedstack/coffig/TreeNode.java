/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface TreeNode {

    NodeAttributes attributes();

    Set<String> keys();

    String value();

    TreeNode item(String key);

    Collection<TreeNode> items();

    Optional<TreeNode> get(String path);

    Stream<TreeNode> stream();

    TreeNode merge(TreeNode otherNode);

    TreeNode freeze();

    MutableTreeNode unfreeze();

    default String indent(String s) {
        return Arrays.stream(s.split("\n")).map(line -> "  " + line).collect(Collectors.joining("\n"));
    }

}
