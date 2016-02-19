/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.configuration.mapper;

import org.junit.Test;
import org.seedstack.configuration.data.ArrayNode;
import org.seedstack.configuration.data.MapNode;
import org.seedstack.configuration.data.PairNode;
import org.seedstack.configuration.fixture.AccessorFixture;
import org.seedstack.configuration.fixture.MultiTypesFixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class ObjectMapperTest {

    private final MapNode accessorFixture = new MapNode(new PairNode("field1", "field1"), new PairNode("field2", "field2"));
    private final MapNode multiTypesFixture = new MapNode(
            new PairNode("aBoolean", "true"),
            new PairNode("aByte", "101"),
            new PairNode("aChar", "A"),
            new PairNode("aDouble", "3.14"),
            new PairNode("aFloat", "3.14"),
            new PairNode("anInt", "42"),
            new PairNode("aLong", "42"),
            new PairNode("aShort", "24"),
            new PairNode("aString", "aString"),

            new PairNode("someBoolean", "true", "true", "true"),
            new PairNode("someByte", "101", "101", "101"),
            new PairNode("someChar", "A", "A", "A"),
            new PairNode("someInt", "42", "42", "42"),
            new PairNode("someLong", "42", "42", "42"),
            new PairNode("someShort", "24", "24", "24"),
            new PairNode("someString", "aString", "aString", "aString"),
            new PairNode("someDouble", "3.14", "3.14", "3.14"),
            new PairNode("someFloat", "3.14", "3.14", "3.14"),

            new PairNode("stringList", "aString", "aString", "aString"),
            new PairNode("stringSet", "aString", "aString", "aString"),

            new PairNode("accessorFixture", accessorFixture),
            new PairNode("aMap", new MapNode(new PairNode("1", "true"), new PairNode("2", "false"))),
            new PairNode("fixtureArray", new ArrayNode(accessorFixture, accessorFixture)),
            new PairNode("fixtureList", new ArrayNode(accessorFixture, accessorFixture)),
            new PairNode("fixtureSet", new ArrayNode(accessorFixture, accessorFixture))
    );
    private ObjectMapper<AccessorFixture> accessorMapper = new ObjectMapper<>(AccessorFixture.class);
    private ObjectMapper<MultiTypesFixture> multiTypesMapper = new ObjectMapper<>(MultiTypesFixture.class);

    @Test
    public void testMapField() {
        AccessorFixture accessorFixture = accessorMapper.map(this.accessorFixture);
        assertThat(accessorFixture.getField1()).isEqualTo("field1");
    }

    @Test
    public void testMapSetter() {
        AccessorFixture accessorFixture = accessorMapper.map(this.accessorFixture);
        assertThat(accessorFixture.getField2()).isEqualTo("field22");
    }

    @Test
    public void testMissingProperty() {
        AccessorFixture accessorFixture = accessorMapper.map(new MapNode());
        assertThat(accessorFixture).isNotNull();
        assertThat(accessorFixture.getField1()).isEqualTo("default");
    }

    @Test
    public void testMultiTypes() {
        MultiTypesFixture multiTypesFixture = multiTypesMapper.map(this.multiTypesFixture);
        assertThat(multiTypesFixture.aBoolean).isEqualTo(true);
        assertThat(multiTypesFixture.aByte).isEqualTo((byte) 101);
        assertThat(multiTypesFixture.aChar).isEqualTo('A');
        assertThat(multiTypesFixture.aDouble).isEqualTo(3.14d);
        assertThat(multiTypesFixture.aFloat).isEqualTo(3.14f);
        assertThat(multiTypesFixture.anInt).isEqualTo(42);
        assertThat(multiTypesFixture.aLong).isEqualTo(42L);
        assertThat(multiTypesFixture.aShort).isEqualTo((short) 24);
        assertThat(multiTypesFixture.aString).isEqualTo("aString");
    }

    @Test
    public void testMultiTypesArray() {
        MultiTypesFixture multiTypesFixture = multiTypesMapper.map(this.multiTypesFixture);
        assertThat(multiTypesFixture.someBoolean).containsOnly(true, true, true);
        assertThat(multiTypesFixture.someChar).containsOnly('A', 'A', 'A');
        assertThat(multiTypesFixture.someInt).containsOnly(42, 42, 42);
        assertThat(multiTypesFixture.someLong).containsOnly(42L, 42L, 42L);
        assertThat(multiTypesFixture.someString).containsOnly("aString", "aString", "aString");
        assertThat(multiTypesFixture.someByte).containsOnly((byte) 101, (byte) 101, (byte) 101);
        assertThat(multiTypesFixture.someShort).containsOnly((short) 24, (short) 24, (short) 24);
        assertThat(multiTypesFixture.someDouble).containsOnly(3.14d, 3.14d, 3.14d);
        assertThat(multiTypesFixture.someFloat).containsOnly(3.14f, 3.14f, 3.14f);
    }

    @Test
    public void testMultiTypesList() {
        MultiTypesFixture multiTypesFixture = multiTypesMapper.map(this.multiTypesFixture);
        assertThat(multiTypesFixture.stringList).containsOnly("aString", "aString", "aString");
    }

    @Test
    public void testMultiTypesSet() {
        MultiTypesFixture multiTypesFixture = multiTypesMapper.map(this.multiTypesFixture);
        assertThat(multiTypesFixture.stringSet).containsOnly("aString", "aString", "aString");
    }

    @Test
    public void testMultiTypesObject() {
        MultiTypesFixture multiTypesFixture = multiTypesMapper.map(this.multiTypesFixture);
        assertThat(multiTypesFixture.accessorFixture.getField1()).isEqualTo("field1");
        assertThat(multiTypesFixture.accessorFixture.getField2()).isEqualTo("field22");
    }

    @Test
    public void testMultiTypesObjectArray() {
        MultiTypesFixture multiTypesFixture = multiTypesMapper.map(this.multiTypesFixture);
        assertThat(multiTypesFixture.fixtureArray).hasSize(2);
        assertThat(multiTypesFixture.fixtureArray[0].getField1()).isEqualTo("field1");
        assertThat(multiTypesFixture.fixtureArray[1].getField2()).isEqualTo("field22");
    }

    @Test
    public void testMultiTypesObjectList() {
        MultiTypesFixture multiTypesFixture = multiTypesMapper.map(this.multiTypesFixture);
        assertThat(multiTypesFixture.fixtureArray).hasSize(2);
        assertThat(multiTypesFixture.fixtureList.get(0).getField1()).isEqualTo("field1");
        assertThat(multiTypesFixture.fixtureList.get(1).getField2()).isEqualTo("field22");
    }

    @Test
    public void testMultiTypesObjectSet() {
        MultiTypesFixture multiTypesFixture = multiTypesMapper.map(this.multiTypesFixture);
        assertThat(multiTypesFixture.fixtureArray).hasSize(2);
        assertThat(multiTypesFixture.fixtureSet.iterator().next().getField1()).isEqualTo("field1");
        assertThat(multiTypesFixture.fixtureSet.iterator().next().getField2()).isEqualTo("field22");
    }

    @Test
    public void testMultiTypesMap() {
        MultiTypesFixture multiTypesFixture = multiTypesMapper.map(this.multiTypesFixture);
        assertThat(multiTypesFixture.aMap).containsOnly(entry(1,true),entry(2,false));
    }
}
