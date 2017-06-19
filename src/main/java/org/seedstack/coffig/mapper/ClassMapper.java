/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.seedstack.coffig.ConfigurationErrorCode;
import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import static org.seedstack.coffig.util.Utils.getRawClass;

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
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Cast is verified in canHandle() method")
    public Object map(TreeNode treeNode, Type type) {
        Type itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
        Class<?> aClass;
        try {
            aClass = Class.forName(treeNode.value());
        } catch (ClassNotFoundException e) {
            throw ConfigurationException.wrap(e, ConfigurationErrorCode.UNABLE_TO_LOAD_CLASS)
                    .put("class", treeNode.value());
        }

        if (itemType instanceof Class && ((Class<?>) itemType).isAssignableFrom(aClass)) {
            return aClass;
        } else if (itemType instanceof WildcardType && isSatisfyingBounds(aClass, (WildcardType) itemType)) {
            return aClass;
        } else {
            throw ConfigurationException.createNew(ConfigurationErrorCode.NON_ASSIGNABLE_CLASS)
                    .put("assigned", aClass.getName())
                    .put("assignee", itemType.getTypeName());
        }
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        return new ValueNode(((Class<?>) object).getCanonicalName());
    }

    private boolean isSatisfyingBounds(Class<?> aClass, WildcardType wildcardType) {
        for (Type bound : wildcardType.getUpperBounds()) {
            if (!(getRawClass(bound)).isAssignableFrom(aClass)) {
                return false;
            }
        }
        for (Type bound : wildcardType.getLowerBounds()) {
            if (!(aClass.isAssignableFrom(getRawClass(bound)))) {
                return false;
            }
        }
        return true;
    }
}
