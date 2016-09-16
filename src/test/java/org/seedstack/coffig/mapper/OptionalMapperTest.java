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
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalMapperTest {
    private ConfigurationMapper mapper = Coffig.basic().getMapper();

    private static class Fixture {
        private Optional<String> optionalString = Optional.empty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMapOptional() throws NoSuchFieldException {
        Fixture result1 = (Fixture) mapper.map(new MapNode(new NamedNode("optionalString", "Hello")), Fixture.class);
        assertThat(result1).isNotNull();
        assertThat(result1.optionalString.isPresent()).isTrue();
        assertThat(result1.optionalString.get()).isEqualTo("Hello");

        Fixture result2 = (Fixture) mapper.map(new MapNode(), Fixture.class);
        assertThat(result2).isNotNull();
        assertThat(result2.optionalString.isPresent()).isFalse();
    }

    @Test
    public void testUnmapOptional() {
    }
}
