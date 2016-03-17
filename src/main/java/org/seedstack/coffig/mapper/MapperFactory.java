/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.MapNode;
import org.seedstack.coffig.TreeNode;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MapperFactory {

    private static volatile MapperFactory mapperFactory;
    private List<ConfigurationMapper> configurationMappers = new ArrayList<>();

    public MapperFactory() {
        configurationMappers.add(new ValueConfigurationMapper());
        configurationMappers.add(new ArrayConfigurationMapper());
        configurationMappers.add(new MapConfigurationMapper());
    }

    public static MapperFactory getInstance() {
        if (mapperFactory == null) {
            synchronized (MapperFactory.class) {
                mapperFactory = new MapperFactory();
            }
        }
        return mapperFactory;
    }

    public Object map(TreeNode treeNode, Type type) {
        Class<?> classToMap = typeToClass(type);
        for (ConfigurationMapper configurationMapper : configurationMappers) {
            if (configurationMapper.canHandle(classToMap)) {
                return configurationMapper.map(treeNode, type);
            }
        }
        return new ObjectMapper<>(classToMap).map((MapNode) treeNode);
    }

    private Class<?> typeToClass(Type type) {
        Class<?> classToMap;
        if (type instanceof Class) {
            classToMap = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            classToMap = (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            throw new ConfigurationException("Unsupported type " + type);
        }
        return classToMap;
    }
}
