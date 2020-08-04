/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import static org.seedstack.shed.reflect.Types.rawClassOf;

import java.lang.reflect.Type;
import java.util.Arrays;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.spi.BaseComposite;
import org.seedstack.coffig.spi.ConfigurationMapper;

public class CompositeMapper extends BaseComposite<ConfigurationMapper> implements ConfigurationMapper {
    private Coffig coffig;

    public CompositeMapper(ConfigurationMapper... items) {
        super(ConfigurationMapper.class, items);
    }

    @Override
    public void initialize(Coffig coffig) {
        super.initialize(coffig);
        this.coffig = coffig;
    }

    @Override
    protected CompositeMapper doFork(ConfigurationMapper... items) {
        return new CompositeMapper(items);
    }

    @Override
    public boolean canHandle(Type type) {
        return Arrays.stream(items).anyMatch(item -> item.canHandle(type));
    }

    public Object map(TreeNode treeNode, Type type) {
        if (treeNode == null || (treeNode.type() == TreeNode.Type.VALUE_NODE && treeNode.isEmpty())) {
            return null;
        }

        for (ConfigurationMapper configurationMapper : items) {
            if (configurationMapper.canHandle(type)) {
                return configurationMapper.map(treeNode, type);
            }
        }

        ObjectMapper<?> objectMapper = new ObjectMapper<>(rawClassOf(type));
        objectMapper.initialize(coffig);
        return objectMapper.map(treeNode);
    }

    public TreeNode unmap(Object object, Type type) {
        if (object == null) {
            return null;
        }

        for (ConfigurationMapper configurationMapper : items) {
            if (configurationMapper.canHandle(type)) {
                return configurationMapper.unmap(object, type);
            }
        }

        ObjectMapper<Object> objectMapper = new ObjectMapper<>(object);
        objectMapper.initialize(coffig);
        return objectMapper.unmap();
    }
}
