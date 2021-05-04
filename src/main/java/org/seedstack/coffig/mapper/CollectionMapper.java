/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.seedstack.shed.reflect.Classes.instantiateDefault;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ArrayNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

public class CollectionMapper implements ConfigurationMapper {
    private Coffig coffig;

    @Override
    public void initialize(Coffig coffig) {
        this.coffig = coffig;
    }

    @Override
    public boolean canHandle(Type type) {
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return Collection.class.isAssignableFrom(((Class<?>) rawType));
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Cast is verified in canHandle() method")
    public Object map(TreeNode treeNode, Type type) {
        Class<?> rawClass = (Class<?>) ((ParameterizedType) type).getRawType();
        Type itemType = ((ParameterizedType) type).getActualTypeArguments()[0];

        if (List.class.isAssignableFrom(rawClass)) {
            return treeNode.nodes().map(childNode -> coffig.getMapper().map(childNode, itemType)).collect(toList());
        } else if (Set.class.isAssignableFrom(rawClass)) {
            return treeNode.nodes().map(childNode -> coffig.getMapper().map(childNode, itemType)).collect(toSet());
        } else {
            return treeNode.nodes().map(childNode -> coffig.getMapper().map(childNode, itemType))
                    .collect(toCollection(() -> (Collection<Object>) instantiateDefault(rawClass)));
        }
    }

    @Override
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Cast is verified in canHandle() method")
    public TreeNode unmap(Object object, Type type) {
        Type itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
        return new ArrayNode(((Collection<?>) object).stream()
                .map(item -> coffig.getMapper().unmap(item, itemType))
                .collect(toList()));
    }
}
