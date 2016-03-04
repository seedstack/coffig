/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.provider;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.coffig.MapNode;

import java.util.HashMap;
import java.util.Map;

@RunWith(JMockit.class)
public class EnvironmentVariableProviderTest {

    private EnvironmentVariableProvider underTest = new EnvironmentVariableProvider();

    @Test
    public void testProvide() {
        new MockUp<System>() {
            @Mock
            java.util.Map<String,String> getenv() {
                Map<String, String> env = new HashMap<>();
                env.put("PROFILE", "DEV");
                return env;
            }
        };

        MapNode conf = underTest.provide();

        Assertions.assertThat(conf.get("env.PROFILE").value()).isEqualTo("DEV");
    }

    @Test
    public void testProvideEmptyMap() {
        new MockUp<System>() {
            @Mock
            java.util.Map<String,String> getenv() {
                return new HashMap<>();
            }
        };

        MapNode conf = underTest.provide();

        Assertions.assertThat(conf).isNotNull();
    }
}
