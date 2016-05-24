/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import org.seedstack.coffig.MapNode;
import org.seedstack.coffig.NamedNode;
import org.seedstack.coffig.spi.ConfigurationProvider;

public class EnvironmentVariableProvider implements ConfigurationProvider {
    @Override
    public MapNode provide() {
        return new MapNode(new NamedNode("env", new MapNode(System.getenv().entrySet().stream()
                .map(e -> new NamedNode(e.getKey(), e.getValue()))
                .toArray(NamedNode[]::new))));
    }
}
