/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.MutableMapNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationComponent;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.seedstack.coffig.utils.Utils.instantiateDefault;

public class MapConfigurationMapper implements ConfigurationMapper {
    private Coffig coffig;

    @Override
    public void initialize(Coffig coffig) {
        this.coffig = coffig;
    }

    @Override
    public ConfigurationComponent fork() {
        return new MapConfigurationMapper();
    }

    @Override
    public boolean canHandle(Type type) {
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return Map.class.isAssignableFrom(((Class<?>) rawType));
            }
        }
        return false;
    }

    @Override
    public Object map(TreeNode treeNode, Type type) {
        Type keyType = ((ParameterizedType) type).getActualTypeArguments()[0];
        Type valueType = ((ParameterizedType) type).getActualTypeArguments()[1];

        if (treeNode instanceof MapNode) {
            return ((MapNode) treeNode).keys().stream()
                    .collect(toMap(
                            key -> coffig.getMapper().map(new ValueNode(key), keyType),
                            key -> coffig.getMapper().map(treeNode.item(key), valueType)
                    ));
        } else {
            return Arrays.stream(treeNode.items())
                    .collect(toMap(
                            node -> coffig.getMapper().map(node, keyType),
                            node -> instantiateDefault((Class<?>) valueType))
                    );
        }
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        MutableMapNode mapNode = new MutableMapNode();
        Type valueType = ((ParameterizedType) type).getActualTypeArguments()[1];
        ((Map<?, ?>) object).forEach(((key, value) -> {
            if (key != null) {
                mapNode.put(String.valueOf(key), coffig.getMapper().unmap(value, valueType));
            }
        }));
        return mapNode;
    }
}
