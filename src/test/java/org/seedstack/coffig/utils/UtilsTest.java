/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.utils;

import org.junit.Test;
import org.seedstack.coffig.util.Utils;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {
    @Test
    public void instantiatePrimitiveTypes() throws Exception {
        assertThat(Utils.instantiateDefault(int.class)).isEqualTo(0);
        assertThat(Utils.instantiateDefault(boolean.class)).isFalse();
        assertThat(Utils.instantiateDefault(int.class)).isEqualTo(0);
        assertThat(Utils.instantiateDefault(long.class)).isEqualTo(0L);
        assertThat(Utils.instantiateDefault(short.class)).isEqualTo((short) 0);
        assertThat(Utils.instantiateDefault(float.class)).isEqualTo(0f);
        assertThat(Utils.instantiateDefault(double.class)).isEqualTo(0d);
        assertThat(Utils.instantiateDefault(byte.class)).isEqualTo((byte) 0);
        assertThat(Utils.instantiateDefault(char.class)).isEqualTo((char) 0);
    }

    @Test
    public void instantiateBoxedTypes() throws Exception {
        assertThat(Utils.instantiateDefault(Integer.class)).isEqualTo(0);
        assertThat(Utils.instantiateDefault(Boolean.class)).isFalse();
        assertThat(Utils.instantiateDefault(Integer.class)).isEqualTo(0);
        assertThat(Utils.instantiateDefault(Long.class)).isEqualTo(0L);
        assertThat(Utils.instantiateDefault(Short.class)).isEqualTo((short) 0);
        assertThat(Utils.instantiateDefault(Float.class)).isEqualTo(0f);
        assertThat(Utils.instantiateDefault(Double.class)).isEqualTo(0d);
        assertThat(Utils.instantiateDefault(Byte.class)).isEqualTo((byte) 0);
        assertThat(Utils.instantiateDefault(Character.class)).isEqualTo((char) 0);
    }

    @Test
    public void instantiateArrays() throws Exception {
        assertThat(Utils.instantiateDefault(Integer[].class)).isEqualTo(new Integer[0]);
        assertThat(Utils.instantiateDefault(int[].class)).isEqualTo(new int[0]);
        assertThat(Utils.instantiateDefault(String[].class)).isEqualTo(new String[0]);
    }
}
