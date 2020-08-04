/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.junit.Test;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassMapperTest {
    private ConfigurationMapper mapper = Coffig.basic().getMapper();

    @Test
    public void testMapAnyClass() {
        FixtureAnyClass result = (FixtureAnyClass) mapper.map(new MapNode(new NamedNode("anyClass", "java.lang.String")), FixtureAnyClass.class);
        assertThat(result).isNotNull();
        assertThat(result.anyClass).isEqualTo(String.class);
    }

    @Test
    public void testMapUpperBoundedClass() {
        FixtureUpperBoundedClass result = (FixtureUpperBoundedClass) mapper.map(new MapNode(new NamedNode("mapperClass", "org.seedstack.coffig.mapper.ClassMapper")), FixtureUpperBoundedClass.class);
        assertThat(result).isNotNull();
        assertThat(result.mapperClass).isEqualTo(ClassMapper.class);
    }

    @Test(expected = ConfigurationException.class)
    public void testMapWrongUpperBoundedClass() throws NoSuchFieldException {
        mapper.map(new MapNode(new NamedNode("mapperClass", "java.lang.String")), FixtureUpperBoundedClass.class);
    }

    @Test
    public void testMapLowerBoundedClass() {
        FixtureLowerBoundedClass result = (FixtureLowerBoundedClass) mapper.map(new MapNode(new NamedNode("configurationMapperClass", "org.seedstack.coffig.spi.ConfigurationMapper")), FixtureLowerBoundedClass.class);
        assertThat(result).isNotNull();
        assertThat(result.configurationMapperClass).isEqualTo(ConfigurationMapper.class);
    }

    @Test(expected = ConfigurationException.class)
    public void testMapWrongLowerBoundedClass() throws NoSuchFieldException {
        mapper.map(new MapNode(new NamedNode("configurationMapperClass", "java.lang.String")), FixtureLowerBoundedClass.class);
    }

    @Test
    public void testUnmapOptional() {
    }

    private static class FixtureAnyClass {
        private Class<?> anyClass;
    }

    private static class FixtureUpperBoundedClass {
        private Class<? extends ConfigurationMapper> mapperClass;
    }

    private static class FixtureLowerBoundedClass {
        private Class<? super ClassMapper> configurationMapperClass;
    }
}
