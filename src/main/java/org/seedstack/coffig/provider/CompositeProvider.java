/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.spi.BaseComposite;
import org.seedstack.coffig.spi.ConfigurationProvider;

public class CompositeProvider extends BaseComposite<ConfigurationProvider> implements ConfigurationProvider {
    public CompositeProvider(ConfigurationProvider... items) {
        super(ConfigurationProvider.class, items);
    }

    @Override
    protected CompositeProvider doFork(ConfigurationProvider... items) {
        return new CompositeProvider(items);
    }

    @Override
    public MapNode provide() {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        try {
            return forkJoinPool.submit(() -> Arrays.stream(items)
                    .parallel()
                    .map(ConfigurationProvider::provide)
                    .reduce((conf1, conf2) -> (MapNode) conf1.merge(conf2))
                    .orElse(new MapNode())
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw ConfigurationException.wrap(e, ConfigurationErrorCode.ERROR_OCCURRED_DURING_COMPOSITE_PROVIDE);
        } finally {
            forkJoinPool.shutdown();
        }
    }
}
