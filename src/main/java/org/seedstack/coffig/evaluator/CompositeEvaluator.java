/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.evaluator;

import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.spi.BaseComposite;
import org.seedstack.coffig.spi.ConfigurationEvaluator;

public class CompositeEvaluator extends BaseComposite<ConfigurationEvaluator> implements ConfigurationEvaluator {
    public CompositeEvaluator(ConfigurationEvaluator... items) {
        super(ConfigurationEvaluator.class, items);
    }

    @Override
    protected ConfigurationEvaluator doFork(ConfigurationEvaluator... items) {
        return new CompositeEvaluator(items);
    }

    @Override
    public TreeNode evaluate(TreeNode rootNode, TreeNode valueNode) {
        TreeNode result = valueNode;
        for (ConfigurationEvaluator evaluator : items) {
            result = evaluator.evaluate(rootNode, result);
        }
        return result;
    }
}
