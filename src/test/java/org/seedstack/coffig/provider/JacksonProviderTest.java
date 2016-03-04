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
import org.seedstack.coffig.MapNode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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

        InputStream jsonInputStream = new ByteArrayInputStream(("{\"app\":\"foo\", " +
                "\"security\": {\"users\": [\"user1\", \"user2\"]}}").getBytes());
        jacksonProvider.addSource(jsonInputStream);

        String YAMLConfig = "app: bar\n" +
                "jdbc:\n" +
                "  dataSources:\n" +
                "    -\n" +
                "        name: \"myDS\"\n" +
                "        driver: \"org.hsqldb.jdbcDriver\"\n" +
                "        url: \"jdbc:hsqldb:hsql://localhost:9001/DS\"";
        InputStream yamlInputStream = new ByteArrayInputStream(YAMLConfig.getBytes());
        jacksonProvider.addSource(yamlInputStream);
    }

    @Test
    public void testProvideJSON() throws Exception {
        MapNode mapNode = jacksonProvider.provide();

        Assertions.assertThat(mapNode).isNotNull();
        Assertions.assertThat(mapNode.value("app").value()).isEqualTo("bar");
        Assertions.assertThat(mapNode.get("security.users.1").value()).isEqualTo("user2");
    }

    @Test
    public void testProvideYAML() throws Exception {
        MapNode mapNode = jacksonProvider.provide();

        Assertions.assertThat(mapNode).isNotNull();
        Assertions.assertThat(mapNode.get("jdbc.dataSources.0.name").value()).isEqualTo("myDS");
    }
}
