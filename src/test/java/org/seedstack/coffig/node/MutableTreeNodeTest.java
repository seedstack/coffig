/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.seedstack.coffig.TreeNode;

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
        assertThat(root.node("id").value()).isEqualTo("foo");
        assertThat(root.node("name").value()).isEqualTo("The Foo app");

        assertThat(root.node("users").nodes()).hasSize(2);
        assertThat(root.node("users").node("0").value()).isEqualTo("u123456");
        assertThat(root.node("users").node("1").value()).isEqualTo("u456789");

        TreeNode dataSource1 = root.node("datasources").node("0");
        assertThat(dataSource1.node("name").value()).isEqualTo("ds1");
        assertThat(dataSource1.node("url").value()).isEqualTo("jdbc:hsqldb:hsql://localhost:9001/ds1");

        assertThat(root.node("server").node("host").value()).isEqualTo("localhost");
        assertThat(root.node("server").node("port").value()).isEqualTo("80");
    }

    @Test
    public void testMerge() {
        assertThat(root.merge(root2)).isEqualTo(mergedRoot);
    }

    @Test
    public void testSearch() throws Exception {
        assertThat(mergedRoot.get("users[0]").get().value()).isEqualTo("u123456");
        assertThat(mergedRoot.get("server.scheme[0]").get().value()).isEqualTo("http");
    }

    @Test
    public void testEquality() throws Exception {
        MapNode tree1 = new MapNode(new NamedNode("key1", "value1"));
        MapNode tree2 = new MapNode(new NamedNode("key1", "value1"));
        assertThat(tree1.equals(tree2));
    }

    @Test
    public void testSearchMissingProps() throws Exception {
        assertThat(mergedRoot.get("server.scheme[0]foo.bar").isPresent()).isFalse();
        assertThat(mergedRoot.get("server.scheme[44]").isPresent()).isFalse();
    }

    @Test
    public void testToString() throws Exception {
        assertThat(root.toString()).isEqualTo(
                "server:\n" +
                        "  port: \"80\"\n" +
                        "  host: \"localhost\"\n" +
                        "datasources:\n" +
                        "  -\n" +
                        "    driver: \"org.hsqldb.jdbcDriver\"\n" +
                        "    name: \"ds1\"\n" +
                        "    url: \"jdbc:hsqldb:hsql://localhost:9001/ds1\"\n" +
                        "name: \"The Foo app\"\n" +
                        "id: \"foo\"\n" +
                        "users:\n" +
                        "  - \"u123456\"\n" +
                        "  - \"u456789\"");
    }
}
