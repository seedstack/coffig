/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.internal;

import org.seedstack.shed.exception.ErrorCode;

public enum ConfigurationErrorCode implements ErrorCode {
    CANNOT_CONVERT_ARRAY_TO_VALUE,
    CANNOT_CONVERT_MAP_TO_VALUE,
    CANNOT_SUPPLY_CONFIGURATION_OBJECT,
    ERROR_DURING_FIELD_ACCESS,
    ERROR_DURING_FIELD_INJECTION,
    ERROR_DURING_GETTER_INVOCATION,
    ERROR_DURING_METHOD_INVOCATION,
    ERROR_DURING_SETTER_INVOCATION,
    ERROR_OCCURRED_DURING_COMPOSITE_PROVIDE,
    FAILED_TO_READ_CONFIGURATION,
    ILLEGAL_CONVERSION,
    ILLEGAL_TREE_ACCESS,
    ILLEGAL_TREE_MERGE,
    NON_ASSIGNABLE_CLASS,
    PATH_NOT_FOUND,
    PROPERTY_NOT_FOUND,
    UNABLE_TO_LOAD_CLASS,
    UNEXPECTED_EXCEPTION,
    UNMAPPING_IS_NOT_SUPPORTED
}
