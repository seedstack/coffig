/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.internal;

import org.seedstack.shed.exception.BaseException;
import org.seedstack.shed.exception.ErrorCode;

public class ConfigurationException extends BaseException {
    protected ConfigurationException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected ConfigurationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * Create a new ConfigurationException from an {@link ErrorCode}.
     *
     * @param errorCode the error code to set.
     * @return the created ConfigurationException.
     */
    public static ConfigurationException createNew(ErrorCode errorCode) {
        return new ConfigurationException(errorCode);
    }

    /**
     * Wrap a ConfigurationException with an {@link ErrorCode} around an existing {@link Throwable}.
     *
     * @param throwable the existing throwable to wrap.
     * @param errorCode the error code to set.
     * @return the created ConfigurationException.
     */
    public static ConfigurationException wrap(Throwable throwable, ErrorCode errorCode) {
        return new ConfigurationException(errorCode, throwable);
    }
}
