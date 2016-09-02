/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.MutableMapNode;
import org.seedstack.coffig.spi.ConfigurationMapper;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ProgrammaticProvider implements ConfigurationProvider {
    private final ConfigurationMapper mapper;
    private final Map<Supplier<Object>, String> suppliers = new HashMap<>();
    private volatile boolean dirty = true;

    public ProgrammaticProvider(ConfigurationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public MapNode provide() {
        MapNode mapNode = (MapNode) new MapNode()
                .merge(suppliers.keySet().stream()
                        .map(this::retrieveTreeNode)
                        .reduce(TreeNode::merge)
                        .orElse(new MapNode())
                );
        dirty = false;
        return mapNode;
    }

    @Override
    public ConfigurationProvider fork() {
        ProgrammaticProvider fork = new ProgrammaticProvider(mapper);
        fork.suppliers.putAll(suppliers);
        return fork;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    public void addObject(Object object) {
        Arrays.stream(object.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Config.class))
                .forEach(method -> addSupplier(() -> {
                    method.setAccessible(true);
                    try {
                        return method.invoke(object);
                    } catch (Exception e) {
                        throw new ConfigurationException("Cannot supply configuration object from " + method.toString());
                    }
                }, Coffig.resolvePath(method)));
    }

    public void addSupplier(Supplier<Object> supplier) {
        suppliers.put(supplier, null);
        dirty = true;
    }

    public void addSupplier(Supplier<Object> supplier, String prefix) {
        suppliers.put(supplier, prefix);
        dirty = true;
    }

    private TreeNode retrieveTreeNode(Supplier<Object> supplier) {
        Object o = supplier.get();
        TreeNode treeNode = mapper.unmap(o, o.getClass());
        String prefix = suppliers.get(supplier);
        if (prefix == null || prefix.isEmpty()) {
            prefix = Coffig.resolvePath(o.getClass());
        }
        if (prefix != null && !prefix.isEmpty()) {
            MutableMapNode mapNode = new MutableMapNode();
            mapNode.set(prefix, treeNode);
            return mapNode;
        } else {
            return treeNode;
        }
    }
}
