/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.node;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class NamedNodeTest {

    public final NamedNode pairWithMap = new NamedNode("server", new MapNode(new NamedNode("port", "80")));

    @Test
    public void testGet() {
        Assertions.assertThat(pairWithMap.node().node("port").value()).isEqualTo("80");
    }

    @Test
    public void testEquals() {
        Assertions.assertThat(new NamedNode("id", "foo")).isEqualTo(new NamedNode("id", "foo"));
        Assertions.assertThat(new NamedNode("id", "foo")).isNotEqualTo(new NamedNode("id", "foo2"));
    }
}
