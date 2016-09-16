/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.evaluator;

import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationEvaluator;
import org.seedstack.coffig.util.AbstractComposite;

public class CompositeEvaluator extends AbstractComposite<ConfigurationEvaluator> implements ConfigurationEvaluator {
    public CompositeEvaluator(ConfigurationEvaluator... items) {
        super(ConfigurationEvaluator.class, items);
    }

    @Override
    protected ConfigurationEvaluator doFork(ConfigurationEvaluator... items) {
        return new CompositeEvaluator(items);
    }

    @Override
    public ValueNode evaluate(TreeNode rootNode, ValueNode valueNode) {
        ValueNode result = valueNode;
        for (ConfigurationEvaluator evaluator : items) {
            result = evaluator.evaluate(rootNode, result);
        }
        return result;
    }
}
