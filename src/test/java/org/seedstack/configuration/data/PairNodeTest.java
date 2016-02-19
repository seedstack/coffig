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
import org.seedstack.configuration.data.MapNode;
import org.seedstack.configuration.data.PairNode;
import org.seedstack.configuration.data.TreeNode;

public class PairNodeTest {

    public final PairNode pairWithMap = new PairNode("server", new MapNode(new PairNode("port", "80")));

    @Test
    public void testMergedAdd() {
        TreeNode newNode = pairWithMap.merge(new PairNode("server", new MapNode(new PairNode("host", "localhost"))));
        Assertions.assertThat(((PairNode) newNode).get().value("port").value()).isEqualTo("80");
        Assertions.assertThat(((PairNode) newNode).get().value("host").value()).isEqualTo("localhost");
    }

    @Test
    public void testMergedReplace() {
        TreeNode newNode = pairWithMap.merge(new PairNode("server", new MapNode(new PairNode("port", "8080"))));
        Assertions.assertThat(((PairNode) newNode).get().value("port").value()).isEqualTo("8080");
    }

    @Test
    public void testGet() {
        Assertions.assertThat(pairWithMap.get().value("port").value()).isEqualTo("80");
    }

    @Test
    public void testEquals() {
        Assertions.assertThat(new PairNode("id", "foo")).isEqualTo(new PairNode("id", "foo"));
        Assertions.assertThat(new PairNode("id", "foo")).isNotEqualTo(new PairNode("id", "foo2"));
    }
}
