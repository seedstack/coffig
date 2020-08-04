/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.fixture;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultiTypesFixture {

    public boolean aBoolean;
    public byte aByte;
    public char aChar;
    public double aDouble;
    public float aFloat;
    public int anInt;
    public long aLong;
    public short aShort;
    public String aString;

    public boolean[] someBoolean;
    public byte[] someByte;
    public char[] someChar;
    public double[] someDouble;
    public float[] someFloat;
    public int[] someInt;
    public long[] someLong;
    public short[] someShort;
    public String[] someString;

    public List<String> stringList;
    public Set<String> stringSet;

    public AccessorFixture accessorFixture;
    public Map<Integer, Boolean> aMap;
    public AccessorFixture[] fixtureArray;
    public List<AccessorFixture> fixtureList;
    public Set<AccessorFixture> fixtureSet;

}
