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
import org.seedstack.coffig.data.ArrayNode;
import org.seedstack.coffig.data.MapNode;
import org.seedstack.coffig.data.NamedNode;
import org.seedstack.coffig.spi.ConfigurationProvider;

public class CoffigTest {

    public final ConfigurationProvider appConfigProvider = () -> new MapNode(
                    new NamedNode("id", "foo"),
                    new NamedNode("name", "The Foo app"));

    public final ConfigurationProvider usersConfigProvider = () -> new MapNode(
            new NamedNode("id", "bar"),
            new NamedNode("users", new ArrayNode("u123456", "u456789")));

    public static class App {
        String id;
        String name;
        String[] users;
    }

    @Test
    public void testConfigurationNotNull() {
        Coffig coffig = new Coffig();
        coffig.compute();
        Assertions.assertThat(coffig.get(App.class)).isNotNull();
    }

    @Test
    public void testWithSimpleProvider() {
        Coffig coffig = new Coffig();
        coffig.addProvider(appConfigProvider);

        coffig.compute();
        App app = coffig.get(App.class);
        Assertions.assertThat(app.id).isEqualTo("foo");
        Assertions.assertThat(app.name).isEqualTo("The Foo app");
    }

    @Test
    public void testWithMergedConfiguration() {
        Coffig coffig = new Coffig();

        coffig.addProvider(appConfigProvider);
        coffig.addProvider(usersConfigProvider);

        coffig.compute();
        App app = coffig.get(App.class);

        Assertions.assertThat(app.id).isEqualTo("bar");
        Assertions.assertThat(app.name).isEqualTo("The Foo app");

        Assertions.assertThat(app.users).hasSize(2);
        Assertions.assertThat(app.users[0]).isEqualTo("u123456");
        Assertions.assertThat(app.users[1]).isEqualTo("u456789");
    }

    @Test
    public void testGetWithPrefix() throws Exception {
        Coffig coffig = new Coffig();
        coffig.addProvider(() -> new MapNode(
                new NamedNode("app",
                        new MapNode(new NamedNode("server",
                                new MapNode(new NamedNode("port", "8080")))))));
        coffig.compute();
        Integer appServerPort = coffig.get("app.server.port", Integer.class);
        Assertions.assertThat(appServerPort).isEqualTo(8080);
    }
}
