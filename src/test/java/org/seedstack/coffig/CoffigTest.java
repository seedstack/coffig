/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.coffig.fixture.EnumFixture;
import org.seedstack.coffig.node.ArrayNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.provider.EmptyProvider;
import org.seedstack.coffig.spi.ConfigurationProvider;

public class CoffigTest {

    private final ConfigurationProvider appConfigProvider = () -> new MapNode(
            new NamedNode("id", "foo"),
            new NamedNode("name", "The Foo app"),
            new NamedNode("someEnum", "FOO"));

    private final ConfigurationProvider usersConfigProvider = () -> new MapNode(
            new NamedNode("id", "bar"),
            new NamedNode("users", new ArrayNode("u123456", "u456789")),
            new NamedNode("elements", new MapNode(new NamedNode("key1", "val1"), new NamedNode("key2", "val2"))),
            new NamedNode("items", "one"));

    private static class App {
        String id;
        String name;
        EnumFixture enumFixture;
        String[] users;
        String[] items;
        String[] elements;
    }

    @Test
    public void testConfigurationNotNull() {
        Coffig coffig = Coffig.builder().withProviders(new EmptyProvider()).build();
        Assertions.assertThat(coffig.get(App.class)).isNotNull();
    }

    @Test
    public void testWithSimpleProvider() {
        Coffig coffig = Coffig.builder().withProviders(appConfigProvider).build();
        App app = coffig.get(App.class);
        Assertions.assertThat(app.id).isEqualTo("foo");
        Assertions.assertThat(app.name).isEqualTo("The Foo app");
    }

    @Test
    public void testWithMergedConfiguration() {
        Coffig coffig = Coffig.builder().withProviders(appConfigProvider, usersConfigProvider).build();
        App app = coffig.get(App.class);

        Assertions.assertThat(app.id).isEqualTo("bar");
        Assertions.assertThat(app.name).isEqualTo("The Foo app");

        Assertions.assertThat(app.users).hasSize(2);
        Assertions.assertThat(app.users[0]).isEqualTo("u123456");
        Assertions.assertThat(app.users[1]).isEqualTo("u456789");
    }

    @Test
    public void testGetWithPath() throws Exception {
        Coffig coffig = Coffig.builder().withProviders(() -> new MapNode(
                new NamedNode("app",
                        new MapNode(new NamedNode("server",
                                new MapNode(new NamedNode("port", "8080"))))
                )
        )).build();
        Integer appServerPort = coffig.get(Integer.class, "app.server.port");
        Assertions.assertThat(appServerPort).isEqualTo(8080);
    }

    @Test
    public void testGetOptionalWithPathAndDefaultValue() throws Exception {
        Coffig coffig = Coffig.builder().withProviders(appConfigProvider).build();
        Assertions.assertThat(coffig.getOptional(String.class, "unknown").orElse("defaultValue")).isEqualTo("defaultValue");
    }

    @Test
    public void testGetWithPathAndDefaultValue() throws Exception {
        Coffig coffig = Coffig.builder().withProviders(appConfigProvider).build();
        Assertions.assertThat(coffig.get(String.class, "unknown")).isEqualTo("");
    }

    @Test
    public void testGetSingleValueAsArray() throws Exception {
        Coffig coffig = Coffig.builder().withProviders(usersConfigProvider).build();
        Assertions.assertThat(coffig.get(App.class).items).containsExactly("one");
    }

    @Test
    public void testGetMapAsArray() throws Exception {
        Coffig coffig = Coffig.builder().withProviders(usersConfigProvider).build();
        Assertions.assertThat(coffig.get(App.class).elements).containsOnly("val1", "val2");
    }
}
