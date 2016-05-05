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

    @Override
    public MapNode provide() {
        MapperFactory mapperFactory = MapperFactory.getInstance();
        return (MapNode) new MapNode()
                .merge(suppliers.stream()
                        .map(Supplier::get)
                        .map(mapperFactory::unmap)
                        .reduce(TreeNode::merge)
                        .orElse(new MapNode())
                );
    }

    public void addObject(Object object) {
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
    }

    public void addSupplier(Supplier<Object> supplier) {
        suppliers.add(supplier);
    }
}
