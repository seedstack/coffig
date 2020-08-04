/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
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

public class PropertiesProviderTest {
    private PropertiesProvider propertiesProvider;

    @Test
    public void testProvideWithoutSources() throws Exception {
        PropertiesProvider propertiesProvider = new PropertiesProvider();
        MapNode mapNode = propertiesProvider.provide();
        Assertions.assertThat(mapNode).isNotNull();
    }

    @Before
    public void setUp() throws Exception {
        propertiesProvider = new PropertiesProvider();
        propertiesProvider.addSource(PropertiesProviderTest.class.getResource("/fixture.properties"));
    }

    @Test
    public void testProvide() throws Exception {
        MapNode mapNode = propertiesProvider.provide();

        Assertions.assertThat(mapNode).isNotNull();
        Assertions.assertThat(mapNode.node("app").value()).isEqualTo("foo");
        Assertions.assertThat(mapNode.node("empty").value()).isEmpty();
        Assertions.assertThat(mapNode.get("test.property").get().value()).isEqualTo("testValue");
    }
}
