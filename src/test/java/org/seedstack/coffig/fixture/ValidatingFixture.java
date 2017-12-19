/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.fixture;

import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ValidatingFixture {
    @NotNull
    private String notNull;
    @Min(5)
    @Max(10)
    private int minMax;
    @Email
    private String email;

    public String getNotNull() {
        return notNull;
    }

    public int getMinMax() {
        return minMax;
    }

    public String getEmail() {
        return email;
    }
}
