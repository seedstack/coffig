/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.configuration.data;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.configuration.ConfigurationException;
import org.seedstack.configuration.PropertyNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

public class MapNodeTest {

    @Test
    public void testChildNode() {
        MapNode mapNode1 = new MapNode(new PairNode("id", "foo"));

        assertThat(mapNode1.value("id")).isNotNull();
        assertThat(mapNode1.value("id")).isInstanceOf(ValueNode.class);

        try {
            mapNode1.value("name");
            Assertions.failBecauseExceptionWasNotThrown(PropertyNotFoundException.class);
        } catch (PropertyNotFoundException e) {
            assertThat(e).hasMessage("Property \"name\" was not found");
            assertThat(e.getPropertyName()).isEqualTo("name");
        }
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
        MapNode mapNode1 = new MapNode(new PairNode("id", "foo"), new PairNode("name", "The foo app"));
        MapNode mapNode2 = new MapNode(new PairNode("name", "foo app"), new PairNode("description", "The app description"));

        TreeNode mapNode3 = mapNode1.merge(mapNode2);
        assertThat(mapNode3).isNotNull();
        assertThat(mapNode3.value("id")).isSameAs(mapNode1.value("id"));
        assertThat(mapNode3.value("id").value()).isEqualTo("foo");
        assertThat(mapNode3.value("name").value()).isEqualTo("foo app");
        assertThat(mapNode3.value("description").value()).isEqualTo("The app description");
    }

    @Test
    public void testMergeIsImmutable() {
        MapNode mapNode1 = new MapNode(new PairNode("id", "foo"));
        MapNode mapNode2 = new MapNode(new PairNode("name", "foo app"));

        TreeNode newMapNode = mapNode1.merge(mapNode2);
        assertThat(newMapNode).isNotSameAs(mapNode1);
        assertThat(newMapNode).isNotSameAs(mapNode2);

        // test that we avoid to create unnecessary objects
        assertThat(newMapNode.value("id")).isSameAs(mapNode1.value("id"));
        assertThat(newMapNode.value("name")).isSameAs(mapNode2.value("name"));
    }

    @Test
    public void testMergeOnlyWithMapNode() {
        MapNode mapNode1 = new MapNode(new PairNode("id", "foo"));
        PairNode mapNode2 = new PairNode("name", "foo app");

        try {
            mapNode1.merge(mapNode2);
            Assertions.failBecauseExceptionWasNotThrown(ConfigurationException.class);
        } catch (ConfigurationException e) {
            Assertions.assertThat(e).hasMessage(ConfigurationException.INCORRECT_MERGE.apply(PairNode.class, MapNode.class));
        }
    }

    @Test
    public void testEquals() {
        assertThat(new MapNode(new PairNode("id", "foo"))).isEqualTo(new MapNode(new PairNode("id", "foo")));
        assertThat(new MapNode(new PairNode("id", "foo"))).isNotEqualTo(new MapNode(new PairNode("id", "foo2")));
        assertThat(new MapNode(new PairNode("id", "foo"))).isNotEqualTo(new MapNode(new PairNode("id", "foo"), new PairNode("name", "foo app")));
        assertThat(new MapNode(new PairNode("id", "foo"), new PairNode("name", "foo app"))).isNotEqualTo(new MapNode(new PairNode("id", "foo")));
    }
}
