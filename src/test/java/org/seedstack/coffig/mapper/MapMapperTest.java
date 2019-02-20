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
import org.seedstack.coffig.fixture.EnumFixture;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MapMapperTest {
    private ConfigurationMapper mapper = Coffig.basic().getMapper();
    private Map<String, Integer> map1 = new HashMap<String, Integer>() {{
        put("key1", 1);
        put("key2", 2);
    }};
    private Map<String, Boolean> map2 = new HashMap<String, Boolean>() {{
        put("key1", true);
        put("key2", false);
    }};
    private Map<String, EnumFixture> map3 = new HashMap<String, EnumFixture>() {{
        put("key1", EnumFixture.BAR);
        put("key2", EnumFixture.FOO);
    }};

    @Test
    public void testMapMap() {
        assertThat(mapper.map(new MapNode(new NamedNode("key1", "1"), new NamedNode("key2", "2")), map1.getClass().getGenericSuperclass())).isEqualTo(map1);
        assertThat(mapper.map(new MapNode(new NamedNode("key1", "true"), new NamedNode("key2", "false")), map2.getClass().getGenericSuperclass())).isEqualTo(map2);
        assertThat(mapper.map(new MapNode(new NamedNode("key1", "BAR"), new NamedNode("key2", "FOO")), map3.getClass().getGenericSuperclass())).isEqualTo(map3);
    }
}
