/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

public class UnmodifiableNodeAttributes implements NodeAttributes {
    private final NodeAttributes nodeAttributes;

    private UnmodifiableNodeAttributes(NodeAttributes nodeAttributes) {
        if (nodeAttributes == null) {
            throw new IllegalArgumentException("Null node attributes not allowed");
        }
        this.nodeAttributes = nodeAttributes;
    }

    @Override
    public String get(String name) {
        return nodeAttributes.get(name);
    }

    @Override
    public void set(String name, String value) {
        throw new UnsupportedOperationException("Attempt to alter an unmodifiable node attributes");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Attempt to alter an unmodifiable node attributes");
    }

    @Override
    public int hashCode() {
        return nodeAttributes.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return nodeAttributes.equals(obj);
    }

    @Override
    public String toString() {
        return nodeAttributes.toString();
    }

    public static NodeAttributes of(NodeAttributes nodeAttributes) {
        if (nodeAttributes instanceof UnmodifiableNodeAttributes) {
            return nodeAttributes;
        } else {
            return new UnmodifiableNodeAttributes(nodeAttributes);
        }
    }
}
