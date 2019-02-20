/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import java.util.Objects;
import org.seedstack.coffig.TreeNode;

public class NamedNode {
    private final String name;
    private final TreeNode value;

    public NamedNode(String name, TreeNode value) {
        this.name = name;
        this.value = value;
    }

    public NamedNode(String name, String value) {
        this(name, new ValueNode(value));
    }

    public NamedNode(String name, String... values) {
        this(name, new ArrayNode(values));
    }

    public String name() {
        return name;
    }

    public TreeNode node() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NamedNode namedNode = (NamedNode) o;
        return Objects.equals(name, namedNode.name)
                && Objects.equals(value, namedNode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}
