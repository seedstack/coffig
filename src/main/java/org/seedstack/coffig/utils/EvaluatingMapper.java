/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.utils;

import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationEvaluator;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.Type;

public class EvaluatingMapper implements ConfigurationMapper {
    private ConfigurationMapper mapper;
    private ConfigurationEvaluator evaluator;
    private TreeNode rootNode;

    public ConfigurationMapper getMapper() {
        return mapper;
    }

    public void setMapper(ConfigurationMapper mapper) {
        this.mapper = mapper;
    }

    public ConfigurationEvaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(ConfigurationEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public void setRootNode(TreeNode rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    public boolean canHandle(Type type) {
        return mapper.canHandle(type);
    }

    @Override
    public Object map(TreeNode treeNode, Type type) {
        if (mapper == null) {
            throw new ConfigurationException(String.format("Cannot map node to type %s: no mapper specified", type.getTypeName()));
        }
        if (evaluator != null && treeNode instanceof ValueNode) {
            treeNode = evaluator.evaluate(rootNode, ((ValueNode) treeNode));
        }
        return mapper.map(treeNode, type);
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        if (mapper == null) {
            throw new ConfigurationException(String.format("Cannot un-map node to type %s: no mapper specified", type.getTypeName()));
        }
        return mapper.unmap(object, type);
    }
}
