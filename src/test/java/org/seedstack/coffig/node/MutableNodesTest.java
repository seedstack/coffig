/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import org.junit.Test;
import org.seedstack.coffig.internal.PropertyNotFoundException;
import org.seedstack.coffig.TreeNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class MutableNodesTest {

    @Test
    public void testMutableNodes() {
        MapNode mapNode = new MapNode();
        ArrayNode arrayNode = new ArrayNode();
        arrayNode.set(null, new ValueNode("foo"));
        arrayNode.set(null, new ValueNode("bar1"));
        arrayNode.set("[0]", new ValueNode("bar2"));
        mapNode.set("custom", arrayNode);

        assertThat(mapNode.get("custom[0]").get().value()).isEqualTo("bar2");
        assertThat(mapNode.get("custom[1]").get().value()).isEqualTo("bar1");
        assertThat(mapNode.get("custom[2]").isPresent()).isFalse();
    }

    // Set

    @Test
    public void testSetValue() {
        MapNode mapNode = new MapNode();
        mapNode.set("custom", new ValueNode("foo"));
        assertThat(mapNode.get("custom").get().value()).isEqualTo("foo");
    }

    @Test
    public void testSetUpdateMap() {
        MapNode mapNode = new MapNode();
        mapNode.set("custom.node.old", new ValueNode("foo"));
        assertThat(mapNode.get("custom.node.old").get().value()).isEqualTo("foo");
        mapNode.set("custom.node.new", new ValueNode("bar"));
        assertThat(mapNode.get("custom.node.old").get().value()).isEqualTo("foo");
        assertThat(mapNode.get("custom.node.new").get().value()).isEqualTo("bar");
    }

    @Test
    public void testSetArray() {
        MapNode mapNode = new MapNode();
        mapNode.set("custom.foo[0]", new ValueNode("val1"));
        assertThat(mapNode.get("custom.foo[0]").get().value()).isEqualTo("val1");
        mapNode.set("custom.foo[1]", new ValueNode("val2"));
        assertThat(mapNode.get("custom.foo[1]").get().value()).isEqualTo("val2");
        mapNode.set("custom.foo[1]", new ValueNode("val3"));
        assertThat(mapNode.get("custom.foo[0]").get().value()).isEqualTo("val1");
        assertThat(mapNode.get("custom.foo[1]").get().value()).isEqualTo("val3");
    }

    @Test
    public void testSetArrayOfNode() {
        MapNode mapNode = new MapNode();
        mapNode.set("arr.0.custom", new ValueNode("foo"));
        assertThat(mapNode.get("arr.0.custom").get().value()).isEqualTo("foo");
        mapNode.set("arr.0.custom2", new ValueNode("foo2"));
        assertThat(mapNode.get("arr.0.custom2").get().value()).isEqualTo("foo2");
    }

    @Test
    public void testSetTreeNode() {
        MapNode mapNode = new MapNode();
        mapNode.set("arr.0.custom", new MapNode(new NamedNode("key", new ValueNode("val"))));
        assertThat(mapNode.get("arr.0.custom.key").get().value()).isEqualTo("val");
    }

    // Remove

    @Test
    public void testRemoveMapNode() {
        MapNode mapNode = new MapNode();
        mapNode.set("custom.key", new ValueNode("val"));

        final TreeNode remove = mapNode.remove("custom.key");

        assertRemovedKey(mapNode, "custom");
        assertThat(remove.value()).isEqualTo("val");
    }

    private void assertRemovedKey(TreeNode treeNode, String path) {
        assertThat(treeNode.get(path).isPresent()).isFalse();
    }

    @Test
    public void testRemoveOnlyEmptyMapNodes() {
        MapNode mapNode = new MapNode();
        mapNode.set("custom.property.key1", new ValueNode("val1"));
        mapNode.set("custom.key2", new ValueNode("val2"));

        TreeNode remove = mapNode.remove("custom.property.key1");

        assertRemovedKey(mapNode, "custom.property.key1");
        assertRemovedKey(mapNode, "custom.property");
        assertThat(mapNode.get("custom.key2").get().value()).isEqualTo("val2");
        assertThat(remove.value()).isEqualTo("val1");

    }

    @Test
    public void testRemoveOnlyEmptyArrayNodes() {
        MapNode mapNode = new MapNode();
        mapNode.set("custom.0.0", new ValueNode("val1"));
        mapNode.set("custom.1", new ValueNode("val2"));

        TreeNode remove = mapNode.remove("custom.0.0");

        assertRemovedKey(mapNode, "custom.0.0");
        assertRemovedKey(mapNode, "custom.0");
        assertThat(mapNode.get("custom.1").get().value()).isEqualTo("val2");
        assertThat(remove.value()).isEqualTo("val1");
    }

    @Test
    public void testRemoveNodeNotFound() {
        MapNode mapNode = new MapNode();
        mapNode.set("custom.0.0", new MapNode());

        try {
            mapNode.remove("custom.0.0.fake.test");
            failBecauseExceptionWasNotThrown(PropertyNotFoundException.class);
        } catch (PropertyNotFoundException e) {
            assertThat(e).hasMessage("[CONFIGURATION] Property not found");
        }
    }

    @Test
    public void testMove() throws Exception {
        MapNode mapNode = new MapNode();
        mapNode.set("custom.0.0", new ValueNode("val"));
        mapNode.move("custom.0.0", "custom.key.test");
        assertThat(mapNode.get("custom.key.test").get().value()).isEqualTo("val");
        assertThat(mapNode.get("custom.0.0").isPresent()).isFalse();
    }
}
