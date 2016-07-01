/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.MutableMapNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryProvider implements ConfigurationProvider {
    private final ConcurrentMap<String, String> data = new ConcurrentHashMap<>();
    private volatile boolean dirty = true;

    @Override
    public MapNode provide() {
        try {
            MutableMapNode tree = new MutableMapNode();
            data.entrySet().stream().forEach(entry -> tree.set(entry.getKey(), new ValueNode(entry.getValue())));
            return tree;
        } finally {
            dirty = false;
        }
    }

    public String put(String key, String value) {
        try {
            return data.put(key, value);
        } finally {
            dirty = true;
        }
    }

    public String remove(String key) {
        try {
            return data.remove(key);
        } finally {
            dirty = true;
        }
    }

    public void putAll(Map<? extends String, ? extends String> m) {
        try {
            data.putAll(m);
        } finally {
            dirty = true;
        }
    }

    public void clear() {
        try {
            data.clear();
        } finally {
            dirty = true;
        }
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public ConfigurationProvider fork() {
        InMemoryProvider inMemoryProvider = new InMemoryProvider();
        inMemoryProvider.data.putAll(data);
        return inMemoryProvider;
    }
}
