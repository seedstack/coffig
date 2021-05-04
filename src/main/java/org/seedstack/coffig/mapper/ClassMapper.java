/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import static org.seedstack.shed.reflect.Types.rawClassOf;

public class ClassMapper implements ConfigurationMapper {
    @Override
    public boolean canHandle(Type type) {
        if (type.equals(Class.class)) {
            return true;
        }
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return Class.class.isAssignableFrom(((Class<?>) rawType));
            }
        }
        return false;
    }

    @Override
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Cast is verified in canHandle() method")
    public Object map(TreeNode treeNode, Type type) {
        if (type instanceof ParameterizedType) {
            Type itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
            Class<?> someClass = loadClass(treeNode);
            if (itemType instanceof Class && ((Class<?>) itemType).isAssignableFrom(someClass)) {
                return someClass;
            } else if (itemType instanceof WildcardType && isSatisfyingBounds(someClass, (WildcardType) itemType)) {
                return someClass;
            } else {
                throw ConfigurationException.createNew(ConfigurationErrorCode.NON_ASSIGNABLE_CLASS)
                        .put("assigned", someClass.getName())
                        .put("assignee", itemType.getTypeName());
            }
        } else {
            return loadClass(treeNode);
        }
    }

    private Class<?> loadClass(TreeNode treeNode) {
        Class<?> someClass;
        try {
            someClass = Class.forName(treeNode.value());
        } catch (ClassNotFoundException e) {
            throw ConfigurationException.wrap(e, ConfigurationErrorCode.UNABLE_TO_LOAD_CLASS)
                    .put("class", treeNode.value());
        }
        return someClass;
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        return new ValueNode(((Class<?>) object).getCanonicalName());
    }

    private boolean isSatisfyingBounds(Class<?> someClass, WildcardType wildcardType) {
        for (Type bound : wildcardType.getUpperBounds()) {
            if (!(rawClassOf(bound)).isAssignableFrom(someClass)) {
                return false;
            }
        }
        for (Type bound : wildcardType.getLowerBounds()) {
            if (!(someClass.isAssignableFrom(rawClassOf(bound)))) {
                return false;
            }
        }
        return true;
    }
}
