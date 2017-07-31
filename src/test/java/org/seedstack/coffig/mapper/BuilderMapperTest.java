/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.junit.Test;
import org.seedstack.coffig.BuilderSupplier;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class BuilderMapperTest {
    private ConfigurationMapper mapper = Coffig.basic().getMapper();
    private MapNode node = new MapNode(
            new NamedNode("test1", new MapNode(
                    new NamedNode("key1", "value1"),
                    new NamedNode("key2", "value2")
            )),
            new NamedNode("test2", new MapNode(
                    new NamedNode("key1", "value3"),
                    new NamedNode("key2", "value4")
            )));

    @Test
    public void testMapGenericSupplier() {
        SomeObject someObject = (SomeObject) mapper.map(node, SomeObject.class);
        TestBuilder builder = someObject.test1.get();
        assertThat(builder.getKey1()).isEqualTo("value1");
        assertThat(builder.getKey2()).isEqualTo("value2");
    }

    @Test
    public void testMapCustomSupplier() {
        SomeObject someObject = (SomeObject) mapper.map(node, SomeObject.class);
        TestBuilder builder = someObject.test2.get();
        assertThat(builder.getKey1()).isEqualTo("value3");
        assertThat(builder.getKey2()).isEqualTo("value4");
    }

    private static class SomeObject {
        private BuilderSupplier<TestBuilder> test1;
        private TestBuilderSupplier test2;
    }

    private static class TestBuilder {
        private String key1;
        private String key2;

        public void key1(String key1) {
            this.key1 = key1;
        }

        public void key2(String key2) {
            this.key2 = key2;
        }

        public String getKey1() {
            return key1;
        }

        public String getKey2() {
            return key2;
        }
    }

    private static class TestBuilderFactory {
        private static TestBuilder builder() {
            return new TestBuilder();
        }
    }

    private static class TestBuilderSupplier implements BuilderSupplier<TestBuilder> {
        private final TestBuilder testBuilder = TestBuilderFactory.builder();

        @Override
        public TestBuilder get() {
            return testBuilder;
        }
    }
}
