/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.processor;

import org.seedstack.coffig.node.MutableMapNode;
import org.seedstack.coffig.spi.ConfigurationProcessor;
import org.seedstack.coffig.utils.AbstractComposite;

public class CompositeProcessor extends AbstractComposite<ConfigurationProcessor> implements ConfigurationProcessor {
    public CompositeProcessor(ConfigurationProcessor... items) {
        super(items);
    }

    @Override
    protected CompositeProcessor doFork() {
        return new CompositeProcessor();
    }

    @Override
    public void process(MutableMapNode configuration) {
        for (ConfigurationProcessor processor : items.values()) {
            processor.process(configuration);
        }
        dirty = false;
    }
}
