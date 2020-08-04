/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import java.lang.reflect.Type;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.spi.ConfigurationEvaluator;
import org.seedstack.coffig.spi.ConfigurationMapper;

public class EvaluatingMapper implements ConfigurationMapper {
    private final ConfigurationMapper mapper;
    private final ConfigurationEvaluator evaluator;
    private Coffig coffig;

    public EvaluatingMapper(ConfigurationMapper mapper, ConfigurationEvaluator evaluator) {
        if (mapper == null) {
            throw new NullPointerException("Mapper cannot be null");
        }
        this.mapper = mapper;
        if (evaluator == null) {
            throw new NullPointerException("Evaluator cannot be null");
        }
        this.evaluator = evaluator;
    }

    @Override
    public void initialize(Coffig coffig) {
        this.coffig = coffig;
        mapper.initialize(coffig);
        evaluator.initialize(coffig);
    }

    @Override
    public void invalidate() {
        mapper.invalidate();
        evaluator.invalidate();
    }

    @Override
    public boolean isDirty() {
        return mapper.isDirty() || evaluator.isDirty();
    }

    @Override
    public EvaluatingMapper fork() {
        ConfigurationMapper forkedMapper = (ConfigurationMapper) mapper.fork();
        ConfigurationEvaluator forkedEvaluator = (ConfigurationEvaluator) evaluator.fork();
        return new EvaluatingMapper(forkedMapper, forkedEvaluator);
    }

    @Override
    public boolean canHandle(Type type) {
        return mapper.canHandle(type);
    }

    @Override
    public Object map(TreeNode treeNode, Type type) {
        return mapper.map(evaluator.evaluate(coffig.getTree(), treeNode), type);
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
