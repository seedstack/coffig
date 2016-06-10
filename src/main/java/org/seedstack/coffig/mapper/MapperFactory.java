/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MapperFactory {
    private List<ConfigurationMapper> configurationMappers = new CopyOnWriteArrayList<>();

    public MapperFactory() {
        addMapper(new EnumConfigurationMapper());
        addMapper(new ValueConfigurationMapper());
        addMapper(new ArrayConfigurationMapper(this));
        addMapper(new CollectionConfigurationMapper(this));
        addMapper(new MapConfigurationMapper(this));
    }

    public void addMapper(ConfigurationMapper configurationMapper) {
        configurationMappers.add(configurationMapper);
    }

    public Object map(TreeNode treeNode, Type type) {
        if (treeNode == null) {
            return null;
        }

        for (ConfigurationMapper configurationMapper : configurationMappers) {
            if (configurationMapper.canHandle(type)) {
                return configurationMapper.map(treeNode, type);
            }
        }
        return new ObjectConfigurationMapper<>(this, (Class<?>) type).map((MapNode) treeNode);
    }

    public TreeNode unmap(Object object, Type type) {
        if (object == null) {
            return null;
        }

        for (ConfigurationMapper configurationMapper : configurationMappers) {
            if (configurationMapper.canHandle(type)) {
                return configurationMapper.unmap(object, type).freeze();
            }
        }
        return new ObjectConfigurationMapper<>(this, object).unmap().freeze();
    }
}
