/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.junit.Test;
import org.seedstack.coffig.ValueNode;

import static org.assertj.core.api.Assertions.assertThat;

public class ValueConfigurationMapperTest {

    private ValueConfigurationMapper valueConverter = new ValueConfigurationMapper();

    @Test
    public void testMapFromNull() {
        assertThat(valueConverter.map(null, Boolean.class)).isNull();
        assertThat(valueConverter.map(null, Byte.class)).isNull();
        assertThat(valueConverter.map(null, Character.class)).isNull();
        assertThat(valueConverter.map(null, Double.class)).isNull();
        assertThat(valueConverter.map(null, Float.class)).isNull();
        assertThat(valueConverter.map(null, Integer.class)).isNull();
        assertThat(valueConverter.map(null, Long.class)).isNull();
        assertThat(valueConverter.map(null, Short.class)).isNull();
        assertThat(valueConverter.map(null, String.class)).isNull();
    }

    @Test
    public void testMapValues() {
        assertThat(valueConverter.map(new ValueNode("true"), Boolean.class)).isEqualTo(true);
        assertThat(valueConverter.map(new ValueNode("false"), boolean.class)).isEqualTo(false);
        assertThat(valueConverter.map(new ValueNode("101"), Byte.class)).isEqualTo((byte) 101);
        assertThat(valueConverter.map(new ValueNode("101"), byte.class)).isEqualTo((byte) 101);
        assertThat(valueConverter.map(new ValueNode("A"), Character.class)).isEqualTo('A');
        assertThat(valueConverter.map(new ValueNode("A"), char.class)).isEqualTo('A');
        assertThat(valueConverter.map(new ValueNode("3.14"), Double.class)).isEqualTo(3.14d);
        assertThat(valueConverter.map(new ValueNode("3.14"), double.class)).isEqualTo(3.14d);
        assertThat(valueConverter.map(new ValueNode("3.14"), Float.class)).isEqualTo(3.14f);
        assertThat(valueConverter.map(new ValueNode("3.14"), float.class)).isEqualTo(3.14f);
        assertThat(valueConverter.map(new ValueNode("3"), Integer.class)).isEqualTo(3);
        assertThat(valueConverter.map(new ValueNode("3"), int.class)).isEqualTo(3);
        assertThat(valueConverter.map(new ValueNode("3"), Long.class)).isEqualTo(3L);
        assertThat(valueConverter.map(new ValueNode("3"), long.class)).isEqualTo(3L);
        assertThat(valueConverter.map(new ValueNode("3"), Short.class)).isEqualTo((short) 3);
        assertThat(valueConverter.map(new ValueNode("3"), short.class)).isEqualTo((short) 3);
        assertThat(valueConverter.map(new ValueNode("abcd"), String.class)).isEqualTo("abcd");
    }
}
