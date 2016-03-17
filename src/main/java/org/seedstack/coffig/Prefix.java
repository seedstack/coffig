/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import java.util.Optional;

/**
 * A prefix is a configuration tree path. It splits the path in two part.
 * The head and the tail, for instance the following prefix:
 *
 * <h2>Prefix with tail</h2>
 *
 * <pre>
 *     app.server.port
 * </pre>
 * will give:
 * <pre>
 *     prefix.head == "app"
 *     prefix.tail == Optional.of("server.port")
 *     prefix.index == -1
 *     prefix.isArray == false
 * </pre>
 *
 * <h2>Prefix without tail</h2>
 *
 * <pre>
 *     app
 * </pre>
 * will give:
 * <pre>
 *     prefix.head == "app"
 *     prefix.tail == Optional.empty()
 *     prefix.index == -1
 *     prefix.isArray == false
 * </pre>
 *
 * <h2>Prefix represents an array</h2>
 *
 * if the head correspond to an integer the index will be initialize.
 * <pre>
 *     0.custom.key
 * </pre>
 * will give:
 * <pre>
 *     prefix.head == "0"
 *     prefix.tail == Optional.of("custom.key")
 *     prefix.index == 0
 *     prefix.isArray == true
 * </pre>
 */
class Prefix {
    final String head;
    final Optional<String> tail;
    final int index;
    final boolean isArray;

    /**
     * Constructs a prefix based on a string.
     *
     * @param prefix the configuration path
     */
    public Prefix(String prefix) {
        String[] splitPrefix = prefix.split("\\.", 2);
        head = splitPrefix[0];

        if (splitPrefix.length == 2) {
            tail = Optional.of(splitPrefix[1]);
        } else {
            tail = Optional.empty();
        }

        boolean headIsInteger;
        int parseInt = -1;
        try {
            parseInt = Integer.valueOf(head);
            if (parseInt >= 0) {
                headIsInteger = true;
            } else {
                throw new ArrayIndexOutOfBoundsException();
            }
        } catch (NumberFormatException e) {
            headIsInteger = false;
        }
        isArray = headIsInteger;
        index = parseInt;
    }
}