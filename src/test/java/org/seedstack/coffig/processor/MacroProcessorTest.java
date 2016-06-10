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
import org.seedstack.coffig.node.ArrayNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.MutableMapNode;
import org.seedstack.coffig.node.NamedNode;

import static org.assertj.core.api.Assertions.assertThat;

public class MacroProcessorTest {
    private MacroProcessor macroProcessor = new MacroProcessor();
    private MutableMapNode config;

    @Before
    public void setUp() throws Exception {
        config = new MutableMapNode(
                new NamedNode("index", "2"),

                new NamedNode("erroneousIndex", "9"),

                new NamedNode("names", new ArrayNode("Adrien", "Kavi", "Redouane", "Pierre", "Thierry")),

                new NamedNode("key1", "World"),

                new NamedNode("key2", new MapNode(
                        new NamedNode("subKey", "boy"),

                        new NamedNode("subIndex", "3")
                )),

                new NamedNode("test", new ArrayNode(
                        new MapNode(
                                new NamedNode("message", "Hello ${key1}!")
                        ),
                        new MapNode(
                                new NamedNode("message", "Hello ${key2.subKey}!")
                        ),
                        new MapNode(
                                new NamedNode("message", "Hello ${names[1]}!")
                        ),
                        new MapNode(
                                new NamedNode("message", "Hello ${names[${index}]}!")
                        ),
                        new MapNode(
                                new NamedNode("message", "Hello ${names[${index}]:'Toto'}!")
                        ),
                        new MapNode(
                                new NamedNode("message", "Hello ${names[${erroneousIndex}]:'Toto'}!")
                        ),
                        new MapNode(
                                new NamedNode("message", "Hello ${names[${erroneousIndex}]:names[${index}]}!")
                        ),
                        new MapNode(
                                new NamedNode("message", "Hello ${names[${erroneousIndex}]:names[${key2.subIndex}]}!")
                        )
                ))
        );
    }

    @Test
    public void testProcessMacro() throws Exception {
        macroProcessor.process(config);
        assertThat(config.get("test[0].message").get().value()).isEqualTo("Hello World!");
        assertThat(config.get("test[1].message").get().value()).isEqualTo("Hello boy!");
        assertThat(config.get("test[2].message").get().value()).isEqualTo("Hello Kavi!");
        assertThat(config.get("test[3].message").get().value()).isEqualTo("Hello Redouane!");
        assertThat(config.get("test[4].message").get().value()).isEqualTo("Hello Redouane!");
        assertThat(config.get("test[5].message").get().value()).isEqualTo("Hello Toto!");
        assertThat(config.get("test[6].message").get().value()).isEqualTo("Hello Redouane!");
        assertThat(config.get("test[7].message").get().value()).isEqualTo("Hello Pierre!");
    }
}
