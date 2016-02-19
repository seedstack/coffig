/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.configuration.provider;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.configuration.data.MapNode;

import java.util.Properties;

@RunWith(JMockit.class)
public class SystemPropertyProviderTest {

    private SystemPropertyProvider underTest = new SystemPropertyProvider();

    @Test
    public void testProvide() {
        new MockUp<System>() {
            @Mock
            Properties getProperties() {
                Properties properties = new Properties();
                properties.put("PROFILE", "DEV");
                return properties;
            }
        };

        MapNode conf = underTest.provide();

        Assertions.assertThat(conf.value("PROFILE").value()).isEqualTo("DEV");
    }

    @Test
    public void testProvideEmptyMap() {
        new MockUp<System>() {
            @Mock
            Properties getProperties() {
                return new Properties();
            }
        };

        MapNode conf = underTest.provide();

        Assertions.assertThat(conf).isNotNull();
    }
}
