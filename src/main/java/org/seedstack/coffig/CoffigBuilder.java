/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import org.seedstack.coffig.evaluator.CompositeEvaluator;
import org.seedstack.coffig.mapper.CompositeMapper;
import org.seedstack.coffig.mapper.EvaluatingMapper;
import org.seedstack.coffig.mapper.ValidatingMapper;
import org.seedstack.coffig.processor.CompositeProcessor;
import org.seedstack.coffig.provider.CompositeProvider;
import org.seedstack.coffig.spi.ConfigurationEvaluator;
import org.seedstack.coffig.spi.ConfigurationMapper;
import org.seedstack.coffig.spi.ConfigurationProcessor;
import org.seedstack.coffig.spi.ConfigurationProvider;
import org.seedstack.shed.ClassLoaders;

public class CoffigBuilder {
    private static final ClassLoader MOST_COMPLETE_CLASS_LOADER = ClassLoaders.findMostCompleteClassLoader
            (CoffigBuilder.class);
    private final List<ConfigurationMapper> mappers = new ArrayList<>();
    private final List<ConfigurationProvider> providers = new ArrayList<>();
    private final List<ConfigurationProcessor> processors = new ArrayList<>();
    private final List<ConfigurationEvaluator> evaluators = new ArrayList<>();
    private boolean detection = true;
    private boolean mapperDetection = true;
    private boolean processorDetection = true;
    private boolean evaluatorDetection = true;
    private boolean providerDetection = true;
    private Object validatorFactory;

    CoffigBuilder() {
    }

    public CoffigBuilder disableAllDetection() {
        detection = false;
        return this;
    }

    public CoffigBuilder disableMapperDetection() {
        mapperDetection = false;
        return this;
    }

    public CoffigBuilder disableProcessorDetection() {
        processorDetection = false;
        return this;
    }

    public CoffigBuilder disableEvaluatorDetection() {
        evaluatorDetection = false;
        return this;
    }

    public CoffigBuilder disableProviderDetection() {
        providerDetection = false;
        return this;
    }

    public CoffigBuilder enableValidation(Object validatorFactory) {
        this.validatorFactory = validatorFactory;
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
        if (detection) {
            if (mapperDetection) {
                mappers.addAll(loadMappers());
            }
            if (processorDetection) {
                processors.addAll(loadProcessors());
            }
            if (evaluatorDetection) {
                evaluators.addAll(loadEvaluators());
            }
            if (providerDetection) {
                providers.addAll(loadProviders());
            }
        }

        return new Coffig(
                wrap(new EvaluatingMapper(
                        new CompositeMapper(mappers.toArray(new ConfigurationMapper[mappers.size()])),
                        new CompositeEvaluator(evaluators.toArray(new ConfigurationEvaluator[evaluators.size()]))
                )),
                new CompositeProvider(providers.toArray(new ConfigurationProvider[providers.size()])),
                new CompositeProcessor(processors.toArray(new ConfigurationProcessor[processors.size()]))
        );
    }

    private ConfigurationMapper wrap(ConfigurationMapper mapper) {
        if (validatorFactory != null) {
            return new ValidatingMapper(mapper, validatorFactory);
        } else {
            return mapper;
        }
    }

    private Collection<? extends ConfigurationMapper> loadMappers() {
        Set<ConfigurationMapper> loadedMappers = new HashSet<>();
        ServiceLoader.load(ConfigurationMapper.class, MOST_COMPLETE_CLASS_LOADER)
                .iterator()
                .forEachRemaining(loadedMappers::add);
        return loadedMappers;
    }

    private Collection<? extends ConfigurationProcessor> loadProcessors() {
        Set<ConfigurationProcessor> loadedProcessors = new HashSet<>();
        ServiceLoader.load(ConfigurationProcessor.class, MOST_COMPLETE_CLASS_LOADER)
                .iterator()
                .forEachRemaining(loadedProcessors::add);
        return loadedProcessors;
    }

    private Collection<? extends ConfigurationEvaluator> loadEvaluators() {
        Set<ConfigurationEvaluator> loadedEvaluators = new HashSet<>();
        ServiceLoader.load(ConfigurationEvaluator.class, MOST_COMPLETE_CLASS_LOADER)
                .iterator()
                .forEachRemaining(loadedEvaluators::add);
        return loadedEvaluators;
    }

    private Collection<? extends ConfigurationProvider> loadProviders() {
        Set<ConfigurationProvider> loadedProviders = new HashSet<>();
        ServiceLoader.load(ConfigurationProvider.class, MOST_COMPLETE_CLASS_LOADER)
                .iterator()
                .forEachRemaining(loadedProviders::add);
        return loadedProviders;
    }
}
