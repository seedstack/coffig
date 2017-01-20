/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.evaluator;

import org.junit.Before;
import org.junit.Test;
import org.seedstack.coffig.node.ArrayNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.NamedNode;
import org.seedstack.coffig.node.ValueNode;

import static org.assertj.core.api.Assertions.assertThat;

public class MacroEvaluatorTest {
    private MacroEvaluator macroEvaluator = new MacroEvaluator();
    private MapNode config;

    @Before
    public void setUp() throws Exception {
        config = new MapNode(
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
                        ),
                        new MapNode(
                                new NamedNode("message", "Hello ${names[0]} and ${names[1]}!")
                        ),
                        new MapNode(
                                new NamedNode("message", "Hello \\${names[0]} and ${names[1]}!")
                        ),
                        new MapNode(
                                new NamedNode("message", "Hello \\${names[${index}]}!")
                        ),
                        new MapNode(
                                new NamedNode("message", "Hello ${names[\\${index}]}!")
                        )
                ))
        );
    }

    @Test
    public void testProcessMacro() throws Exception {
        assertThat(evaluate("test[0].message")).isEqualTo("Hello World!");
        assertThat(evaluate("test[1].message")).isEqualTo("Hello boy!");
        assertThat(evaluate("test[2].message")).isEqualTo("Hello Kavi!");
        assertThat(evaluate("test[3].message")).isEqualTo("Hello Redouane!");
        assertThat(evaluate("test[4].message")).isEqualTo("Hello Redouane!");
        assertThat(evaluate("test[5].message")).isEqualTo("Hello Toto!");
        assertThat(evaluate("test[6].message")).isEqualTo("Hello Redouane!");
        assertThat(evaluate("test[7].message")).isEqualTo("Hello Pierre!");
        assertThat(evaluate("test[8].message")).isEqualTo("Hello Adrien and Kavi!");
    }

    @Test
    public void testEscaping() throws Exception {
        assertThat(evaluate("test[9].message")).isEqualTo("Hello ${names[0]} and Kavi!");
        assertThat(evaluate("test[10].message")).isEqualTo("Hello ${names[${index}]}!");
        assertThat(evaluate("test[11].message")).isEqualTo("Hello !");
    }

    private String evaluate(String path) {
        return macroEvaluator.evaluate(config, (ValueNode) config.get(path).get()).value();
    }
}
