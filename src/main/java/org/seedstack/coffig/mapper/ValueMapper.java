/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.ConfigurationErrorCode;
import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ValueMapper implements ConfigurationMapper {
    private final Map<Type, Function<String, ?>> converters = new HashMap<>();

    public ValueMapper() {
        converters.put(Boolean.class, Boolean::valueOf);
        converters.put(boolean.class, Boolean::valueOf);
        converters.put(Byte.class, Byte::valueOf);
        converters.put(byte.class, Byte::valueOf);
        converters.put(Character.class, this::charOf);
        converters.put(char.class, this::charOf);
        converters.put(Double.class, Double::valueOf);
        converters.put(double.class, Double::valueOf);
        converters.put(Float.class, Float::valueOf);
        converters.put(float.class, Float::valueOf);
        converters.put(Integer.class, Integer::valueOf);
        converters.put(int.class, Integer::valueOf);
        converters.put(Long.class, Long::valueOf);
        converters.put(long.class, Long::valueOf);
        converters.put(Short.class, Short::valueOf);
        converters.put(short.class, Short::valueOf);
        converters.put(String.class, String::valueOf);
    }

    @Override
    public boolean canHandle(Type type) {
        return type instanceof Class && converters.containsKey(type);
    }

    @Override
    public Object map(TreeNode value, Type type) {
        return converters.get(type).apply(value.value());
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        return new ValueNode(String.valueOf(object));
    }

    private char charOf(String value) {
        if (value.length() == 1) {
            return value.charAt(0);
        }
        throw ConfigurationException.createNew(ConfigurationErrorCode.ILLEGAL_CONVERSION)
                .put("value", value)
                .put("type", char.class);
    }
}
