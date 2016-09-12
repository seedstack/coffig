/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

public class DefaultMapper extends CompositeMapper {
    public DefaultMapper() {
        super();
        add(new EnumMapper());
        add(new ValueMapper());
        add(new ArrayMapper());
        add(new CollectionMapper());
        add(new MapMapper());
        add(new FileMapper());
    }

    @Override
    protected DefaultMapper doFork() {
        return new DefaultMapper();
    }
}
