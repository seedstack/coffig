/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.util.Optional;
import java.util.stream.Stream;

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

    String toMappedString(ConfigurationMapper mapper);

    default String safeValue() {
        try {
            return value();
        } catch (Exception e) {
            return formatNodeError(e);
        }
    }

    default TreeNode move(String sourcePath, String destinationPath) {
        this.set(destinationPath, this.remove(sourcePath));
        return this;
    }

    static String formatNodeError(String err) {
        return String.format("<!! %s !!>", err);
    }

    static String formatNodeError(Exception exc) {
        return String.format("<!! %s !!>", exc.getMessage());
    }

    enum Type {
        MAP_NODE,
        ARRAY_NODE,
        VALUE_NODE
    }
}
