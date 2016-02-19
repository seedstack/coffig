/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.configuration.mapper;

import org.seedstack.configuration.data.MapNode;
import org.seedstack.configuration.data.TreeNode;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

class MapMapper implements Mapper {

    private ValueMapper valueMapper = new ValueMapper();

    @Override
    public boolean canHandle(Class<?> aClass) {
        return aClass.equals(Map.class);
    }

    public Object map(TreeNode treeNode, Type type) {
        MapNode mapNode = (MapNode) treeNode;
        Object actualValue = null;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                // TODO Complex objects as key are not supported due to MapNode supporting only String as key
                Class<?> keyClass = (Class<?>)parameterizedType.getActualTypeArguments()[0];
                Class<?> valueClass = (Class<?>)parameterizedType.getActualTypeArguments()[1];
                actualValue = mapNode.keys().stream()
                        .collect(toMap(key -> valueMapper.convertObject(key, keyClass),
                                key -> MapperFactory.getInstance().map(treeNode.value(key), valueClass)));
            }
        }
        return actualValue;
    }
}
