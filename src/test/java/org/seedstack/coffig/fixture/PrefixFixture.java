/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.fixture;

import org.seedstack.coffig.Config;

@Config("foo.bar")
public class PrefixFixture {
    public String aString;
    @Config("baz")
    public AccessorFixture accessorFixture;
    public InnerClass innerClass;

    public PrefixFixture() {
    }

    public PrefixFixture(String aString) {
        this.aString = aString;
    }

    @Config("qux")
    public static class InnerClass {
        public String innerField;
    }
}
