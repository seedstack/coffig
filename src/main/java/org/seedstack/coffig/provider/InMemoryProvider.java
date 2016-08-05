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
        MutableMapNode tree = new MutableMapNode();
        data.entrySet().forEach(entry -> tree.set(entry.getKey(), new ValueNode(entry.getValue())));
        dirty = false;
        return tree;
    }

    public String put(String key, String value) {
        String result = data.put(key, value);
        dirty = true;
        return result;
    }

    public String remove(String key) {
        String result = data.remove(key);
        dirty = true;
        return result;
    }

    public void putAll(Map<? extends String, ? extends String> m) {
        data.putAll(m);
        dirty = true;
    }

    public void clear() {
        data.clear();
        dirty = true;
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
