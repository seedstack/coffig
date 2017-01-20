/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import org.seedstack.coffig.node.ArrayNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class InMemoryProvider implements ConfigurationProvider {
    private final ConcurrentMap<String, Object> data = new ConcurrentHashMap<>();
    private final AtomicBoolean dirty = new AtomicBoolean(true);

    @Override
    @SuppressWarnings("unchecked")
    public MapNode provide() {
        MapNode tree = new MapNode();
        data.entrySet().forEach(entry -> {
            Object value = entry.getValue();
            if (value.getClass().isArray()) {
                tree.set(entry.getKey(), new ArrayNode((String[]) value));
            } else if (List.class.isAssignableFrom(value.getClass())) {
                tree.set(entry.getKey(), new ArrayNode(((List<String>) value)
                        .stream()
                        .map(ValueNode::new)
                        .collect(Collectors.toList()))
                );
            } else {
                tree.set(entry.getKey(), new ValueNode((String) value));
            }
        });
        dirty.set(false);
        return tree;
    }

    public InMemoryProvider put(String key, String value) {
        data.put(key, value);
        dirty.set(true);
        return this;
    }

    public InMemoryProvider put(String key, String... values) {
        data.put(key, values);
        dirty.set(true);
        return this;
    }

    public InMemoryProvider put(String key, Collection<String> values) {
        data.put(key, new ArrayList<>(values));
        dirty.set(true);
        return this;
    }

    public InMemoryProvider remove(String key) {
        Object result = data.remove(key);
        dirty.set(true);
        return this;
    }

    public InMemoryProvider putAll(Map<? extends String, ?> m) {
        data.putAll(m);
        dirty.set(true);
        return this;
    }

    public InMemoryProvider clear() {
        data.clear();
        dirty.set(true);
        return this;
    }

    @Override
    public boolean isDirty() {
        return dirty.get();
    }

    @Override
    public ConfigurationProvider fork() {
        InMemoryProvider inMemoryProvider = new InMemoryProvider();
        inMemoryProvider.data.putAll(data);
        return inMemoryProvider;
    }
}
