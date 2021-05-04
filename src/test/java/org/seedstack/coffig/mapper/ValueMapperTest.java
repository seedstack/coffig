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
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class ValueMapperTest {
    private ConfigurationMapper mapper = Coffig.basic().getMapper();

    @Test
    public void testMapValues() {
        assertThat(mapper.map(new ValueNode("true"), Boolean.class)).isEqualTo(true);
        assertThat(mapper.map(new ValueNode("false"), boolean.class)).isEqualTo(false);
        assertThat(mapper.map(new ValueNode("101"), Byte.class)).isEqualTo((byte) 101);
        assertThat(mapper.map(new ValueNode("101"), byte.class)).isEqualTo((byte) 101);
        assertThat(mapper.map(new ValueNode("A"), Character.class)).isEqualTo('A');
        assertThat(mapper.map(new ValueNode("A"), char.class)).isEqualTo('A');
        assertThat(mapper.map(new ValueNode("3.14"), Double.class)).isEqualTo(3.14d);
        assertThat(mapper.map(new ValueNode("3.14"), double.class)).isEqualTo(3.14d);
        assertThat(mapper.map(new ValueNode("3.14"), Float.class)).isEqualTo(3.14f);
        assertThat(mapper.map(new ValueNode("3.14"), float.class)).isEqualTo(3.14f);
        assertThat(mapper.map(new ValueNode("3"), Integer.class)).isEqualTo(3);
        assertThat(mapper.map(new ValueNode("3"), int.class)).isEqualTo(3);
        assertThat(mapper.map(new ValueNode("3"), Long.class)).isEqualTo(3L);
        assertThat(mapper.map(new ValueNode("3"), long.class)).isEqualTo(3L);
        assertThat(mapper.map(new ValueNode("3"), Short.class)).isEqualTo((short) 3);
        assertThat(mapper.map(new ValueNode("3"), short.class)).isEqualTo((short) 3);
        assertThat(mapper.map(new ValueNode("abcd"), String.class)).isEqualTo("abcd");
    }
}
