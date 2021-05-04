/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

public class URIMapper implements ConfigurationMapper {
    @Override
    public boolean canHandle(Type type) {
        return type instanceof Class && URI.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public Object map(TreeNode treeNode, Type type) {
        try {
            return new URI(treeNode.value());
        } catch (URISyntaxException e) {
            throw ConfigurationException.wrap(e, ConfigurationErrorCode.ILLEGAL_CONVERSION)
                    .put("value", treeNode.value())
                    .put("type", type.getTypeName());
        }
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        return new ValueNode(object.toString());
    }
}
