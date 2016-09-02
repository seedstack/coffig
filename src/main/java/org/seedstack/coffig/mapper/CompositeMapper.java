/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.utils.AbstractComposite;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class CompositeMapper extends AbstractComposite<ConfigurationMapper> implements ConfigurationMapper {
    @Override
    public boolean canHandle(Type type) {
        return items.values().stream().anyMatch(item -> item.canHandle(type));
    }

    public Object map(TreeNode treeNode, Type type) {
        if (treeNode == null) {
            return null;
        }

        for (ConfigurationMapper configurationMapper : items.values()) {
            if (configurationMapper.canHandle(type)) {
                return configurationMapper.map(treeNode, type);
            }
        }

        return new ObjectConfigurationMapper<>(this, getRawClass(type)).map(treeNode);
    }

    public TreeNode unmap(Object object, Type type) {
        if (object == null) {
            return null;
        }

        for (ConfigurationMapper configurationMapper : items.values()) {
            if (configurationMapper.canHandle(type)) {
                return configurationMapper.unmap(object, type).freeze();
            }
        }

        return new ObjectConfigurationMapper<>(this, object).unmap().freeze();
    }

    @Override
    protected ConfigurationMapper doFork() {
        return new CompositeMapper();
    }

    public static Class<?> getRawClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            return (Class<?>) rawType;
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawClass(componentType), 0).getClass();
        } else if (type instanceof TypeVariable) {
            return Object.class;
        } else {
            throw new IllegalArgumentException("Unsupported type " + type.getTypeName());
        }
    }
}
