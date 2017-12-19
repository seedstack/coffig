/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.mapper;

import org.junit.Test;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesMapperTest {
    private ConfigurationMapper mapper = Coffig.basic().getMapper();
    private Properties properties = new Properties() {{
        setProperty("key1", "value1");
        setProperty("key2", "value2");
    }};

    @Test
    public void testMapProperties() {
        assertThat(mapper.map(new MapNode(new NamedNode("key1", "value1"), new NamedNode("key2", "value2")), Properties.class)).isEqualTo(properties);
    }

    @Test
    public void testUnmapProperties() {
        assertThat(mapper.unmap(properties, Properties.class)).isEqualTo(new MapNode(new NamedNode("key1", "value1"), new NamedNode("key2", "value2")));
    }
}
