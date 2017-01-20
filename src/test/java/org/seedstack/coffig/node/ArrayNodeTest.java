/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.TreeNode;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrayNodeTest {

    @Test(expected = ConfigurationException.class)
    public void testValue() {
        new ArrayNode("plop").value();
    }

    @Test
    public void testAccessWithName() {
        new ArrayNode("plop").node("0");
    }

    @Test
    public void testChildNodes() {
        List<TreeNode> treeNodes = new ArrayNode("foo", "bar").nodes().collect(Collectors.toList());
        assertThat(treeNodes).hasSize(2);
        assertThat(treeNodes.get(0).value()).isEqualTo("foo");
        assertThat(treeNodes.get(1).value()).isEqualTo("bar");
    }

    @Test
    public void testMerge() {
        ArrayNode arrayNode1 = new ArrayNode("foo", "bar");
        ArrayNode arrayNode2 = new ArrayNode("foo", "bar");
        assertThat(arrayNode1.merge(arrayNode2)).isSameAs(arrayNode2);

        ArrayNode arrayNode3 = new ArrayNode("foo", "fuu");
        assertThat(arrayNode1.merge(arrayNode3)).isSameAs(arrayNode3);
    }

    @Test
    public void testEquals() {
        assertThat(new ArrayNode("foo", "bar")).isEqualTo(new ArrayNode(new ValueNode("foo"), new ValueNode("bar")));
        assertThat(new ArrayNode("foo", "bar", "fuu")).isNotEqualTo(new ArrayNode("foo", "bar"));
        assertThat(new ArrayNode("foo", "bar")).isNotEqualTo(new ArrayNode("foo", "bar", "fuu"));
        assertThat(new ArrayNode("foo", "bar")).isNotEqualTo(new ArrayNode("foo", "fuu"));
    }

    @Test
    public void testSearch() throws Exception {
        ArrayNode arrayNode = new ArrayNode("foo", "bar");

        Assertions.assertThat(arrayNode.get("[0]")).isPresent();
        Assertions.assertThat(arrayNode.get("[0]").get().value()).isEqualTo("foo");
        Assertions.assertThat(arrayNode.get("[1]").get().value()).isEqualTo("bar");
    }

    @Test
    public void testSearchNotIndex() throws Exception {
        ArrayNode arrayNode = new ArrayNode("foo", "bar");
        assertThat(arrayNode.get("x").isPresent()).isFalse();
    }

    @Test
    public void testSearchOutOfBound() throws Exception {
        ArrayNode arrayNode = new ArrayNode("foo", "bar");
        assertThat(arrayNode.get("100").isPresent()).isFalse();
    }
}
