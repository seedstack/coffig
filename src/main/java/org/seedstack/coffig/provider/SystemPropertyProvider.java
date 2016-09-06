/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.util.Properties;

public class SystemPropertyProvider implements ConfigurationProvider {
    private Properties latestSystemProperties;

    @Override
    public MapNode provide() {
        fetchSystemProperties();
        return new MapNode(new NamedNode("sys", new MapNode(latestSystemProperties.entrySet().stream()
                .map(e -> new NamedNode((String) e.getKey(), (String) e.getValue()))
                .toArray(NamedNode[]::new))));
    }

    @Override
    public boolean isDirty() {
        Properties previousSystemProperties = latestSystemProperties;
        fetchSystemProperties();
        return !latestSystemProperties.equals(previousSystemProperties);
    }

    private void fetchSystemProperties() {
        latestSystemProperties = System.getProperties();
    }
}
