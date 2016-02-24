/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.coffig.data.MapNode;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledConfigurationTest {

    private Coffig underTest = new Coffig();

    int initialDelay = 0;
    int delay = 5;

    @Mocked
    private ScheduledExecutorService executorService;

    @Before
    public void setUp() throws Exception {
        new MockUp<Executors>() {
            @Mock
            ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
                return executorService;
            }
        };
    }

    @Test
    public void testSchedule() throws InterruptedException {
        new Expectations() {{
            executorService.scheduleWithFixedDelay(withAny((Runnable) () -> {}), initialDelay, delay, TimeUnit.MILLISECONDS);
        }};
        underTest.addProvider(MapNode::new);
        underTest.schedule(initialDelay, delay);
    }

    @Test
    public void testReSchedule() throws InterruptedException {
        new Expectations() {{
            executorService.scheduleWithFixedDelay(withAny((Runnable) () -> {}), initialDelay, delay, TimeUnit.MILLISECONDS);
            times = 2;
        }};
        underTest.schedule(initialDelay, delay);
        underTest.schedule(initialDelay, delay);
    }

    @Test
    public void shutdown() throws Exception {
        underTest.schedule(initialDelay, delay);
        new Expectations() {{
            executorService.isTerminated(); result = true;
        }};
        underTest.shutdown();
    }

    @Test
    public void shutdownNotTerminated() throws Exception {
        underTest.schedule(initialDelay, delay);
        new Expectations() {{
            executorService.isTerminated(); result = false;
            executorService.shutdownNow(); times = 1;
        }};
        underTest.shutdown();
    }

    @Test
    public void shutdownWithoutSchedule() throws Exception {
        new Expectations() {{
            executorService.isTerminated(); times = 0;
        }};
        underTest.shutdown();
    }
}
