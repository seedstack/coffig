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
import org.seedstack.coffig.*;

public class MutableTreeNodeTest {

    MapNode root = new MapNode(
            new NamedNode("id", "foo"),
            new NamedNode("name", new ValueNode("The Foo app")),

            new NamedNode("users", new ArrayNode("u123456", "u456789")),

            new NamedNode("datasources", new ArrayNode(new MapNode(
                    new NamedNode("name", "ds1"),
                    new NamedNode("url", "jdbc:hsqldb:hsql://localhost:9001/ds1"),
                    new NamedNode("driver", "org.hsqldb.jdbcDriver")
            ))),

            new NamedNode("server", new MapNode(
                    new NamedNode("host", "localhost"),
                    new NamedNode("port", "80")
            ))
    );

    MapNode root2 = new MapNode(
            new NamedNode("id", "fuu"),
            new NamedNode("description", new ValueNode("some description")),

            new NamedNode("users", new ArrayNode("u123456", "u456789", "uZZZZZ")),

            new NamedNode("server", new MapNode(
                    new NamedNode("scheme", new ArrayNode("http", "https")),
                    new NamedNode("port", "8080")
            ))
    );

    MapNode mergedRoot = new MapNode(
            new NamedNode("id", "fuu"),
            new NamedNode("name", new ValueNode("The Foo app")),
            new NamedNode("description", new ValueNode("some description")),

            new NamedNode("users", new ArrayNode("u123456", "u456789", "uZZZZZ")),

            new NamedNode("datasources", new ArrayNode(new MapNode(
                    new NamedNode("name", "ds1"),
                    new NamedNode("url", "jdbc:hsqldb:hsql://localhost:9001/ds1"),
                    new NamedNode("driver", "org.hsqldb.jdbcDriver")
            ))),

            new NamedNode("server", new MapNode(
                    new NamedNode("host", "localhost"),
                    new NamedNode("port", "8080"),
                    new NamedNode("scheme", new ArrayNode("http", "https"))
            ))
    );

    @Test
    public void testNode() {
        Assertions.assertThat(root.value("id").value()).isEqualTo("foo");
        Assertions.assertThat(root.value("name").value()).isEqualTo("The Foo app");

        Assertions.assertThat(root.value("users").values()).hasSize(2);
        Assertions.assertThat(root.value("users").values()[0].value()).isEqualTo("u123456");
        Assertions.assertThat(root.value("users").values()[1].value()).isEqualTo("u456789");

        TreeNode dataSource1 = root.value("datasources").values()[0];
        Assertions.assertThat(dataSource1.value("name").value()).isEqualTo("ds1");
        Assertions.assertThat(dataSource1.value("url").value()).isEqualTo("jdbc:hsqldb:hsql://localhost:9001/ds1");

        Assertions.assertThat(root.value("server").value("host").value()).isEqualTo("localhost");
        Assertions.assertThat(root.value("server").value("port").value()).isEqualTo("80");
    }

    @Test
    public void testMerge() {
        Assertions.assertThat(root.merge(root2)).isEqualTo(mergedRoot);
    }

    @Test
    public void testSearch() throws Exception {
        Assertions.assertThat(mergedRoot.get("users.0").value()).isEqualTo("u123456");
        Assertions.assertThat(mergedRoot.get("server.scheme.0").value()).isEqualTo("http");
    }

    @Test
    public void testSearchMissingProps() throws Exception {
        try {
            mergedRoot.get("server.scheme.0.foo.bar");
            Assertions.failBecauseExceptionWasNotThrown(PropertyNotFoundException.class);
        } catch (PropertyNotFoundException e) {
            Assertions.assertThat(e.getPropertyName()).isEqualTo("server.scheme.0.[foo.bar]");
        }
    }

    @Test
    public void testToString() throws Exception {
        System.out.println(root.toString());
        Assertions.assertThat(root.toString()).isEqualTo(
                "server:\n" +
                "  port: 80\n" +
                "  host: localhost\n" +
                "datasources:\n" +
                "  -\n" +
                "    driver: org.hsqldb.jdbcDriver\n" +
                "    name: ds1\n" +
                "    url: jdbc:hsqldb:hsql://localhost:9001/ds1\n" +
                "name: The Foo app\n" +
                "id: foo\n" +
                "users:\n" +
                "  - u123456\n" +
                "  - u456789");
    }
}
