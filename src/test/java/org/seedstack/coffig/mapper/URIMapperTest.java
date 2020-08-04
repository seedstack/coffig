/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
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

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class URIMapperTest {
    private ConfigurationMapper mapper = Coffig.basic().getMapper();

    @Test
    public void testMapEnum() {
        URI uri = (URI) mapper.map(new ValueNode("mailto:account@domain.tld"), URI.class);
        assertThat(uri.getScheme()).isEqualTo("mailto");
        assertThat(uri.getSchemeSpecificPart()).isEqualTo("account@domain.tld");
    }

    @Test
    public void testUnmapEnum() throws URISyntaxException {
        assertThat(mapper.unmap(new URI("mailto:account@domain.tld"), URI.class)).isEqualTo(new ValueNode("mailto:account@domain.tld"));
    }
}
