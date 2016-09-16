/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.junit.Test;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.node.ArrayNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrayMapperTest {
    private ConfigurationMapper mapper = Coffig.basic().getMapper();

    @Test
    public void testMapArrays() {
        assertThat((Boolean[]) mapper.map(new ArrayNode("true", "true"), Boolean[].class)).containsOnly(true, true);
        assertThat((boolean[]) mapper.map(new ArrayNode("false", "false"), boolean[].class)).containsOnly(false, false);
        assertThat((Byte[]) mapper.map(new ArrayNode("101", "101"), Byte[].class)).containsOnly((byte) 101, (byte) 101);
        assertThat((byte[]) mapper.map(new ArrayNode("101", "101"), byte[].class)).containsOnly((byte) 101, (byte) 101);
        assertThat((Character[]) mapper.map(new ArrayNode("A", "A"), Character[].class)).containsOnly('A', 'A');
        assertThat((char[]) mapper.map(new ArrayNode("A", "A"), char[].class)).containsOnly('A', 'A');
        assertThat((Double[]) mapper.map(new ArrayNode("3.14", "3.14"), Double[].class)).containsOnly(3.14d, 3.14d);
        assertThat((double[]) mapper.map(new ArrayNode("3.14", "3.14"), double[].class)).containsOnly(3.14d, 3.14d);
        assertThat((Float[]) mapper.map(new ArrayNode("3.14", "3.14"), Float[].class)).containsOnly(3.14f, 3.14f);
        assertThat((float[]) mapper.map(new ArrayNode("3.14", "3.14"), float[].class)).containsOnly(3.14f, 3.14f);
        assertThat((Integer[]) mapper.map(new ArrayNode("3", "3"), Integer[].class)).containsOnly(3, 3);
        assertThat((int[]) mapper.map(new ArrayNode("3", "3"), int[].class)).containsOnly(3, 3);
        assertThat((Long[]) mapper.map(new ArrayNode("3", "3"), Long[].class)).containsOnly(3L, 3L);
        assertThat((long[]) mapper.map(new ArrayNode("3", "3"), long[].class)).containsOnly(3L, 3L);
        assertThat((Short[]) mapper.map(new ArrayNode("3", "3"), Short[].class)).containsOnly((short) 3, (short) 3);
        assertThat((short[]) mapper.map(new ArrayNode("3", "3"), short[].class)).containsOnly((short) 3, (short) 3);
        assertThat((String[]) mapper.map(new ArrayNode("abcd", "abcd"), String[].class)).containsOnly("abcd", "abcd");
    }
}
