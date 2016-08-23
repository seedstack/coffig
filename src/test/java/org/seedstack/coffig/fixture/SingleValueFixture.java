/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.fixture;

import org.seedstack.coffig.SingleValue;

public class SingleValueFixture {
    private InnerFixture innerFixture;

    public InnerFixture getInnerFixture() {
        return innerFixture;
    }

    public static class InnerFixture {
        @SingleValue
        private boolean enabled;
        private int value = 5;

        public boolean isEnabled() {
            return enabled;
        }

        public int getValue() {
            return value;
        }
    }
}
