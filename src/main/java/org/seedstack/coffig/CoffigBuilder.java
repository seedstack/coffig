/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import org.seedstack.coffig.evaluator.CompositeEvaluator;
import org.seedstack.coffig.mapper.CompositeMapper;
import org.seedstack.coffig.mapper.DefaultMapper;
import org.seedstack.coffig.mapper.EvaluatingMapper;
import org.seedstack.coffig.processor.CompositeProcessor;
import org.seedstack.coffig.provider.CompositeProvider;
import org.seedstack.coffig.spi.ConfigurationEvaluator;
import org.seedstack.coffig.spi.ConfigurationMapper;
import org.seedstack.coffig.spi.ConfigurationProcessor;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.util.Arrays;

public class CoffigBuilder {
    private final Coffig coffig;
    private final CompositeMapper compositeMapper;
    private final CompositeProvider compositeProvider;
    private final CompositeProcessor compositeProcessor;
    private final CompositeEvaluator compositeEvaluator;
    private boolean addDefaultMapper = true;

    CoffigBuilder(Coffig coffig) {
        this.coffig = coffig;
        this.compositeMapper = new CompositeMapper();
        this.compositeEvaluator = new CompositeEvaluator();
        this.compositeProvider = new CompositeProvider();
        this.compositeProcessor = new CompositeProcessor();

        coffig.setMapper(new EvaluatingMapper().setMapper(compositeMapper).setEvaluator(compositeEvaluator));
        coffig.setProvider(compositeProvider);
        coffig.setProcessor(compositeProcessor);
    }

    public CoffigBuilder withoutDefaultMapper() {
        addDefaultMapper = false;
        return this;
    }

    public CoffigBuilder withMappers(ConfigurationMapper... configurationMappers) {
        Arrays.stream(configurationMappers).forEachOrdered(compositeMapper::add);
        return this;
    }

    public CoffigBuilder withProviders(ConfigurationProvider... configurationProviders) {
        Arrays.stream(configurationProviders).forEachOrdered(compositeProvider::add);
        return this;
    }

    public CoffigBuilder withProcessors(ConfigurationProcessor... configurationProcessors) {
        Arrays.stream(configurationProcessors).forEachOrdered(compositeProcessor::add);
        return this;
    }

    public CoffigBuilder withEvaluators(ConfigurationEvaluator... configurationEvaluators) {
        Arrays.stream(configurationEvaluators).forEachOrdered(compositeEvaluator::add);
        return this;
    }

    public Coffig build() {
        if (addDefaultMapper) {
            compositeMapper.add(new DefaultMapper());
        }
        coffig.initialize();
        return coffig;
    }
}
