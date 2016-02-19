/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.configuration;

import java.util.function.Function;

public class PropertyNotFoundException extends ConfigurationException {

    private String name;
    private static Function<String, String> messager = name -> String.format("Property \"%s\" was not found", name);

    public PropertyNotFoundException(String name) {
        super(messager.apply(name));
        this.name = name;
    }

    public PropertyNotFoundException(Throwable cause, String name) {
        super(cause);
        this.name = name;
    }

    @Override
    public String getMessage() {
        return messager.apply(name);
    }

    public String getPropertyName() {
        return name;
    }
}
