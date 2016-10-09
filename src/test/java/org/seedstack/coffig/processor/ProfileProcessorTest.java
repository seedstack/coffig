/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.processor;

import org.junit.Before;
import org.junit.Test;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.MutableMapNode;
import org.seedstack.coffig.node.NamedNode;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileProcessorTest {
    private ProfileProcessor profileProcessor = new ProfileProcessor();
    private MutableMapNode config;

    @Before
    public void setUp() throws Exception {
        config = new MutableMapNode(
                new NamedNode("a", "1"),
                new NamedNode("b<profile1>", "2"),
                new NamedNode("c<profile2>", new MapNode(
                        new NamedNode("ca", "3"),
                        new NamedNode("cb<profile3>", "4")
                ))
        );
        profileProcessor.process(config);
    }

    @Test
    public void testRemoval() throws Exception {
        assertThat(config.get("a").get().attributes().get("profile")).isNull();
        assertThat(config.get("b").get().attributes().get("profile")).isEqualTo("profile1");
        assertThat(config.get("c").get().attributes().get("profile")).isEqualTo("profile2");
        assertThat(config.get("c.cb").get().attributes().get("profile")).isEqualTo("profile3");
    }
}
