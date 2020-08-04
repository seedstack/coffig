/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.spi.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemPropertiesProvider implements ConfigurationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemPropertiesProvider.class);

    @Override
    public MapNode provide() {
        LOGGER.debug("Reading configuration from system properties");
        return new MapNode(System.getProperties().entrySet().stream()
                .map(e -> new NamedNode((String) e.getKey(), (String) e.getValue()))
                .toArray(NamedNode[]::new));
    }
}
