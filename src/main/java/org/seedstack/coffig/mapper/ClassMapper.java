/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public class ClassMapper implements ConfigurationMapper {
    @Override
    public boolean canHandle(Type type) {
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return Class.class.isAssignableFrom(((Class<?>) rawType));
            }
        }
        return false;
    }

    @Override
    public Object map(TreeNode treeNode, Type type) {
        Type itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
        Class<?> aClass;
        try {
            aClass = Class.forName(treeNode.value());
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Unable to load class <" + treeNode.value() + ">");
        }

        if (itemType instanceof Class && ((Class<?>) itemType).isAssignableFrom(aClass)) {
            return aClass;
        } else if (itemType instanceof WildcardType && isSatisfyingBounds(aClass, (WildcardType) itemType)) {
            return aClass;
        } else {
            throw new ConfigurationException("Class<" + aClass.getCanonicalName() + "> is not assignable to Class<" + itemType.getTypeName() + ">");
        }
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        return new ValueNode(((Class<?>) object).getCanonicalName());
    }

    private boolean isSatisfyingBounds(Class<?> aClass, WildcardType wildcardType) {
        for (Type bound : wildcardType.getUpperBounds()) {
            if (!(getRawType(bound)).isAssignableFrom(aClass)) {
                return false;
            }
        }
        for (Type bound : wildcardType.getLowerBounds()) {
            if (!(aClass.isAssignableFrom(getRawType(bound)))) {
                return false;
            }
        }
        return true;
    }

    private Class<?> getRawType(Type type) {
        if (type instanceof Class) {
            return ((Class<?>) type);
        } else if (type instanceof ParameterizedType) {
            return getRawType(((ParameterizedType) type).getRawType());
        } else {
            throw new ConfigurationException("Cannot resolve raw type of " + type.getTypeName());
        }
    }
}
