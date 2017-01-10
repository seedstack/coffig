/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import org.seedstack.shed.exception.ErrorCode;

public enum ConfigurationErrorCode implements ErrorCode {
    CANNOT_ACCESS_ARRAY_AS_MAP,
    CANNOT_ACCESS_ARRAY_AS_SINGLE_VALUE,
    CANNOT_ACCESS_MAP_AS_SINGLE_VAlUE,
    CANNOT_ACCESS_SINGLE_VALUE_AS_MAP,
    CANNOT_SUPPLY_CONFIGURATION_OBJECT,
    ERROR_DURING_FIELD_ACCESS,
    ERROR_DURING_FIELD_INJECTION,
    ERROR_DURING_GETTER_INVOCATION,
    ERROR_DURING_INSTANTIATION,
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
    SPECIFIED_ITEM_CLASS_NOT_FOUND,
    UNABLE_TO_LOAD_CLASS,
    UNMAPPING_IS_NOT_SUPPORTED
}
