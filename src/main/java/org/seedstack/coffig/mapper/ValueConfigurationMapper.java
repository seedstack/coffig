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
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ValueConfigurationMapper implements ConfigurationMapper {

    final StringConverter booleanConverter = Boolean::valueOf;
    final StringConverter byteConverter = Byte::valueOf;
    final StringConverter charConverter = v -> {
        if (v.length() == 1)
            return v.charAt(0);
        throw new ConfigurationException("Failed to convert \"" + v + "\" to Character.");
    };
    final StringConverter doubleConverter = Double::valueOf;
    final StringConverter floatConverter = Float::valueOf;
    final StringConverter integerConverter = Integer::valueOf;
    final StringConverter longConverter = Long::valueOf;
    final StringConverter shortConverter = Short::valueOf;
    final StringConverter stringConverter = String::valueOf;
    private final Map<Class<?>, StringConverter> converters = new HashMap<>();
    final StringArrayConverter booleanArrayConverter = values -> {
        boolean[] booleans = new boolean[values.length];
        for (int i = 0; i < values.length; i++) {
            booleans[i] = (boolean) this.convertObject(values[i], boolean.class);
        }
        return booleans;
    };
    final StringArrayConverter byteArrayConverter = values -> {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) this.convertObject(values[i], byte.class);
        }
        return bytes;
    };
    final StringArrayConverter charArrayConverter = values -> {
        char[] chars = new char[values.length];
        for (int i = 0; i < values.length; i++) {
            chars[i] = (char) this.convertObject(values[i], char.class);
        }
        return chars;
    };
    final StringArrayConverter doubleArrayConverter = values -> {
        double[] doubles = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            doubles[i] = (double) this.convertObject(values[i], double.class);
        }
        return doubles;
    };
    final StringArrayConverter floatArrayConverter = values -> {
        float[] floats = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            floats[i] = (float) this.convertObject(values[i], float.class);
        }
        return floats;
    };
    final StringArrayConverter intArrayConverter = values -> {
        int[] ints = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            ints[i] = (int) this.convertObject(values[i], int.class);
        }
        return ints;
    };
    final StringArrayConverter longArrayConverter = values -> {
        long[] longs = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            longs[i] = (long) this.convertObject(values[i], long.class);
        }
        return longs;
    };
    final StringArrayConverter shortArrayConverter = values -> {
        short[] shorts = new short[values.length];
        for (int i = 0; i < values.length; i++) {
            shorts[i] = (short) this.convertObject(values[i], short.class);
        }
        return shorts;
    };
    private final Map<Class<?>, StringArrayConverter> arrayConverters = new HashMap<>();

    public ValueConfigurationMapper() {
        converters.put(Boolean.class, booleanConverter);
        converters.put(boolean.class, booleanConverter);
        converters.put(Byte.class, byteConverter);
        converters.put(byte.class, byteConverter);
        converters.put(Character.class, charConverter);
        converters.put(char.class, charConverter);
        converters.put(Double.class, doubleConverter);
        converters.put(double.class, doubleConverter);
        converters.put(Float.class, floatConverter);
        converters.put(float.class, floatConverter);
        converters.put(Integer.class, integerConverter);
        converters.put(int.class, integerConverter);
        converters.put(Long.class, longConverter);
        converters.put(long.class, longConverter);
        converters.put(Short.class, shortConverter);
        converters.put(short.class, shortConverter);
        converters.put(String.class, stringConverter);

        arrayConverters.put(boolean[].class, booleanArrayConverter);
        arrayConverters.put(byte[].class, byteArrayConverter);
        arrayConverters.put(char[].class, charArrayConverter);
        arrayConverters.put(double[].class, doubleArrayConverter);
        arrayConverters.put(float[].class, floatArrayConverter);
        arrayConverters.put(int[].class, intArrayConverter);
        arrayConverters.put(long[].class, longArrayConverter);
        arrayConverters.put(short[].class, shortArrayConverter);
    }

    private Stream<Object> valuesToObjectStream(String[] values, Class<?> expectedType) {
        return Arrays.stream(values).map(object -> convertObject(object, expectedType));
    }

    public Object convertArray(String[] values, Type type) {
        if (type instanceof Class) {
            Class<?> aClass = (Class<?>) type;
            if (arrayConverters.containsKey(aClass)) {
                return arrayConverters.get(aClass).convert(values);
            }
            return valuesToObjectStream(values, aClass.getComponentType())
                    .toArray(s -> (Object[]) Array.newInstance(aClass.getComponentType(), s));

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

    @Override
    public boolean canHandle(Class<?> aClass) {
        return converters.containsKey(aClass);
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

    public Object convertObject(String value, Type type) {
        Class<?> aClass = (Class<?>) type;
        if (value != null) {
            StringConverter stringConverter = converters.get(aClass);
            if (stringConverter == null) {
                throw new ConfigurationException("No converter found for class " + aClass.getCanonicalName());
            }
            return stringConverter.convert(value);
        }
        return null;
    }

    @FunctionalInterface
    interface StringConverter {
        Object convert(String value);
    }

    @FunctionalInterface
    interface StringArrayConverter {
        Object convert(String[] value);
    }
}
