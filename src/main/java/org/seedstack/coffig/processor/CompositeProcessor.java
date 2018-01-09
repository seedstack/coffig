/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.processor;

import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.spi.BaseComposite;
import org.seedstack.coffig.spi.ConfigurationProcessor;

public class CompositeProcessor extends BaseComposite<ConfigurationProcessor> implements ConfigurationProcessor {
    public CompositeProcessor(ConfigurationProcessor... items) {
        super(ConfigurationProcessor.class, items);
    }

    @Override
    protected CompositeProcessor doFork(ConfigurationProcessor... items) {
        return new CompositeProcessor(items);
    }

    @Override
    public void process(MapNode configuration) {
        for (ConfigurationProcessor processor : items) {
            processor.process(configuration);
        }
    }
}
