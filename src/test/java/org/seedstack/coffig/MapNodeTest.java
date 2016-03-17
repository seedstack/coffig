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

import static org.assertj.core.api.Assertions.assertThat;

public class MapNodeTest {

    @Test
    public void testChildNode() {
        MapNode mapNode1 = new MapNode(new NamedNode("id", "foo"));

        assertThat(mapNode1.value("id")).isNotNull();
        assertThat(mapNode1.value("id")).isInstanceOf(ValueNode.class);

        try {
            mapNode1.value("name");
            Assertions.failBecauseExceptionWasNotThrown(PropertyNotFoundException.class);
        } catch (PropertyNotFoundException e) {
            assertThat(e).hasMessage("Property \"[name]\" was not found");
            assertThat(e.getPropertyName()).isEqualTo("[name]");
        }
    }

    @Test
    public void testNodeExist() {
        MapNode mapNode1 = new MapNode(new NamedNode("id", "foo"));
        Assertions.assertThat(mapNode1.exist("id")).isTrue();
        Assertions.assertThat(mapNode1.exist("zzz")).isFalse();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testChildNodes() {
        new MapNode().values();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testValue() {
        new MapNode().value();
    }

    @Test
    public void testMerge() {
        MapNode mapNode1 = new MapNode(new NamedNode("id", "foo"), new NamedNode("name", "The foo app"));
        MapNode mapNode2 = new MapNode(new NamedNode("name", "foo app"), new NamedNode("description", "The app description"));

        TreeNode mapNode3 = mapNode1.merge(mapNode2);
        assertThat(mapNode3).isNotNull();
        assertThat(mapNode3.value("id")).isSameAs(mapNode1.value("id"));
        assertThat(mapNode3.value("id").value()).isEqualTo("foo");
        assertThat(mapNode3.value("name").value()).isEqualTo("foo app");
        assertThat(mapNode3.value("description").value()).isEqualTo("The app description");
    }

    @Test
    public void testMergeIsImmutable() {
        MapNode mapNode1 = new MapNode(new NamedNode("id", "foo"));
        MapNode mapNode2 = new MapNode(new NamedNode("name", "foo app"));

        TreeNode newMapNode = mapNode1.merge(mapNode2);
        assertThat(newMapNode).isNotSameAs(mapNode1);
        assertThat(newMapNode).isNotSameAs(mapNode2);

        // test that we avoid to create unnecessary objects
        assertThat(newMapNode.value("id")).isSameAs(mapNode1.value("id"));
        assertThat(newMapNode.value("name")).isSameAs(mapNode2.value("name"));
    }

    @Test
    public void testMergeOnlyWithMapNode() {
        MapNode mapNode1 = new MapNode(new NamedNode("id", "foo"));
        ValueNode mapNode2 = new ValueNode("foo app");

        try {
            mapNode1.merge(mapNode2);
            Assertions.failBecauseExceptionWasNotThrown(ConfigurationException.class);
        } catch (ConfigurationException e) {
            assertThat(e).hasMessage(ConfigurationException.INCORRECT_MERGE.apply(ValueNode.class, MapNode.class));
        }
    }

    @Test
    public void testEquals() {
        assertThat(new MapNode(new NamedNode("id", "foo"))).isEqualTo(new MapNode(new NamedNode("id", "foo")));
        assertThat(new MapNode(new NamedNode("id", "foo"))).isNotEqualTo(new MapNode(new NamedNode("id", "foo2")));
        assertThat(new MapNode(new NamedNode("id", "foo"))).isNotEqualTo(new MapNode(new NamedNode("id", "foo"), new NamedNode("name", "foo app")));
        assertThat(new MapNode(new NamedNode("id", "foo"), new NamedNode("name", "foo app"))).isNotEqualTo(new MapNode(new NamedNode("id", "foo")));
    }

    @Test
    public void testGet() throws Exception {
        MapNode mapNode = new MapNode(new NamedNode("id", "foo"), new NamedNode("name", "The foo app"));
        assertThat(mapNode.get("id")).isNotNull();
        assertThat(mapNode.get("id").value()).isEqualTo("foo");
        assertThat(mapNode.get("name").value()).isEqualTo("The foo app");

        try {
            mapNode.get("foo");
            Assertions.failBecauseExceptionWasNotThrown(PropertyNotFoundException.class);
        } catch (PropertyNotFoundException e) {
            Assertions.assertThat(e.getPropertyName()).isEqualTo("[foo]");
        }
    }

    @Test
    public void testWithIntegerKey() {
        MapNode mapNode = new MapNode(new NamedNode("0", "foo"));
        assertThat(mapNode.get("0").value()).isEqualTo("foo");
    }
}
