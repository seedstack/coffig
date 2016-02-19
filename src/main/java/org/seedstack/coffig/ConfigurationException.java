/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import java.util.function.BiFunction;

public class ConfigurationException extends RuntimeException {

    public static final BiFunction<Class<?>,Class<?>, String> INCORRECT_MERGE =
            (c1, c2) -> String.format("Attempt to merge a %s with a %s", c1.getCanonicalName(), c2.getCanonicalName());

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}
