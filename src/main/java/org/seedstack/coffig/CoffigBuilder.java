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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoffigBuilder {
    private final List<ConfigurationMapper> mappers = new ArrayList<>();
    private final List<ConfigurationProvider> providers = new ArrayList<>();
    private final List<ConfigurationProcessor> processors = new ArrayList<>();
    private final List<ConfigurationEvaluator> evaluators = new ArrayList<>();
    private boolean addDefaultMapper = true;

    CoffigBuilder() {
    }

    public CoffigBuilder withoutDefaultMapper() {
        addDefaultMapper = false;
        return this;
    }

    public CoffigBuilder withMappers(ConfigurationMapper... mappers) {
        this.mappers.addAll(Arrays.asList(mappers));
        return this;
    }

    public CoffigBuilder withProviders(ConfigurationProvider... providers) {
        this.providers.addAll(Arrays.asList(providers));
        return this;
    }

    public CoffigBuilder withProcessors(ConfigurationProcessor... processors) {
        this.processors.addAll(Arrays.asList(processors));
        return this;
    }

    public CoffigBuilder withEvaluators(ConfigurationEvaluator... evaluators) {
        this.evaluators.addAll(Arrays.asList(evaluators));
        return this;
    }

    public Coffig build() {
        if (addDefaultMapper) {
            mappers.add(0, new DefaultMapper());
        }

        return new Coffig(
                new EvaluatingMapper(
                        new CompositeMapper(mappers.toArray(new ConfigurationMapper[mappers.size()])),
                        new CompositeEvaluator(evaluators.toArray(new ConfigurationEvaluator[evaluators.size()]))
                ),
                new CompositeProvider(providers.toArray(new ConfigurationProvider[providers.size()])),
                new CompositeProcessor(processors.toArray(new ConfigurationProcessor[processors.size()]))
        );
    }
}
