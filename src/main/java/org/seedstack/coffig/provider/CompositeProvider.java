/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class CompositeProvider implements ConfigurationProvider {
    private final List<ConfigurationProvider> providers = new CopyOnWriteArrayList<>();
    private volatile boolean dirty = true;

    public CompositeProvider(ConfigurationProvider... configurationProviders) {
        providers.addAll(Arrays.asList(configurationProviders));
    }

    @Override
    public MapNode provide() {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        try {
            return forkJoinPool.submit(() -> providers.parallelStream()
                    .map(ConfigurationProvider::provide)
                    .reduce((conf1, conf2) -> (MapNode) conf1.merge(conf2))
                    .orElse(new MapNode())
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ConfigurationException(e);
        } finally {
            forkJoinPool.shutdown();
        }
    }

    @Override
    public boolean isDirty() {
        return dirty || providers.stream().filter(ConfigurationProvider::isDirty).count() > 0;
    }

    @Override
    public ConfigurationProvider fork() {
        CompositeProvider fork = new CompositeProvider();
        providers.stream().map(ConfigurationProvider::fork).forEachOrdered(fork.providers::add);
        return fork;
    }

    public void clear() {
        try {
            providers.clear();
        } finally {
            dirty = true;
        }
    }

    public void add(int index, ConfigurationProvider configurationProvider) {
        try {
            providers.add(index, configurationProvider);
        } finally {
            dirty = true;
        }
    }

    public void add(ConfigurationProvider configurationProvider) {
        try {
            providers.add(configurationProvider);
        } finally {
            dirty = true;
        }
    }

    public void remove(int index) {
        try {
            providers.remove(index);
        } finally {
            dirty = true;
        }
    }

    public void remove(ConfigurationProvider configurationProvider) {
        try {
            providers.remove(configurationProvider);
        } finally {
            dirty = true;
        }
    }
}
