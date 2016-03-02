/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.coffig.PropertyNotFoundException;

public class MutableTreeNodeTest {

    MapNode root = new MapNode(
            new PairNode("id", "foo"),
            new PairNode("name", new ValueNode("The Foo app")),

            new PairNode("users", new ArrayNode("u123456", "u456789")),

            new PairNode("datasources", new ArrayNode(new MapNode(
                    new PairNode("name", "ds1"),
                    new PairNode("url", "jdbc:hsqldb:hsql://localhost:9001/ds1"),
                    new PairNode("driver", "org.hsqldb.jdbcDriver")
            ))),

            new PairNode("server", new MapNode(
                    new PairNode("host", "localhost"),
                    new PairNode("port", "80")
            ))
    );

    MapNode root2 = new MapNode(
            new PairNode("id", "fuu"),
            new PairNode("description", new ValueNode("some description")),

            new PairNode("users", new ArrayNode("u123456", "u456789", "uZZZZZ")),

            new PairNode("server", new MapNode(
                    new PairNode("scheme", new ArrayNode("http", "https")),
                    new PairNode("port", "8080")
            ))
    );

    MapNode mergedRoot = new MapNode(
            new PairNode("id", "fuu"),
            new PairNode("name", new ValueNode("The Foo app")),
            new PairNode("description", new ValueNode("some description")),

            new PairNode("users", new ArrayNode("u123456", "u456789", "uZZZZZ")),

            new PairNode("datasources", new ArrayNode(new MapNode(
                    new PairNode("name", "ds1"),
                    new PairNode("url", "jdbc:hsqldb:hsql://localhost:9001/ds1"),
                    new PairNode("driver", "org.hsqldb.jdbcDriver")
            ))),

            new PairNode("server", new MapNode(
                    new PairNode("host", "localhost"),
                    new PairNode("port", "8080"),
                    new PairNode("scheme", new ArrayNode("http", "https"))
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
        Assertions.assertThat(mergedRoot.search("users.0").value()).isEqualTo("u123456");
        Assertions.assertThat(mergedRoot.search("server.scheme.0").value()).isEqualTo("http");
    }

    @Test
    public void testSearchMissingProps() throws Exception {
        try {
            mergedRoot.search("server.scheme.0.foo.bar");
            Assertions.failBecauseExceptionWasNotThrown(PropertyNotFoundException.class);
        } catch (PropertyNotFoundException e) {
            Assertions.assertThat(e.getPropertyName()).isEqualTo("server.scheme.0.foo.bar");
        }

        try {
            mergedRoot.search("server.scheme.44");
            Assertions.failBecauseExceptionWasNotThrown(PropertyNotFoundException.class);
        } catch (PropertyNotFoundException e) {
            Assertions.assertThat(e.getCause()).isNotNull();
        }
    }

    @Test
    public void testToString() throws Exception {
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
