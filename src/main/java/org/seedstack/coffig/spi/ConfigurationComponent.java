/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.spi;

import java.util.HashSet;
import java.util.Set;
import org.seedstack.coffig.Coffig;

public interface ConfigurationComponent {
    default String name() {
        // the simple class name by default
        return this.getClass().getSimpleName();
    }

    default void initialize(Coffig coffig) {
        // nothing to do by default
    }

    default void invalidate() {
        // nothing to do by default
    }

    default boolean isDirty() {
        // consider this as an immutable component by default
        return false;
    }

    default ConfigurationComponent fork() {
        // consider this as stateless (shareable) component by default
        return this;
    }

    default Set<ConfigurationWatcher> watchers() {
        return new HashSet<>();
    }
}
