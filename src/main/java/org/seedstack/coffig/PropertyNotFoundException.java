/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

public class PropertyNotFoundException extends ConfigurationException {
    private final String propertyName;
    private String highlightedName;

    public PropertyNotFoundException(String propertyName, Throwable cause) {
        super("Property not found: " + propertyName, cause);
        this.propertyName = propertyName;
        this.highlightedName = "<" + propertyName + ">";
    }

    public PropertyNotFoundException(String propertyName) {
        this(propertyName, null);
    }

    public PropertyNotFoundException(PropertyNotFoundException e, String name) {
        super("Sub-property not found: " + name + "." + e.highlightedName);
        this.propertyName = name + "." + e.propertyName;
        this.highlightedName = name + "." + e.highlightedName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
