/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValueNodeTest {

    public static final ValueNode val1 = new ValueNode("val1");

    @Test
    public void testEquals() {
        assertThat(new ValueNode("val1")).isEqualTo(new ValueNode("val1"));
        assertThat(new ValueNode("val1")).isNotEqualTo(new ValueNode("val2"));
    }

    @Test
    public void testValue() {
        assertThat(val1.value()).isEqualTo("val1");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testChildNode() {
        val1.value(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testChildNodes() {
        val1.values();
    }

    @Test
    public void testMergeValueNode() {
        ValueNode val1 = new ValueNode("val1");
        ValueNode val2 = new ValueNode("val2");

        TreeNode newValueNode = val1.merge(val2);
        assertThat(newValueNode).isSameAs(val2);
    }

    @Test
    public void testSearch() throws Exception {
        ValueNode val1 = new ValueNode("val1");
        assertThat(val1.get("foo").isPresent()).isFalse();
    }
}
