/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.Test;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.ConfigurationValidationException;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.fixture.ValidatingFixture;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.NamedNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import javax.validation.Validation;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidatingMapperTest {
    private ConfigurationMapper mapper = Coffig.builder()
            .enableValidation(Validation.byDefaultProvider()
                    .configure()
                    .messageInterpolator(new ParameterMessageInterpolator())
                    .buildValidatorFactory())
            .build()
            .getMapper();

    @Test
    public void testValidationOK() throws Exception {
        MapNode tree = new MapNode(
                new NamedNode("notNull", "value"),
                new NamedNode("minMax", "7"),
                new NamedNode("email", "some@email.com")
        );

        ValidatingFixture validatingFixture = (ValidatingFixture) mapper.map(tree, ValidatingFixture.class);
        assertThat(validatingFixture.getNotNull()).isEqualTo("value");
        assertThat(validatingFixture.getMinMax()).isEqualTo(7);
        assertThat(validatingFixture.getEmail()).isEqualTo("some@email.com");

        TreeNode treeNode = mapper.unmap(validatingFixture, ValidatingFixture.class);
        assertThat(treeNode.get("notNull").get()).isEqualTo(new ValueNode("value"));
        assertThat(treeNode.get("minMax").get()).isEqualTo(new ValueNode("7"));
        assertThat(treeNode.get("email").get()).isEqualTo(new ValueNode("some@email.com"));
    }

    @Test(expected = ConfigurationValidationException.class)
    public void testMapValidationFail() throws Exception {
        mapper.map(new MapNode(), ValidatingFixture.class);
    }

    @Test(expected = ConfigurationValidationException.class)
    public void tesUnmapValidationFail() throws Exception {
        mapper.unmap(new ValidatingFixture(), ValidatingFixture.class);
    }
}