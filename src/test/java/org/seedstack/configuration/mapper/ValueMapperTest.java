/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.configuration.mapper;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValueMapperTest {

    private ValueMapper valueConverter = new ValueMapper();

    @Test
    public void testConvertFromNull() {
        assertThat(valueConverter.convertObject(null, Boolean.class)).isNull();
        assertThat(valueConverter.convertObject(null, Byte.class)).isNull();
        assertThat(valueConverter.convertObject(null, Character.class)).isNull();
        assertThat(valueConverter.convertObject(null, Double.class)).isNull();
        assertThat(valueConverter.convertObject(null, Float.class)).isNull();
        assertThat(valueConverter.convertObject(null, Integer.class)).isNull();
        assertThat(valueConverter.convertObject(null, Long.class)).isNull();
        assertThat(valueConverter.convertObject(null, Short.class)).isNull();
        assertThat(valueConverter.convertObject(null, String.class)).isNull();
    }

    @Test
    public void testConvert() {
        assertThat(valueConverter.convertObject("true", Boolean.class)).isEqualTo(true);
        assertThat(valueConverter.convertObject("false", boolean.class)).isEqualTo(false);
        assertThat(valueConverter.convertObject("101", Byte.class)).isEqualTo((byte) 101);
        assertThat(valueConverter.convertObject("101", byte.class)).isEqualTo((byte) 101);
        assertThat(valueConverter.convertObject("A", Character.class)).isEqualTo('A');
        assertThat(valueConverter.convertObject("A", char.class)).isEqualTo('A');
        assertThat(valueConverter.convertObject("3.14", Double.class)).isEqualTo(3.14d);
        assertThat(valueConverter.convertObject("3.14", double.class)).isEqualTo(3.14d);
        assertThat(valueConverter.convertObject("3.14", Float.class)).isEqualTo(3.14f);
        assertThat(valueConverter.convertObject("3.14", float.class)).isEqualTo(3.14f);
        assertThat(valueConverter.convertObject("3", Integer.class)).isEqualTo(3);
        assertThat(valueConverter.convertObject("3", int.class)).isEqualTo(3);
        assertThat(valueConverter.convertObject("3", Long.class)).isEqualTo(3L);
        assertThat(valueConverter.convertObject("3", long.class)).isEqualTo(3L);
        assertThat(valueConverter.convertObject("3", Short.class)).isEqualTo((short) 3);
        assertThat(valueConverter.convertObject("3", short.class)).isEqualTo((short) 3);
        assertThat(valueConverter.convertObject("abcd", String.class)).isEqualTo("abcd");
    }

    @Test
    public void testConvertArrays() {
        assertThat((Boolean[]) valueConverter.convertArray(new String[]{"true", "true"}, Boolean[].class)).containsOnly(true, true);
        assertThat((boolean[]) valueConverter.convertArray(new String[]{"false", "false"}, boolean[].class)).containsOnly(false, false);
        assertThat((Byte[]) valueConverter.convertArray(new String[]{"101", "101"}, Byte[].class)).containsOnly((byte) 101, (byte) 101);
        assertThat((byte[]) valueConverter.convertArray(new String[]{"101", "101"}, byte[].class)).containsOnly((byte) 101, (byte) 101);
        assertThat((Character[]) valueConverter.convertArray(new String[]{"A", "A"}, Character[].class)).containsOnly('A', 'A');
        assertThat((char[]) valueConverter.convertArray(new String[]{"A", "A"}, char[].class)).containsOnly('A', 'A');
        assertThat((Double[]) valueConverter.convertArray(new String[]{"3.14", "3.14"}, Double[].class)).containsOnly(3.14d, 3.14d);
        assertThat((double[]) valueConverter.convertArray(new String[]{"3.14", "3.14"}, double[].class)).containsOnly(3.14d, 3.14d);
        assertThat((Float[]) valueConverter.convertArray(new String[]{"3.14", "3.14"}, Float[].class)).containsOnly(3.14f, 3.14f);
        assertThat((float[]) valueConverter.convertArray(new String[]{"3.14", "3.14"}, float[].class)).containsOnly(3.14f, 3.14f);
        assertThat((Integer[]) valueConverter.convertArray(new String[]{"3", "3"}, Integer[].class)).containsOnly(3, 3);
        assertThat((int[]) valueConverter.convertArray(new String[]{"3", "3"}, int[].class)).containsOnly(3, 3);
        assertThat((Long[]) valueConverter.convertArray(new String[]{"3", "3"}, Long[].class)).containsOnly(3L, 3L);
        assertThat((long[]) valueConverter.convertArray(new String[]{"3", "3"}, long[].class)).containsOnly(3L, 3L);
        assertThat((Short[]) valueConverter.convertArray(new String[]{"3", "3"}, Short[].class)).containsOnly((short) 3, (short) 3);
        assertThat((short[]) valueConverter.convertArray(new String[]{"3", "3"}, short[].class)).containsOnly((short) 3, (short) 3);
        assertThat((String[]) valueConverter.convertArray(new String[]{"abcd", "abcd"}, String[].class)).containsOnly("abcd", "abcd");
    }
}
