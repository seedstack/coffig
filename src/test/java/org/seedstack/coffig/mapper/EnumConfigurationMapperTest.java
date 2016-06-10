/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.junit.Test;
import org.seedstack.coffig.fixture.SomeEnum;
import org.seedstack.coffig.node.ValueNode;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumConfigurationMapperTest {
    private EnumConfigurationMapper enumConfigurationMapper = new EnumConfigurationMapper();

    @Test
    public void testMapEnum() {
        assertThat(enumConfigurationMapper.map(new ValueNode("FOO"), SomeEnum.class)).isEqualTo(SomeEnum.FOO);
        assertThat(enumConfigurationMapper.map(new ValueNode("BAR"), SomeEnum.class)).isEqualTo(SomeEnum.BAR);
    }

    @Test
    public void testUnmapEnum() {
        assertThat(enumConfigurationMapper.unmap(SomeEnum.FOO, SomeEnum.class)).isEqualTo(new ValueNode("FOO"));
        assertThat(enumConfigurationMapper.unmap(SomeEnum.BAR, SomeEnum.class)).isEqualTo(new ValueNode("BAR"));
    }
}
