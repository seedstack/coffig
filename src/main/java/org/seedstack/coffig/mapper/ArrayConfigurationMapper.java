/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.ValueNode;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

class ArrayConfigurationMapper implements ConfigurationMapper {

    private ValueConfigurationMapper valueMapper = new ValueConfigurationMapper();

    @Override
    public boolean canHandle(Class<?> aClass) {
        return aClass.equals(List.class) || aClass.equals(Set.class) || aClass.equals(Collection.class) || aClass.isArray();
    }

    public Object map(TreeNode treeNode, Type type) {
        Object actualValue = null;
        TreeNode[] nodes = treeNode.values();
        if (isArrayOfValueNode(nodes)) {
            String[] values = Arrays.stream(nodes).map(TreeNode::value).toArray(String[]::new);
            actualValue = valueMapper.convertArray(values, type);
        } else {
            if (isArray(type)) {
                Class componentType = ((Class) type).getComponentType();
                actualValue = Arrays.stream(nodes).map(node -> MapperFactory.getInstance().map(node, componentType))
                        .toArray(s -> (Object[]) Array.newInstance(componentType, s));
            } else if (type instanceof ParameterizedType) {
                actualValue = mapArrayNodeToCollection((ParameterizedType) type, nodes);
            }
        }
        return actualValue;
    }

    private Collection mapArrayNodeToCollection(ParameterizedType parameterizedType, TreeNode[] nodes) {
        Class<?> typeArgument = (Class<?>) parameterizedType.getActualTypeArguments()[0];

        Stream<Object> nodeStream = Arrays.stream(nodes).map(node -> MapperFactory.getInstance().map(node, typeArgument));

        Type rawType = parameterizedType.getRawType();
        Collection actualValue = null;
        if (rawType.equals(List.class) || rawType.equals(Collection.class)) {
            actualValue = nodeStream.collect(toList());
        } else if (rawType.equals(Set.class)) {
            actualValue = nodeStream.collect(toSet());
        }
        return actualValue;
    }

    private boolean isArrayOfValueNode(TreeNode[] nodes) {
        return nodes.length > 0 && nodes[0] instanceof ValueNode;
    }

    private boolean isArray(Type type) {
        return type instanceof Class && ((Class) type).isArray();
    }
}
