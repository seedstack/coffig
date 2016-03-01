/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data.mutable;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MutableNodesTest {

    @Test
    public void testMutableNodes() {
        MutableMapNode mapNode = new MutableMapNode();
        MutableArrayNode arrayNode = new MutableArrayNode();
        arrayNode.add(new MutableValueNode("foo"));
        arrayNode.add(new MutableValueNode("bar"));
        arrayNode.add(0, new MutableValueNode("foo"));
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
        mapNode.set("custom", "foo");
        assertThat(mapNode.search("custom").value()).isEqualTo("foo");
    }

    @Test
    public void testSetUpdateMap() {
        MutableMapNode mapNode = new MutableMapNode();
        mapNode.set("custom.node.old", "foo");
        assertThat(mapNode.search("custom.node.old").value()).isEqualTo("foo");
        mapNode.set("custom.node.new", "bar");
        assertThat(mapNode.search("custom.node.old").value()).isEqualTo("foo");
        assertThat(mapNode.search("custom.node.new").value()).isEqualTo("bar");
    }

    @Test
    public void testSetArray() {
        MutableMapNode mapNode = new MutableMapNode();
        mapNode.set("custom.foo.0", "val1");
        assertThat(mapNode.search("custom.foo.0").value()).isEqualTo("val1");
        mapNode.set("custom.foo.1", "val2");
        assertThat(mapNode.search("custom.foo.1").value()).isEqualTo("val2");
        mapNode.set("custom.foo.1", "val3");
        assertThat(mapNode.search("custom.foo.0").value()).isEqualTo("val1");
        assertThat(mapNode.search("custom.foo.1").value()).isEqualTo("val3");
    }

    @Test
    public void testSetArrayOfNode() {
        MutableMapNode mapNode = new MutableMapNode();
        mapNode.set("arr.0.custom", "foo");
        assertThat(mapNode.search("arr.0.custom").value()).isEqualTo("foo");
    }
}
