/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.internal;

public class PropertyNotFoundException extends ConfigurationException {
    private final String propertyName;
    private String highlightedName;

    public PropertyNotFoundException(String propertyName, Throwable cause) {
        super(ConfigurationErrorCode.PROPERTY_NOT_FOUND, cause);
        this.propertyName = propertyName;
        this.highlightedName = "<" + propertyName + ">";
        put("property", highlightedName);
    }

    public PropertyNotFoundException(String propertyName) {
        this(propertyName, null);
    }

    public PropertyNotFoundException(PropertyNotFoundException e, String name) {
        super(ConfigurationErrorCode.PROPERTY_NOT_FOUND);
        this.propertyName = name + "." + e.propertyName;
        this.highlightedName = name + "." + e.highlightedName;
        put("property", this.highlightedName);
    }

    public String getPropertyName() {
        return propertyName;
    }
}
