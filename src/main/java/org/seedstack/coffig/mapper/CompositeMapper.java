/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.spi.ConfigurationMapper;
import org.seedstack.coffig.utils.AbstractComposite;

import java.lang.reflect.Type;

import static org.seedstack.coffig.utils.Utils.getRawClass;

public class CompositeMapper extends AbstractComposite<ConfigurationMapper> implements ConfigurationMapper {
    public CompositeMapper(ConfigurationMapper... items) {
        super(items);
    }

    @Override
    protected CompositeMapper doFork() {
        return new CompositeMapper();
    }

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

        ObjectMapper<?> objectMapper = new ObjectMapper<>(getRawClass(type));
        objectMapper.initialize(coffig);
        return objectMapper.map(treeNode);
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

        ObjectMapper<Object> objectMapper = new ObjectMapper<>(object);
        objectMapper.initialize(coffig);
        return objectMapper.unmap().freeze();
    }
}
