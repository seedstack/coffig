/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.data;

import java.util.Optional;

class Prefix {
    String head;
    Optional<String> tail;
    int index;
    boolean isArray;

    public Prefix(String prefix) {
        String[] splitPrefix = prefix.split("\\.", 2);
        head = splitPrefix[0];
        setTail(splitPrefix);
        setIndex();
    }

    private void setTail(String[] splitPrefix) {
        if (splitPrefix.length == 2) {
            tail = Optional.of(splitPrefix[1]);
        } else {
            tail = Optional.empty();
        }
    }

    private void setIndex() {
        try {
            index = Integer.valueOf(head);
            if (index >= 0) {
                isArray = true;
            } else {
                throw new ArrayIndexOutOfBoundsException();
            }
        } catch (NumberFormatException e) {
            isArray = false;
        }
    }
}