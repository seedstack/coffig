/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.coffig.node.MapNode;

public class JacksonProviderTest {

    private JacksonProvider jacksonProvider;

    @Test
    public void testProvideWithoutSources() throws Exception {
        JacksonProvider jacksonProvider = new JacksonProvider();

        MapNode mapNode = jacksonProvider.provide();

        Assertions.assertThat(mapNode).isNotNull();
    }

    @Before
    public void setUp() throws Exception {
        jacksonProvider = new JacksonProvider();
        jacksonProvider.addSource(JacksonProviderTest.class.getResource("/fixture.json"));
        jacksonProvider.addSource(JacksonProviderTest.class.getResource("/fixture.yaml"));
    }

    @Test
    public void testProvideJSON() throws Exception {
        MapNode mapNode = jacksonProvider.provide();

        Assertions.assertThat(mapNode).isNotNull();
        Assertions.assertThat(mapNode.node("app").value()).isEqualTo("bar");
        Assertions.assertThat(mapNode.get("security.users[1]").get().value()).isEqualTo("user2");
    }

    @Test
    public void testProvideYAML() throws Exception {
        MapNode mapNode = jacksonProvider.provide();

        Assertions.assertThat(mapNode).isNotNull();
        Assertions.assertThat(mapNode.get("jdbc.dataSources[0].name").get().value()).isEqualTo("myDS");
    }
}
