/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.junit.Test;
import org.seedstack.coffig.node.ArrayNode;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrayConfigurationMapperTest {
    private ArrayConfigurationMapper arrayConfigurationMapper = new ArrayConfigurationMapper(new MapperFactory());

    @Test
    public void testMapArrays() {
        assertThat((Boolean[]) arrayConfigurationMapper.map(new ArrayNode("true", "true"), Boolean[].class)).containsOnly(true, true);
        assertThat((boolean[]) arrayConfigurationMapper.map(new ArrayNode("false", "false"), boolean[].class)).containsOnly(false, false);
        assertThat((Byte[]) arrayConfigurationMapper.map(new ArrayNode("101", "101"), Byte[].class)).containsOnly((byte) 101, (byte) 101);
        assertThat((byte[]) arrayConfigurationMapper.map(new ArrayNode("101", "101"), byte[].class)).containsOnly((byte) 101, (byte) 101);
        assertThat((Character[]) arrayConfigurationMapper.map(new ArrayNode("A", "A"), Character[].class)).containsOnly('A', 'A');
        assertThat((char[]) arrayConfigurationMapper.map(new ArrayNode("A", "A"), char[].class)).containsOnly('A', 'A');
        assertThat((Double[]) arrayConfigurationMapper.map(new ArrayNode("3.14", "3.14"), Double[].class)).containsOnly(3.14d, 3.14d);
        assertThat((double[]) arrayConfigurationMapper.map(new ArrayNode("3.14", "3.14"), double[].class)).containsOnly(3.14d, 3.14d);
        assertThat((Float[]) arrayConfigurationMapper.map(new ArrayNode("3.14", "3.14"), Float[].class)).containsOnly(3.14f, 3.14f);
        assertThat((float[]) arrayConfigurationMapper.map(new ArrayNode("3.14", "3.14"), float[].class)).containsOnly(3.14f, 3.14f);
        assertThat((Integer[]) arrayConfigurationMapper.map(new ArrayNode("3", "3"), Integer[].class)).containsOnly(3, 3);
        assertThat((int[]) arrayConfigurationMapper.map(new ArrayNode("3", "3"), int[].class)).containsOnly(3, 3);
        assertThat((Long[]) arrayConfigurationMapper.map(new ArrayNode("3", "3"), Long[].class)).containsOnly(3L, 3L);
        assertThat((long[]) arrayConfigurationMapper.map(new ArrayNode("3", "3"), long[].class)).containsOnly(3L, 3L);
        assertThat((Short[]) arrayConfigurationMapper.map(new ArrayNode("3", "3"), Short[].class)).containsOnly((short) 3, (short) 3);
        assertThat((short[]) arrayConfigurationMapper.map(new ArrayNode("3", "3"), short[].class)).containsOnly((short) 3, (short) 3);
        assertThat((String[]) arrayConfigurationMapper.map(new ArrayNode("abcd", "abcd"), String[].class)).containsOnly("abcd", "abcd");
    }
}
