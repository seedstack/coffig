/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ValueConfigurationMapper implements ConfigurationMapper {
    private final Map<Class<?>, Function<String, ?>> converters = new HashMap<>();
    private final Map<Class<?>, Function<String[], ?>> arrayConverters = new HashMap<>();

    ValueConfigurationMapper() {
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

        arrayConverters.put(boolean[].class, arrayConverter(boolean.class, Boolean::valueOf));
        arrayConverters.put(byte[].class, arrayConverter(byte.class, Byte::valueOf));
        arrayConverters.put(char[].class, arrayConverter(char.class, this::charOf));
        arrayConverters.put(double[].class, arrayConverter(double.class, Double::valueOf));
        arrayConverters.put(float[].class, arrayConverter(float.class, Float::valueOf));
        arrayConverters.put(int[].class, arrayConverter(int.class, Integer::valueOf));
        arrayConverters.put(long[].class, arrayConverter(long.class, Long::valueOf));
        arrayConverters.put(short[].class, arrayConverter(short.class, Short::valueOf));
//        arrayConverters.put(String[].class, arrayConverter(String.class, String::valueOf));
    }

    private Function<String[], Object> arrayConverter(Class componentType, Function<String, Object> conversionFunction) {
        return values -> {
            Object array = Array.newInstance(componentType, values.length);
            for (int i = 0; i < values.length; i++) {
                Array.set(array, i, conversionFunction.apply(values[i]));
            }
            return array;
        };
    }

    @Override
    public boolean canHandle(Type type) {
        return type instanceof Class && converters.containsKey(type);
    }

    @Override
    public Object map(TreeNode value, Type type) {
        return convertObject(value.value(), type);
    }

    @Override
    public TreeNode unmap(Object object) {
        if (object == null) {
            return null;
        } else {
            return new ValueNode(object.toString());
        }
    }

    Object convertArray(String[] values, Type type) {
        if (type instanceof Class) {
            Class<?> aClass = (Class<?>) type;
            if (arrayConverters.containsKey(aClass)) {
                return arrayConverters.get(aClass).apply(values);
            }
            return valuesToObjectStream(values, aClass.getComponentType()).toArray(s -> (Object[]) Array.newInstance(aClass.getComponentType(), s));

        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> typeArgument = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
            if (parameterizedType.getRawType().equals(List.class)) {
                return valuesToObjectStream(values, typeArgument).collect(Collectors.toList());
            } else if (parameterizedType.getRawType().equals(Set.class)) {
                return valuesToObjectStream(values, typeArgument).collect(Collectors.toSet());
            }
        }
        return null;
    }

    Object convertObject(String value, Type type) {
        Class<?> aClass = (Class<?>) type;
        if (value != null) {
            Function<String, ?> stringConverter = converters.get(aClass);
            if (stringConverter == null) {
                throw new ConfigurationException("No converter found for class " + aClass.getCanonicalName());
            }
            return stringConverter.apply(value);
        }
        return null;
    }

    private Stream<Object> valuesToObjectStream(String[] values, Class<?> expectedType) {
        return Arrays.stream(values).map(object -> convertObject(object, expectedType));
    }

    private char charOf(String value) {
        if (value.length() == 1) {
            return value.charAt(0);
        }
        throw new ConfigurationException("Failed to convert \"" + value + "\" to Character.");
    }
}
