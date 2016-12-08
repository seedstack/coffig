/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import org.junit.Test;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;

import static org.assertj.core.api.Assertions.assertThat;

public class PrefixProviderTest {
    private PrefixProvider<InMemoryProvider> prefixProvider = new PrefixProvider<>(
            "p1.p2",
            new InMemoryProvider().put("someKey", "someValue")
    );

    @Test
    public void testPrefixIsApplied() throws Exception {
        assertThat(prefixProvider.provide()).isEqualTo(new MapNode(
                new NamedNode("p1", new MapNode(
                        new NamedNode("p2", new MapNode(
                                new NamedNode("someKey", "someValue"))
                        )
                ))
        ));
    }
}