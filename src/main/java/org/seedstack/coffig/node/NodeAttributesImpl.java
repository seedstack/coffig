/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import org.seedstack.coffig.NodeAttributes;

import java.util.HashMap;
import java.util.Map;

class NodeAttributesImpl implements NodeAttributes {
    private final Map<String, String> attributes;

    NodeAttributesImpl() {
        this.attributes = new HashMap<>();
    }

    NodeAttributesImpl(NodeAttributesImpl other) {
        this.attributes = new HashMap<>(other.attributes);
    }

    @Override
    public String get(String name) {
        return attributes.get(name);
    }

    @Override
    public void set(String name, String value) {
        attributes.put(name, value);
    }

    @Override
    public void clear() {
        attributes.clear();
    }
}
