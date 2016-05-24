/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import org.seedstack.coffig.Config;
import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.MapNode;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.mapper.MapperFactory;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ProgrammaticProvider implements ConfigurationProvider {
    private List<Supplier<Object>> suppliers = new ArrayList<>();
    private volatile boolean dirty = false;

    @Override
    public MapNode provide() {
        try {
            MapperFactory mapperFactory = MapperFactory.getInstance();
            return (MapNode) new MapNode()
                    .merge(suppliers.stream()
                            .map(Supplier::get)
                            .map(mapperFactory::unmap)
                            .reduce(TreeNode::merge)
                            .orElse(new MapNode())
                    );
        } finally {
            dirty = false;
        }
    }

    @Override
    public ConfigurationProvider fork() {
        ProgrammaticProvider fork = new ProgrammaticProvider();
        fork.suppliers.addAll(suppliers);
        return fork;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    public void addObject(Object object) {
        try {
            Arrays.stream(object.getClass().getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Config.class))
                    .map(method -> (Supplier) () -> {
                        method.setAccessible(true);
                        try {
                            return method.invoke(object);
                        } catch (Exception e) {
                            throw new ConfigurationException("Cannot supply configuration object from " + method.toString());
                        }
                    })
                    .forEach(suppliers::add);
        } finally {
            dirty = true;
        }
    }

    public void addSupplier(Supplier<Object> supplier) {
        try {
            suppliers.add(supplier);
        } finally {
            dirty = true;
        }
    }
}
