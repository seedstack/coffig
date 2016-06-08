/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.junit.Test;
import org.seedstack.coffig.MapNode;
import org.seedstack.coffig.NamedNode;
import org.seedstack.coffig.fixture.SomeEnum;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MapConfigurationMapperTest {
    private MapConfigurationMapper mapConfigurationMapper = new MapConfigurationMapper(new MapperFactory());
    private Map<String, Integer> map1 = new HashMap<String, Integer>() {{
        put("key1", 1);
        put("key2", 2);
    }};
    private Map<String, Boolean> map2 = new HashMap<String, Boolean>() {{
        put("key1", true);
        put("key2", false);
    }};
    private Map<String, SomeEnum> map3 = new HashMap<String, SomeEnum>() {{
        put("key1", SomeEnum.BAR);
        put("key2", SomeEnum.FOO);
    }};

    @Test
    public void testMapMap() {
        assertThat(mapConfigurationMapper.map(new MapNode(new NamedNode("key1", "1"), new NamedNode("key2", "2")), map1.getClass().getGenericSuperclass())).isEqualTo(map1);
        assertThat(mapConfigurationMapper.map(new MapNode(new NamedNode("key1", "true"), new NamedNode("key2", "false")), map2.getClass().getGenericSuperclass())).isEqualTo(map2);
        assertThat(mapConfigurationMapper.map(new MapNode(new NamedNode("key1", "BAR"), new NamedNode("key2", "FOO")), map3.getClass().getGenericSuperclass())).isEqualTo(map3);
    }
}
