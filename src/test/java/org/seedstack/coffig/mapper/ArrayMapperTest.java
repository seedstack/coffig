/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
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
        assertThat((Boolean[]) mapper.map(new ArrayNode("true", "true"), Boolean[].class)).containsExactly(true, true);
        assertThat((boolean[]) mapper.map(new ArrayNode("false", "false"), boolean[].class)).containsExactly(false, false);
        assertThat((Byte[]) mapper.map(new ArrayNode("101", "101"), Byte[].class)).containsExactly((byte) 101, (byte) 101);
        assertThat((byte[]) mapper.map(new ArrayNode("101", "101"), byte[].class)).containsExactly((byte) 101, (byte) 101);
        assertThat((Character[]) mapper.map(new ArrayNode("A", "A"), Character[].class)).containsExactly('A', 'A');
        assertThat((char[]) mapper.map(new ArrayNode("A", "A"), char[].class)).containsExactly('A', 'A');
        assertThat((Double[]) mapper.map(new ArrayNode("3.14", "3.14"), Double[].class)).containsExactly(3.14d, 3.14d);
        assertThat((double[]) mapper.map(new ArrayNode("3.14", "3.14"), double[].class)).containsExactly(3.14d, 3.14d);
        assertThat((Float[]) mapper.map(new ArrayNode("3.14", "3.14"), Float[].class)).containsExactly(3.14f, 3.14f);
        assertThat((float[]) mapper.map(new ArrayNode("3.14", "3.14"), float[].class)).containsExactly(3.14f, 3.14f);
        assertThat((Integer[]) mapper.map(new ArrayNode("3", "3"), Integer[].class)).containsExactly(3, 3);
        assertThat((int[]) mapper.map(new ArrayNode("3", "3"), int[].class)).containsExactly(3, 3);
        assertThat((Long[]) mapper.map(new ArrayNode("3", "3"), Long[].class)).containsExactly(3L, 3L);
        assertThat((long[]) mapper.map(new ArrayNode("3", "3"), long[].class)).containsExactly(3L, 3L);
        assertThat((Short[]) mapper.map(new ArrayNode("3", "3"), Short[].class)).containsExactly((short) 3, (short) 3);
        assertThat((short[]) mapper.map(new ArrayNode("3", "3"), short[].class)).containsExactly((short) 3, (short) 3);
        assertThat((String[]) mapper.map(new ArrayNode("abcd", "abcd"), String[].class)).containsExactly("abcd", "abcd");
        assertThat((Class<? extends Number>[]) mapper.map(new ArrayNode("java.lang.Double", "java.lang.Integer"), Class[].class)).containsExactly(Double.class, Integer.class);
    }
}
