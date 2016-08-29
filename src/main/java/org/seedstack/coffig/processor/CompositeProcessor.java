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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CompositeProcessor implements ConfigurationProcessor {
    private final List<ConfigurationProcessor> processors = new CopyOnWriteArrayList<>();

    public CompositeProcessor(ConfigurationProcessor... configurationProcessors) {
        processors.addAll(Arrays.asList(configurationProcessors));
    }

    @Override
    public void process(MutableMapNode configuration) {
        for (ConfigurationProcessor processor : processors) {
            processor.process(configuration);
        }
    }

    @Override
    public ConfigurationProcessor fork() {
        CompositeProcessor fork = new CompositeProcessor();
        processors.stream().map(ConfigurationProcessor::fork).forEachOrdered(fork.processors::add);
        return fork;
    }

    public void clear() {
        processors.clear();
    }

    public void add(ConfigurationProcessor configurationProcessor) {
        processors.add(configurationProcessor);
    }

    public void add(int index, ConfigurationProcessor configurationProcessor) {
        processors.add(index, configurationProcessor);
    }

    public void remove(int index) {
        processors.remove(index);
    }

    public void remove(ConfigurationProcessor configurationProcessor) {
        processors.remove(configurationProcessor);
    }


}
