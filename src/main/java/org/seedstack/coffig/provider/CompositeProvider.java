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
import org.seedstack.coffig.utils.AbstractComposite;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class CompositeProvider extends AbstractComposite<ConfigurationProvider> implements ConfigurationProvider {
    public CompositeProvider(ConfigurationProvider... items) {
        super(items);
    }

    @Override
    public MapNode provide() {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        try {
            MapNode mapNode = forkJoinPool.submit(() -> items.values().parallelStream()
                    .map(ConfigurationProvider::provide)
                    .reduce((conf1, conf2) -> (MapNode) conf1.merge(conf2))
                    .orElse(new MapNode())
            ).get();
            dirty = false;
            return mapNode;
        } catch (InterruptedException | ExecutionException e) {
            throw new ConfigurationException(e);
        } finally {
            forkJoinPool.shutdown();
        }
    }

    @Override
    protected CompositeProvider doFork() {
        return new CompositeProvider();
    }
}
