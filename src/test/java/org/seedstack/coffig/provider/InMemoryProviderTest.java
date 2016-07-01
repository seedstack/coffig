/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import mockit.Deencapsulation;
import org.junit.Test;
import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.node.ValueNode;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryProviderTest {
    private InMemoryProvider inMemoryProvider = new InMemoryProvider();

    @Test
    public void testMapIsInitiallyEmpty() throws Exception {
        assertThat(getData(inMemoryProvider)).isEmpty();
        assertThat(inMemoryProvider.isDirty()).isTrue();
    }

    @Test
    public void testPutIsMakingProviderDirty() throws Exception {
        inMemoryProvider.provide();
        assertThat(inMemoryProvider.isDirty()).isFalse();
        inMemoryProvider.put("a", "b");
        assertThat(inMemoryProvider.isDirty()).isTrue();
    }

    @Test
    public void testRemoveIsMakingProviderDirty() throws Exception {
        inMemoryProvider.provide();
        assertThat(inMemoryProvider.isDirty()).isFalse();
        inMemoryProvider.remove("a");
        assertThat(inMemoryProvider.isDirty()).isTrue();
    }

    @Test
    public void testPutAllIsMakingProviderDirty() throws Exception {
        inMemoryProvider.provide();
        assertThat(inMemoryProvider.isDirty()).isFalse();
        inMemoryProvider.putAll(new HashMap<>());
        assertThat(inMemoryProvider.isDirty()).isTrue();
    }

    @Test
    public void testNestingWorksCorrectly() throws Exception {
        inMemoryProvider.put("a.b.c", "1");
        inMemoryProvider.put("a.b.d", "2");
        assertThat(inMemoryProvider.provide().get("a.b.c").get()).isEqualTo(new ValueNode("1"));
        assertThat(inMemoryProvider.provide().get("a.b.d").get()).isEqualTo(new ValueNode("2"));
    }

    @Test(expected = ConfigurationException.class)
    public void testCollidingKeysAreCausingException() throws Exception {
        inMemoryProvider.put("a", "1");
        inMemoryProvider.put("a.b.d", "2");
        inMemoryProvider.provide();
    }

    private Map<String, String> getData(InMemoryProvider inMemoryProvider) {
        return Deencapsulation.getField(inMemoryProvider, "data");
    }
}