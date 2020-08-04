/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import java.util.Optional;
import java.util.stream.Stream;
import org.seedstack.coffig.node.NamedNode;

public interface TreeNode {

    boolean isHidden();

    void hide();

    Type type();

    String value();

    Stream<TreeNode> nodes();

    Stream<NamedNode> namedNodes();

    TreeNode node(String key);

    Optional<TreeNode> get(String path);

    Stream<TreeNode> walk();

    boolean isEmpty();

    TreeNode merge(TreeNode otherNode);

    TreeNode set(String path, TreeNode value);

    TreeNode remove(String path);

    default TreeNode move(String sourcePath, String destinationPath) {
        this.set(destinationPath, this.remove(sourcePath));
        return this;
    }

    enum Type {
        MAP_NODE,
        ARRAY_NODE,
        VALUE_NODE
    }
}
