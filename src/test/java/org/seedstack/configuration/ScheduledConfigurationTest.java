/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.configuration;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.seedstack.configuration.data.MapNode;
import org.seedstack.configuration.data.PairNode;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ScheduledConfigurationTest {

    private Coffig underTest = new Coffig();
    private final Random random = new SecureRandom("seed".getBytes());
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private CountDownLatch countDownLatch2 = new CountDownLatch(2);

    int initialDelay = 0;
    int delay = 5;

    public static class Counter {
        long count;
    }

    @Test
    public void testSchedule() throws InterruptedException {
        underTest.addProvider(() -> {
            countDownLatch.countDown();
            countDownLatch2.countDown();
            return new MapNode(new PairNode("count", String.valueOf(random.nextInt())));
        });
        underTest.schedule(initialDelay, delay);
        countDownLatch.await();
        TimeUnit.MILLISECONDS.sleep(20);
        Counter counter = underTest.get(Counter.class);
        countDownLatch2.await();
        TimeUnit.MILLISECONDS.sleep(20);
        Counter counter2 = underTest.get(Counter.class);

        Assertions.assertThat(counter).isNotNull();
        Assertions.assertThat(counter.count).isNotEqualTo(counter2.count);
    }

    @Test
    public void testReSchedule() throws InterruptedException {
        underTest.schedule(initialDelay, delay);
        underTest.schedule(initialDelay, delay);
    }

    @After
    public void tearDown() throws Exception {
        underTest.shutdown();
    }
}
