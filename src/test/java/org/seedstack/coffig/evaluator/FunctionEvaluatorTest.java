/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.evaluator;

import org.junit.Before;
import org.junit.Test;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ArrayNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.node.ValueNode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FunctionEvaluatorTest {
    private FunctionEvaluator functionEvaluator = new FunctionEvaluator();

    private MapNode config = new MapNode(
            new NamedNode("null", (String) null),
            new NamedNode("object", new MapNode(
                    new NamedNode("field1", "hello"),
                    new NamedNode("field2", new ArrayNode("item1", "item2", "item3"))
            )),
            new NamedNode("test", new MapNode(
                    new NamedNode("noArg", "$prefix()"),
                    new NamedNode("literal", "$greet('World', '?')"),
                    new NamedNode("nestedCall", "$greet('World', $prefix())"),
                    new NamedNode("nestedRef", "$greet('World', test.noArg)"),
                    new NamedNode("unresolvedRef", "$greet('World', test.unknown)"),
                    new NamedNode("mappedArg1", "$greetSeveralTimes('World', '5', '!')"),
                    new NamedNode("mappedArg2", "$verifyObject(object)"),
                    new NamedNode("escaped", "test: \\$verifyObject(object)!"),
                    new NamedNode("rawNodeArg", "test: $rawNodeArg(object)!"),
                    new NamedNode("rawArrayNode", "test: $rawArrayNode()!"),
                    new NamedNode("rawMapNode", "test: $rawMapNode()!"),
                    new NamedNode("rawValueNode", "test: $rawValueNode()!")
            ))
    );

    private static String rawNodeArg(TreeNode map) {
        assertThat(map.type()).isEqualTo(TreeNode.Type.MAP_NODE);
        return "ok";
    }

    private static ArrayNode rawArrayNode() {
        return new ArrayNode("val1", "val2");
    }

    private static MapNode rawMapNode() {
        return new MapNode(new NamedNode("key1", "val1"));
    }

    private static ValueNode rawValueNode() {
        return new ValueNode("val1");
    }

    private static String greet(String name, String suffix) {
        return "Hello " + name + suffix;
    }

    private static String verifyObject(MappedClass mappedObject) {
        assertThat(mappedObject.field1).isEqualTo("hello");
        assertThat(mappedObject.field2).containsExactly("item1", "item2", "item3");
        return "true";
    }

    private static String greetSeveralTimes(String name, int count, String suffix) {
        StringBuilder sb = new StringBuilder("Hello ");
        for (int i = 0; i < count; i++) {
            sb.append(name);
            if (i < count - 1) {
                sb.append(" ");
            }
        }
        return sb.append(suffix).toString();
    }

    private static String prefix() {
        return "!";
    }

    @Before
    public void setUp() throws Exception {
        functionEvaluator.registerFunction("greet", FunctionEvaluatorTest.class.getDeclaredMethod("greet", String.class, String.class), null);
        functionEvaluator.registerFunction("greetSeveralTimes", FunctionEvaluatorTest.class.getDeclaredMethod("greetSeveralTimes", String.class, int.class, String.class), null);
        functionEvaluator.registerFunction("prefix", FunctionEvaluatorTest.class.getDeclaredMethod("prefix"), null);
        functionEvaluator.registerFunction("verifyObject", FunctionEvaluatorTest.class.getDeclaredMethod("verifyObject", MappedClass.class), null);
        functionEvaluator.registerFunction("rawNodeArg", FunctionEvaluatorTest.class.getDeclaredMethod("rawNodeArg", TreeNode.class), null);
        functionEvaluator.registerFunction("rawMapNode", FunctionEvaluatorTest.class.getDeclaredMethod("rawMapNode"), null);
        functionEvaluator.registerFunction("rawArrayNode", FunctionEvaluatorTest.class.getDeclaredMethod("rawArrayNode"), null);
        functionEvaluator.registerFunction("rawValueNode", FunctionEvaluatorTest.class.getDeclaredMethod("rawValueNode"), null);
        functionEvaluator.initialize(Coffig.basic());
    }

    private String evaluate(String path) {
        return functionEvaluator.evaluate(config, config.get(path).get()).value();
    }

    @Test
    public void testNoArgument() throws Exception {
        assertThat(evaluate("test.noArg")).isEqualTo("!");
    }

    @Test
    public void testLiteralArgument() throws Exception {
        assertThat(evaluate("test.literal")).isEqualTo("Hello World?");
    }

    @Test
    public void testNestedFunctions() throws Exception {
        assertThat(evaluate("test.nestedCall")).isEqualTo("Hello World!");
    }

    @Test
    public void testNestedReference() throws Exception {
        assertThat(evaluate("test.nestedRef")).isEqualTo("Hello World!");
    }

    @Test
    public void testUnresolvedReference() throws Exception {
        assertThat(evaluate("test.unresolvedRef")).isEqualTo("Hello World");
    }

    @Test
    public void testMappedLiteral() throws Exception {
        assertThat(evaluate("test.mappedArg1")).isEqualTo("Hello World World World World World!");
    }

    @Test
    public void testMappedReference() throws Exception {
        assertThat(evaluate("test.mappedArg2")).isEqualTo("true");
    }

    @Test
    public void testEscaping() throws Exception {
        assertThat(evaluate("test.escaped")).isEqualTo("test: $verifyObject(object)!");
    }

    @Test
    public void testRawNodeArgument() throws Exception {
        assertThat(evaluate("test.rawNodeArg")).isEqualTo("test: ok!");
    }

    @Test
    public void testRawArrayNode() throws Exception {
        assertThat(evaluate("test.rawArrayNode")).isEqualTo("test: <!! [CONFIGURATION] Cannot convert array to value !!>!");
    }

    @Test
    public void testRawMapNode() throws Exception {
        assertThat(evaluate("test.rawMapNode")).isEqualTo("test: <!! [CONFIGURATION] Cannot convert map to value !!>!");
    }

    @Test
    public void testRawValueNode() throws Exception {
        assertThat(evaluate("test.rawValueNode")).isEqualTo("test: val1!");
    }


    private static class MappedClass {
        private String field1;
        private List<String> field2;
    }
}
