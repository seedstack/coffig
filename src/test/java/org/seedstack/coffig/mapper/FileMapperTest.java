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

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class FileMapperTest {
    private ConfigurationMapper mapper = Coffig.basic().getMapper();

    @Test
    public void testMapEnum() {
        String tmpDirectory = System.getProperty("java.io.tmpdir");
        assertThat((File) mapper.map(new ValueNode(tmpDirectory), File.class)).isEqualTo(new File(tmpDirectory));
    }

    @Test
    public void testUnmapEnum() {
        String tmpDirectory = System.getProperty("java.io.tmpdir");
        assertThat(mapper.unmap(new File(tmpDirectory), File.class)).isEqualTo(new ValueNode(new File(tmpDirectory).getPath()));
    }
}
