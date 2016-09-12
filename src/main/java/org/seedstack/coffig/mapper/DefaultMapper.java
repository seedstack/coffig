/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.spi.ConfigurationMapper;

public class DefaultMapper extends CompositeMapper {
    public DefaultMapper() {
        super(
                new EnumMapper(),
                new ValueMapper(),
                new ArrayMapper(),
                new CollectionMapper(),
                new MapMapper(),
                new FileMapper()
        );
    }

    @Override
    protected DefaultMapper doFork(ConfigurationMapper... items) {
        return new DefaultMapper();
    }
}
