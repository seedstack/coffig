/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.spi.ConfigurationComponent;
import org.seedstack.coffig.spi.ConfigurationProvider;

public class PrefixProvider<T extends ConfigurationProvider> implements ConfigurationProvider {
    private final String prefix;
    private final T configurationProvider;

    public PrefixProvider(String prefix, T configurationProvider) {
        this.prefix = prefix;
        this.configurationProvider = configurationProvider;
    }

    @Override
    public MapNode provide() {
        MapNode result = new MapNode();
        result.set(prefix, configurationProvider.provide());
        return result;
    }

    @Override
    public void initialize(Coffig coffig) {
        configurationProvider.initialize(coffig);
    }

    @Override
    public void invalidate() {
        configurationProvider.invalidate();
    }

    @Override
    public boolean isDirty() {
        return configurationProvider.isDirty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigurationComponent fork() {
        return new PrefixProvider<>(prefix, (T) configurationProvider.fork());
    }
}
