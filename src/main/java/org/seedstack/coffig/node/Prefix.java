/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern SUBSCRIPTION_PATTERN = Pattern.compile("(.*)\\[(\\d+)\\]");

    private final String head;
    private final String tail;
    private final int index;

    /**
     * Constructs a prefix based on a string.
     *
     * @param prefix the configuration path
     */
    Prefix(String prefix) {
        String[] splitPrefix = prefix.split("\\.", 2);
        Matcher matcher = SUBSCRIPTION_PATTERN.matcher(splitPrefix[0]);

        if (matcher.matches()) {
            if (matcher.group(1).isEmpty()) {
                head = matcher.group(2);
                tail = splitPrefix.length > 1 ? splitPrefix[1] : null;
                index = Integer.parseInt(head);
            } else {
                head = matcher.group(1);
                tail = String.format("[%s]", matcher.group(2)) + (splitPrefix.length > 1 ? "." + splitPrefix[1] : "");
                index = -1;
            }
        } else {
            head = splitPrefix[0];
            tail = splitPrefix.length > 1 ? splitPrefix[1] : null;
            index = -1;
        }
    }

    boolean hasHead() {
        return head != null && !head.isEmpty();
    }

    String getHead() {
        if (!hasHead()) {
            throw new IllegalStateException("Prefix does not have a head");
        }
        return head;
    }

    boolean hasTail() {
        return tail != null && !tail.isEmpty();
    }

    String getTail() {
        if (!hasTail()) {
            throw new IllegalStateException("Prefix does not have a tail");
        }
        return tail;
    }

    boolean isArray() {
        return index != -1;
    }

    int getIndex() {
        if (!isArray()) {
            throw new IllegalStateException("Prefix does not denote an array");
        }
        return index;
    }
}