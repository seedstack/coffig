/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import org.seedstack.coffig.data.MapNode;
import org.seedstack.coffig.data.TreeNode;
import org.seedstack.coffig.mapper.MapperFactory;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.util.List;
import java.util.concurrent.*;

public class Coffig {

    private final List<ConfigurationProvider> providers = new CopyOnWriteArrayList<>();
    private volatile MapNode configurationTree = new MapNode();
    private ScheduledExecutorService executorService;

    public void addProvider(ConfigurationProvider configurationProvider) {
        providers.add(configurationProvider);
    }

    public void schedule(int initialDelay, int delay) {
        if (executorService == null) {
            executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleWithFixedDelay(this::compute, initialDelay, delay, TimeUnit.MILLISECONDS);
        } else {
            shutdown();
            schedule(initialDelay, delay);
        }
    }

    public void compute() {
        if (providers.size() > 0) {
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            try {
                configurationTree = forkJoinPool.submit(() -> providers.parallelStream()
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
    }

    public <T> T get(Class<T> configurationClass) {
        return doGet(configurationTree, configurationClass);
    }

    @SuppressWarnings("unchecked")
    private <T> T doGet(TreeNode treeNode, Class<T> configurationClass) {
        return (T) MapperFactory.getInstance().map(treeNode, configurationClass);
    }

    public void shutdown() {
        if (executorService != null) {
            try {
                executorService.shutdown();
            } finally {
                if (!executorService.isTerminated()) {
                    executorService.shutdownNow();
                }
                executorService = null;
            }
        }
    }

    public <T> T get(String prefix, Class<T> configurationClass) {
        return doGet(configurationTree.search(prefix), configurationClass);
    }
}
