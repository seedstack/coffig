/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import org.seedstack.coffig.spi.ConfigurationProvider;
import org.seedstack.coffig.data.MapNode;
import org.seedstack.coffig.data.PairNode;

public class SystemPropertyProvider implements ConfigurationProvider {

    @Override
    public MapNode provide() {
        return new MapNode(System.getProperties().entrySet().stream()
                .map(e -> new PairNode((String) e.getKey(), (String) e.getValue()))
                .toArray(PairNode[]::new));
    }
}
