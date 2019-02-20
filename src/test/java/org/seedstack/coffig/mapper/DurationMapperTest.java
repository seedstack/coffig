/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
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

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DurationMapperTest {
    private static final Duration REFERENCE = Duration.ofSeconds(20, 345 * 1000 * 1000);
    private ConfigurationMapper mapper = Coffig.basic().getMapper();

    @Test
    public void testMapDuration() throws NoSuchFieldException {
        assertThat((Duration) (mapper.map(new ValueNode("PT20.345S"), Duration.class))).isEqualTo(REFERENCE);
    }

    @Test
    public void testUnmapDuration() throws Exception {
        assertThat(mapper.unmap(REFERENCE, Duration.class)).isEqualTo(new ValueNode("PT20.345S"));
    }
}
