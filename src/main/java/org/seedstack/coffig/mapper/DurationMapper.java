/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import java.lang.reflect.Type;
import java.time.Duration;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

public class DurationMapper implements ConfigurationMapper {
    @Override
    public boolean canHandle(Type type) {
        return type instanceof Class && Duration.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public Object map(TreeNode treeNode, Type type) {
        return Duration.parse(treeNode.value());
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        return new ValueNode(object.toString());
    }
}
