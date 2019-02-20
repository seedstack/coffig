/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.spi.ConfigurationComponent;
import org.seedstack.coffig.spi.ConfigurationProvider;

public class ProgrammaticProvider implements ConfigurationProvider, ConfigurationComponent {
    private final Map<Supplier<Object>, String> suppliers = new HashMap<>();
    private final AtomicBoolean dirty = new AtomicBoolean(true);
    private Coffig coffig;

    @Override
    public void initialize(Coffig coffig) {
        this.coffig = coffig;
    }

    @Override
    public boolean isDirty() {
        return dirty.get();
    }

    @Override
    public ConfigurationProvider fork() {
        ProgrammaticProvider fork = new ProgrammaticProvider();
        fork.suppliers.putAll(suppliers);
        return fork;
    }

    @Override
    public MapNode provide() {
        MapNode mapNode = (MapNode) new MapNode()
                .merge(suppliers.keySet().stream()
                        .map(this::retrieveTreeNode)
                        .reduce(TreeNode::merge)
                        .orElse(new MapNode())
                );
        dirty.set(false);
        return mapNode;
    }

    public void addObject(Object object) {
        Arrays.stream(object.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Config.class))
                .forEach(method -> addSupplier(() -> {
                    method.setAccessible(true);
                    try {
                        return method.invoke(object);
                    } catch (Exception e) {
                        throw ConfigurationException.wrap(e, ConfigurationErrorCode.CANNOT_SUPPLY_CONFIGURATION_OBJECT)
                                .put("class", object.getClass())
                                .put("method", method.getName());
                    }
                }, Coffig.pathOf(method)));
    }

    public void addSupplier(Supplier<Object> supplier) {
        suppliers.put(supplier, null);
        dirty.set(true);
    }

    public void addSupplier(Supplier<Object> supplier, String prefix) {
        suppliers.put(supplier, prefix);
        dirty.set(true);
    }

    private TreeNode retrieveTreeNode(Supplier<Object> supplier) {
        Object o = supplier.get();
        TreeNode treeNode = coffig.getMapper().unmap(o, o.getClass());
        String prefix = suppliers.get(supplier);
        if (prefix == null || prefix.isEmpty()) {
            prefix = Coffig.pathOf(o.getClass());
        }
        if (prefix != null && !prefix.isEmpty()) {
            MapNode mapNode = new MapNode();
            mapNode.set(prefix, treeNode);
            return mapNode;
        } else {
            return treeNode;
        }
    }
}
