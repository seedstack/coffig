/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationEvaluator;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.Type;

public class EvaluatingMapper implements ConfigurationMapper {
    private final ConfigurationMapper mapper;
    private final ConfigurationEvaluator evaluator;
    private Coffig coffig;

    public EvaluatingMapper(ConfigurationMapper mapper, ConfigurationEvaluator evaluator) {
        this.mapper = mapper;
        this.evaluator = evaluator;
    }

    @Override
    public void initialize(Coffig coffig) {
        this.coffig = coffig;
        if (mapper != null) {
            mapper.initialize(coffig);
        }
        if (evaluator != null) {
            evaluator.initialize(coffig);
        }
    }

    @Override
    public void invalidate() {
        if (mapper != null) {
            mapper.invalidate();
        }
        if (evaluator != null) {
            evaluator.invalidate();
        }
    }

    @Override
    public boolean isDirty() {
        return mapper != null && mapper.isDirty() || evaluator != null && evaluator.isDirty();
    }

    @Override
    public EvaluatingMapper fork() {
        ConfigurationMapper forkedMapper = mapper == null ? null : (ConfigurationMapper) mapper.fork();
        ConfigurationEvaluator forkedEvaluator = evaluator == null ? null : (ConfigurationEvaluator) evaluator.fork();
        return new EvaluatingMapper(forkedMapper, forkedEvaluator);
    }

    @Override
    public boolean canHandle(Type type) {
        return mapper.canHandle(type);
    }

    @Override
    public Object map(TreeNode treeNode, Type type) {
        if (this.mapper == null) {
            throw new ConfigurationException(String.format("Cannot map tree node to %s: no mapper specified", type.getTypeName()));
        }
        if (evaluator != null && treeNode instanceof ValueNode) {
            treeNode = evaluator.evaluate(coffig.getTree(), ((ValueNode) treeNode));
        }
        return mapper.map(treeNode, type);
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        return mapper.unmap(object, type);
    }

    public ConfigurationMapper getMapper() {
        return mapper;
    }

    public ConfigurationEvaluator getEvaluator() {
        return evaluator;
    }
}
