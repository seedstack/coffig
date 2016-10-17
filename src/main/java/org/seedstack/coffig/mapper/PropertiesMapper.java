/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.MutableMapNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.Type;
import java.util.Properties;

public class PropertiesMapper implements ConfigurationMapper {
    @Override
    public boolean canHandle(Type type) {
        return type instanceof Class && type.equals(Properties.class);
    }

    @Override
    public Object map(TreeNode treeNode, Type type) {
        Properties properties = new Properties();
        if (treeNode instanceof MapNode) {
            ((MapNode) treeNode).keys().forEach(key -> properties.setProperty(key, treeNode.item(key).value()));
        } else {
            treeNode.items().forEach(item -> properties.setProperty(item.value(), null));
        }
        return properties;
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        MutableMapNode mapNode = new MutableMapNode();
        ((Properties) object).forEach((key, value) -> {
            if (key != null) {
                mapNode.put(String.valueOf(key), new ValueNode(String.valueOf(value)));
            }
        });
        return mapNode;
    }
}
