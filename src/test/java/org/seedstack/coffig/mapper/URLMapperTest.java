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
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class URLMapperTest {
    private ConfigurationMapper mapper = Coffig.basic().getMapper();

    @Test
    public void testMapEnum() {
        URL url = (URL) mapper.map(new ValueNode("http://subdomain.domain.tld:8080/path"), URL.class);
        assertThat(url.getProtocol()).isEqualTo("http");
        assertThat(url.getHost()).isEqualTo("subdomain.domain.tld");
        assertThat(url.getPort()).isEqualTo(8080);
        assertThat(url.getPath()).isEqualTo("/path");
    }

    @Test
    public void testUnmapEnum() throws MalformedURLException {
        assertThat(mapper.unmap(new URL("http://subdomain.domain.tld:8080/path"), URL.class)).isEqualTo(new ValueNode("http://subdomain.domain.tld:8080/path"));
    }
}
