/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.node.MutableArrayNode;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

class ArrayConfigurationMapper implements ConfigurationMapper {
    private final MapperFactory mapperFactory;

    ArrayConfigurationMapper(MapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
    }

    @Override
    public boolean canHandle(Type type) {
        return type instanceof Class && ((Class<?>) type).isArray();
    }

    @Override
    public Object map(TreeNode treeNode, Type type) {
        Class componentType = ((Class) type).getComponentType();
        TreeNode[] values = treeNode.items();
        Object array = Array.newInstance(componentType, values.length);
        AtomicInteger index = new AtomicInteger();
        Arrays.stream(values).map(childNode -> mapperFactory.map(childNode, componentType)).forEach(item -> Array.set(array, index.getAndIncrement(), item));
        return array;
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        MutableArrayNode mutableArrayNode = new MutableArrayNode();
        Class componentType = ((Class) type).getComponentType();
        int length = Array.getLength(object);
        for (int i = 0; i < length; i++) {
            TreeNode treeNode = mapperFactory.unmap(Array.get(object, i), componentType);
            if (treeNode != null) {
                mutableArrayNode.add(treeNode);
            }
        }
        return mutableArrayNode;
    }
}
