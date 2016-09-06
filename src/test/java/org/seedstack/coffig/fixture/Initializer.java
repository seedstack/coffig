/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.fixture;

import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.spi.ConfigurationComponent;

public final class Initializer {
    private Initializer() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends ConfigurationComponent> T initialize(ConfigurationComponent configurationComponent) {
        configurationComponent.initialize(Coffig.builder().withDefaultMapper().build());
        return (T) configurationComponent;
    }
}
