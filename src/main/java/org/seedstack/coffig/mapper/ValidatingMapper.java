/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.ConfigurationValidationException;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.spi.ConfigurationComponent;
import org.seedstack.coffig.spi.ConfigurationMapper;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.lang.reflect.Type;
import java.util.Set;

public class ValidatingMapper implements ConfigurationMapper {
    private final ConfigurationMapper mapper;
    private final Validator validator;

    public ValidatingMapper(ConfigurationMapper mapper) {
        this.mapper = mapper;
        this.validator = Validation
                .byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();
    }

    @Override
    public void initialize(Coffig coffig) {
        mapper.initialize(coffig);
    }

    @Override
    public void invalidate() {
        mapper.invalidate();
    }

    @Override
    public boolean isDirty() {
        return mapper.isDirty();
    }

    @Override
    public ConfigurationComponent fork() {
        return new ValidatingMapper((ConfigurationMapper) mapper.fork());
    }

    @Override
    public boolean canHandle(Type type) {
        return mapper.canHandle(type);
    }

    @Override
    public Object map(TreeNode treeNode, Type type) {
        Object result = mapper.map(treeNode, type);
        validate(result);
        return result;
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        validate(object);
        return mapper.unmap(object, type);
    }

    public ConfigurationMapper getMapper() {
        return mapper;
    }

    private void validate(Object object) {
        if (object != null) {
            Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
            if (!constraintViolations.isEmpty()) {
                throw new ConfigurationValidationException(constraintViolations);
            }
        }
    }
}
