/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.configuration;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ToDeleteTest {

    int initialDelay = 0;
    int period = 10;
    final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    volatile long time;

    @Test @Ignore
    public void test() throws InterruptedException, ExecutionException {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        final RunnableFuture<Long> task = new FutureTask<>(System::nanoTime);
        Runnable taskReader = () -> {
            try {
                System.out.println(task.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        };

        executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(taskReader, initialDelay + 10, period, TimeUnit.MILLISECONDS);

        TimeUnit.MILLISECONDS.sleep(50);

    }
}
