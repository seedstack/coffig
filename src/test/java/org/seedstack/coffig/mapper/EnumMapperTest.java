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
import org.seedstack.coffig.fixture.EnumFixture;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumMapperTest {
    private ConfigurationMapper mapper = Coffig.builder().withMappers(new EnumMapper()).build().getMapper();

    @Test
    public void testMapEnum() {
        assertThat(mapper.map(new ValueNode("FOO"), EnumFixture.class)).isEqualTo(EnumFixture.FOO);
        assertThat(mapper.map(new ValueNode("BAR"), EnumFixture.class)).isEqualTo(EnumFixture.BAR);
    }

    @Test
    public void testUnmapEnum() {
        assertThat(mapper.unmap(EnumFixture.FOO, EnumFixture.class)).isEqualTo(new ValueNode("FOO"));
        assertThat(mapper.unmap(EnumFixture.BAR, EnumFixture.class)).isEqualTo(new ValueNode("BAR"));
    }
}
