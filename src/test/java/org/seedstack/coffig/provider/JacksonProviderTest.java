/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.provider;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.coffig.node.MapNode;

public class JacksonProviderTest {
    @Test
    public void testProvideWithoutSources() throws Exception {
        JacksonProvider jacksonProvider = new JacksonProvider();
        MapNode mapNode = jacksonProvider.provide();
        Assertions.assertThat(mapNode).isNotNull();
    }

    @Test
    public void testProvideJSON() throws Exception {
        JacksonProvider jacksonProvider = new JacksonProvider();
        jacksonProvider.addSource(JacksonProviderTest.class.getResource("/fixture.json"));
        MapNode mapNode = jacksonProvider.provide();

        Assertions.assertThat(mapNode).isNotNull();
        Assertions.assertThat(mapNode.get("null").get().value()).isNull();
        Assertions.assertThat(mapNode.get("empty").get().value()).isEmpty();
        Assertions.assertThat(mapNode.get("someInt").get().value()).isEqualTo("5");
        Assertions.assertThat(mapNode.node("app").value()).isEqualTo("foo");
        Assertions.assertThat(mapNode.get("security.users[1]").get().value()).isEqualTo("user2");
    }

    @Test
    public void testProvideYAML() throws Exception {
        JacksonProvider jacksonProvider = new JacksonProvider();
        jacksonProvider.addSource(JacksonProviderTest.class.getResource("/fixture.yaml"));
        MapNode mapNode = jacksonProvider.provide();

        Assertions.assertThat(mapNode).isNotNull();
        Assertions.assertThat(mapNode.get("null").get().value()).isNull();
        Assertions.assertThat(mapNode.get("empty").get().value()).isEmpty();
        Assertions.assertThat(mapNode.get("someInt").get().value()).isEqualTo("5");
        Assertions.assertThat(mapNode.node("app").value()).isEqualTo("bar");
        Assertions.assertThat(mapNode.get("jdbc.dataSources[0].name").get().value()).isEqualTo("myDS");
    }
}
