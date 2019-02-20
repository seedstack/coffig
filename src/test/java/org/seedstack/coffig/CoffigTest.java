/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.seedstack.coffig.fixture.EnumFixture;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.node.ArrayNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.provider.VoidProvider;
import org.seedstack.coffig.spi.ConfigurationProvider;

public class CoffigTest {
    private String mutableValue1 = "bar";
    private String mutableValue2 = "val1";
    private String mutableValue3 = "val2";

    private final ConfigurationProvider appConfigProvider = () -> new MapNode(
            new NamedNode("id", "foo"),
            new NamedNode("name", "The Foo app"),
            new NamedNode("someEnum", "FOO"));

    private final ConfigurationProvider usersConfigProvider = () -> new MapNode(
            new NamedNode("id", mutableValue1),
            new NamedNode("users", new ArrayNode("u123456", "u456789")),
            new NamedNode("elements",
                    new MapNode(new NamedNode("key1", mutableValue2), new NamedNode("key2", mutableValue3))),
            new NamedNode("items", "one"));

    private final ConfigurationProvider classConfigProvider = () -> new MapNode(
            new NamedNode("someListClass", "java.lang.Object"));

    @Test
    public void testConfigurationNotNull() {
        Coffig coffig = Coffig.builder().withProviders(new VoidProvider()).build();
        assertThat(coffig.get(App.class)).isNotNull();
    }

    @Test
    public void testWithSimpleProvider() {
        Coffig coffig = Coffig.builder().withProviders(appConfigProvider).build();
        App app = coffig.get(App.class);
        assertThat(app.id).isEqualTo("foo");
        assertThat(app.name).isEqualTo("The Foo app");
    }

    @Test
    public void testWithMergedConfiguration() {
        Coffig coffig = Coffig.builder().withProviders(appConfigProvider, usersConfigProvider).build();
        App app = coffig.get(App.class);

        assertThat(app.id).isEqualTo("bar");
        assertThat(app.name).isEqualTo("The Foo app");

        assertThat(app.users).hasSize(2);
        assertThat(app.users[0]).isEqualTo("u123456");
        assertThat(app.users[1]).isEqualTo("u456789");
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
        assertThat(appServerPort).isEqualTo(8080);
    }

    @Test
    public void testGetOptionalWithPathAndDefaultValue() throws Exception {
        Coffig coffig = Coffig.builder().withProviders(appConfigProvider).build();
        assertThat(coffig.getOptional(String.class, "unknown").orElse("defaultValue"))
                .isEqualTo("defaultValue");
    }

    @Test
    public void testGetWithPathAndDefaultValue() throws Exception {
        Coffig coffig = Coffig.builder().withProviders(appConfigProvider).build();
        assertThat(coffig.get(String.class, "unknown")).isEqualTo("");
    }

    @Test(expected = ConfigurationException.class)
    public void testGetMandatoryWithPathAndDefaultValue() throws Exception {
        Coffig coffig = Coffig.builder().withProviders(appConfigProvider).build();
        coffig.getMandatory(String.class, "unknown");
    }

    @Test
    public void testGetSingleValueAsArray() throws Exception {
        Coffig coffig = Coffig.builder().withProviders(usersConfigProvider).build();
        assertThat(coffig.get(App.class).items).containsExactly("one");
    }

    @Test
    public void testGetMapAsArray() throws Exception {
        Coffig coffig = Coffig.builder().withProviders(usersConfigProvider).build();
        assertThat(coffig.get(App.class).elements).containsOnly("val1", "val2");
    }

    @Test
    public void testErrorMessages() throws Exception {
        Coffig coffig = Coffig.builder().withProviders(classConfigProvider).build();
        try {
            coffig.get(ClassConfig.class);
            fail("should have thrown a ConfigurationException");
        } catch (ConfigurationException e) {
            assertThat(e.getMessage()).isEqualTo("[CONFIGURATION] Non assignable class");
            assertThat(e.getDescription())
                    .isEqualTo("Class 'java.lang.Object' is not compatible with type '? extends java.util.List'.");
        }
    }

    @Test
    public void testListeners() {
        AtomicInteger listener0CallCount = new AtomicInteger();
        AtomicInteger listener1CallCount = new AtomicInteger();
        AtomicInteger listener2CallCount = new AtomicInteger();
        Coffig coffig = Coffig.builder().withProviders(usersConfigProvider).build();
        coffig.refresh();
        coffig.registerListener("", c -> listener0CallCount.incrementAndGet());
        coffig.registerListener("elements", c -> listener1CallCount.incrementAndGet());
        ConfigChangeListener configChangeListener = c -> listener2CallCount.incrementAndGet();
        coffig.registerListener("elements.key1", configChangeListener);
        assertThat(listener0CallCount.get()).isEqualTo(0);
        assertThat(listener1CallCount.get()).isEqualTo(0);
        assertThat(listener2CallCount.get()).isEqualTo(0);
        coffig.refresh();
        assertThat(listener0CallCount.get()).isEqualTo(0);
        assertThat(listener1CallCount.get()).isEqualTo(0);
        assertThat(listener2CallCount.get()).isEqualTo(0);
        mutableValue1 = "baz";
        coffig.refresh();
        assertThat(listener0CallCount.get()).isEqualTo(1);
        assertThat(listener1CallCount.get()).isEqualTo(0);
        assertThat(listener2CallCount.get()).isEqualTo(0);
        mutableValue3 = "newVal";
        coffig.refresh();
        assertThat(listener0CallCount.get()).isEqualTo(2);
        assertThat(listener1CallCount.get()).isEqualTo(1);
        assertThat(listener2CallCount.get()).isEqualTo(0);
        mutableValue2 = "newVal";
        coffig.refresh();
        assertThat(listener0CallCount.get()).isEqualTo(3);
        assertThat(listener1CallCount.get()).isEqualTo(2);
        assertThat(listener2CallCount.get()).isEqualTo(1);
        coffig.unregisterListener(configChangeListener);
        mutableValue2 = "otherNewVal";
        coffig.refresh();
        assertThat(listener0CallCount.get()).isEqualTo(4);
        assertThat(listener1CallCount.get()).isEqualTo(3);
        assertThat(listener2CallCount.get()).isEqualTo(1);
    }

    private static class App {
        String id;
        String name;
        EnumFixture enumFixture;
        String[] users;
        String[] items;
        String[] elements;
    }

    private static class ClassConfig {
        private Class<? extends List> someListClass;
    }
}
