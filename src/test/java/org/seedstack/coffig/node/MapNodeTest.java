/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.internal.PropertyNotFoundException;
import org.seedstack.coffig.TreeNode;

import static org.assertj.core.api.Assertions.assertThat;

public class MapNodeTest {

    @Test
    public void testChildNode() {
        MapNode mapNode1 = new MapNode(new NamedNode("id", "foo"));

        assertThat(mapNode1.node("id")).isNotNull();
        assertThat(mapNode1.node("id")).isInstanceOf(ValueNode.class);

        try {
            mapNode1.node("name");
            Assertions.failBecauseExceptionWasNotThrown(PropertyNotFoundException.class);
        } catch (PropertyNotFoundException e) {
            assertThat(e).hasMessage("[CONFIGURATION] Property not found");
            assertThat(e.getPropertyName()).isEqualTo("name");
        }
    }

    @Test
    public void testValues() {
        assertThat(new MapNode(new NamedNode("key1", "val1"), new NamedNode("key2", "val2")).nodes()).containsOnly(new ValueNode("val1"), new ValueNode("val2"));
    }

    @Test(expected = ConfigurationException.class)
    public void testValue() {
        new MapNode().value();
    }

    @Test
    public void testMerge() {
        MapNode mapNode1 = new MapNode(new NamedNode("id", "foo"), new NamedNode("name", "The foo app"));
        MapNode mapNode2 = new MapNode(new NamedNode("name", "foo app"), new NamedNode("description", "The app description"));

        TreeNode mapNode3 = mapNode1.merge(mapNode2);
        assertThat(mapNode3).isNotNull();
        assertThat(mapNode3.node("id")).isSameAs(mapNode1.node("id"));
        assertThat(mapNode3.node("id").value()).isEqualTo("foo");
        assertThat(mapNode3.node("name").value()).isEqualTo("foo app");
        assertThat(mapNode3.node("description").value()).isEqualTo("The app description");
    }

    @Test
    public void testMergeAvoidUnnecessaryCopies() {
        MapNode mapNode1 = new MapNode(new NamedNode("id", "foo"));
        MapNode mapNode2 = new MapNode(new NamedNode("name", "foo app"));

        TreeNode newMapNode = mapNode1.merge(mapNode2);

        // test that we avoid to create unnecessary objects
        assertThat(newMapNode.node("id")).isSameAs(mapNode1.node("id"));
        assertThat(newMapNode.node("name")).isSameAs(mapNode2.node("name"));
    }

    @Test
    public void testMergeOnlyWithMapNode() {
        MapNode mapNode1 = new MapNode(new NamedNode("id", "foo"));
        ValueNode mapNode2 = new ValueNode("foo app");

        try {
            mapNode1.merge(mapNode2);
            Assertions.failBecauseExceptionWasNotThrown(ConfigurationException.class);
        } catch (ConfigurationException e) {
            assertThat(e).hasMessage("[CONFIGURATION] Illegal tree merge");
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
    public void testSearch() throws Exception {
        MapNode mapNode = new MapNode(new NamedNode("id", "foo"), new NamedNode("name", "The foo app"));
        assertThat(mapNode.get("id")).isNotNull();
        assertThat(mapNode.get("id").get().value()).isEqualTo("foo");
        assertThat(mapNode.get("name").get().value()).isEqualTo("The foo app");
        assertThat(mapNode.get("foo").isPresent()).isFalse();
    }
}
