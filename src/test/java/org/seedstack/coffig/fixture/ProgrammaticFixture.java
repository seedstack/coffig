/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.fixture;

import org.seedstack.coffig.Config;

public class ProgrammaticFixture {
    @Config
    private PrefixFixture providePrefixFixture() {
        return new PrefixFixture("provided");
    }

    @Config("overridden")
    private PrefixFixture provideOverriddenPrefixFixture() {
        return new PrefixFixture("provided");
    }
}
