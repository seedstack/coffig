/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data.mutable;

import org.junit.Test;
import org.seedstack.coffig.data.MapNode;
import org.seedstack.coffig.data.PairNode;
import org.seedstack.coffig.data.ValueNode;

import static org.assertj.core.api.Assertions.assertThat;

public class MutableNodesTest {

    @Test
    public void testMutableNodes() {
        MutableMapNode mapNode = new MutableMapNode();
        MutableArrayNode arrayNode = new MutableArrayNode();
        arrayNode.add(new ValueNode("foo"));
        arrayNode.add(new ValueNode("bar"));
        arrayNode.add(0, new ValueNode("foo"));
        MutablePairNode pairNode = new MutablePairNode();
        pairNode.setName("custom");
        pairNode.setValue(arrayNode);
        mapNode.put("custom", pairNode);

        assertThat(mapNode.search("custom.0").value()).isEqualTo("foo");
        assertThat(mapNode.search("custom.1").value()).isEqualTo("foo");
        assertThat(mapNode.search("custom.2").value()).isEqualTo("bar");
    }

    @Test
    public void testSetValue() {
        MutableMapNode mapNode = new MutableMapNode();
        mapNode.set("custom", new ValueNode("foo"));
        assertThat(mapNode.search("custom").value()).isEqualTo("foo");
    }

    @Test
    public void testSetUpdateMap() {
        MutableMapNode mapNode = new MutableMapNode();
        mapNode.set("custom.node.old", new ValueNode("foo"));
        assertThat(mapNode.search("custom.node.old").value()).isEqualTo("foo");
        mapNode.set("custom.node.new", new ValueNode("bar"));
        assertThat(mapNode.search("custom.node.old").value()).isEqualTo("foo");
        assertThat(mapNode.search("custom.node.new").value()).isEqualTo("bar");
    }

    @Test
    public void testSetArray() {
        MutableMapNode mapNode = new MutableMapNode();
        mapNode.set("custom.foo.0", new ValueNode("val1"));
        assertThat(mapNode.search("custom.foo.0").value()).isEqualTo("val1");
        mapNode.set("custom.foo.1", new ValueNode("val2"));
        assertThat(mapNode.search("custom.foo.1").value()).isEqualTo("val2");
        mapNode.set("custom.foo.1", new ValueNode("val3"));
        assertThat(mapNode.search("custom.foo.0").value()).isEqualTo("val1");
        assertThat(mapNode.search("custom.foo.1").value()).isEqualTo("val3");
    }

    @Test
    public void testSetArrayOfNode() {
        MutableMapNode mapNode = new MutableMapNode();
        mapNode.set("arr.0.custom", new ValueNode("foo"));
        assertThat(mapNode.search("arr.0.custom").value()).isEqualTo("foo");
    }

    @Test
    public void testSetTreeNode() {
        MutableMapNode mapNode = new MutableMapNode();
        mapNode.set("arr.0.custom", new MapNode(new PairNode("key", new ValueNode("val"))));
        assertThat(mapNode.search("arr.0.custom.key").value()).isEqualTo("val");
    }
}
