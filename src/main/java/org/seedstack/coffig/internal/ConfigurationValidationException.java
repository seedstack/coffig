/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.internal;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

/**
 * This exception class formats a fully detailed message from a set of constraint violations or from an existing
 * {@link ConstraintViolationException}.
 */
public class ConfigurationValidationException extends ConstraintViolationException {
    /**
     * Creates a verbose {@link ConstraintViolationException} from a set of constraint violations.
     *
     * @param constraintViolations the set of contraint violations.
     */
    public ConfigurationValidationException(final Set<? extends ConstraintViolation<?>> constraintViolations) {
        super(createVerboseMessage(constraintViolations), constraintViolations);
    }

    private static String createVerboseMessage(final Set<? extends ConstraintViolation<?>> constraintViolations) {
        final StringBuilder sb = new StringBuilder(1024);
        boolean first = true;
        for (final ConstraintViolation<?> constraintViolation : constraintViolations) {
            if (first) {
                sb.append(constraintViolation.getRootBeanClass().getCanonicalName());
                sb.append("\n");
                first = false;
            }
            sb.append("\t");
            sb.append(constraintViolation.getPropertyPath());
            sb.append(": ");
            sb.append(constraintViolation.getMessage());
            sb.append(" ('");
            sb.append(constraintViolation.getInvalidValue());
            sb.append("')\n");
        }
        return sb.toString();
    }
}