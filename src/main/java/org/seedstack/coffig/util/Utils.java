/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.util;

import org.seedstack.coffig.Config;
import org.seedstack.coffig.ConfigurationErrorCode;
import org.seedstack.coffig.ConfigurationException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

public final class Utils {
    private Utils() {
    }

    public static String resolvePath(AnnotatedElement annotatedElement) {
        Config annotation;
        StringBuilder path = new StringBuilder();
        if (annotatedElement instanceof Class) {
            Class<?> currentClass = (Class) annotatedElement;
            while (currentClass != null && (annotation = currentClass.getAnnotation(Config.class)) != null) {
                if (!annotation.value().isEmpty()) {
                    if (path.length() > 0) {
                        path.insert(0, ".");
                    }
                    path.insert(0, annotation.value());
                }
                currentClass = currentClass.getDeclaringClass();
            }
            return path.toString();
        } else {
            annotation = annotatedElement.getAnnotation(Config.class);
            if (annotation != null) {
                return annotation.value();
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T instantiateDefault(Class<T> configurationClass) {
        if (configurationClass.isArray()) {
            return (T) Array.newInstance(configurationClass.getComponentType(), 0);
        } else {
            return instantiateScalar(configurationClass);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiateScalar(Class<T> configurationClass) {
        if (boolean.class.equals(configurationClass) || Boolean.class.equals(configurationClass)) {
            return (T) Boolean.FALSE;
        } else if (int.class.equals(configurationClass) || Integer.class.equals(configurationClass)) {
            return (T) Integer.valueOf(0);
        } else if (long.class.equals(configurationClass) || Long.class.equals(configurationClass)) {
            return (T) Long.valueOf(0L);
        } else if (short.class.equals(configurationClass) || Short.class.equals(configurationClass)) {
            return (T) Short.valueOf((short) 0);
        } else if (float.class.equals(configurationClass) || Float.class.equals(configurationClass)) {
            return (T) Float.valueOf(0f);
        } else if (double.class.equals(configurationClass) || Double.class.equals(configurationClass)) {
            return (T) Double.valueOf(0d);
        } else if (byte.class.equals(configurationClass) || Byte.class.equals(configurationClass)) {
            return (T) Byte.valueOf((byte) 0);
        } else if (char.class.equals(configurationClass) || Character.class.equals(configurationClass)) {
            return (T) Character.valueOf((char) 0);
        } else {
            try {
                Constructor<T> defaultConstructor = configurationClass.getDeclaredConstructor();
                defaultConstructor.setAccessible(true);
                return defaultConstructor.newInstance();
            } catch (Exception e) {
                throw ConfigurationException.wrap(e, ConfigurationErrorCode.ERROR_DURING_INSTANTIATION)
                        .put("class", configurationClass.getName());
            }
        }
    }

    public static Class<?> getRawClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            return (Class<?>) rawType;
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawClass(componentType), 0).getClass();
        } else if (type instanceof TypeVariable) {
            return Object.class;
        } else {
            throw new IllegalArgumentException("Unsupported type " + type.getTypeName());
        }
    }

    public static String getSimpleTypeName(Type type) {
        return buildTypeName(type, new StringBuilder()).toString();
    }

    private static StringBuilder buildTypeName(Type type, StringBuilder sb) {
        if (type instanceof ParameterizedType) {
            buildTypeName(((ParameterizedType) type).getRawType(), sb);
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            sb.append("<");
            buildGenericTypeNames(actualTypeArguments, sb);
            sb.append(">");
        } else if (type instanceof Class) {
            sb.append(((Class) type).getSimpleName());
        } else if (type instanceof WildcardType) {
            sb.append("?");
            Type[] lowerBounds = ((WildcardType) type).getLowerBounds();
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (lowerBounds.length > 0) {
                sb.append(" super ");
                buildGenericTypeNames(lowerBounds, sb);
            } else if (upperBounds.length > 0) {
                if (upperBounds.length > 1 || !upperBounds[0].equals(Object.class)) {
                    sb.append(" extends ");
                    buildGenericTypeNames(upperBounds, sb);
                }
            }
        }
        return sb;
    }

    private static void buildGenericTypeNames(Type[] actualTypeArguments, StringBuilder sb) {
        for (int i = 0; i < actualTypeArguments.length; i++) {
            Type typeArgument = actualTypeArguments[i];
            buildTypeName(typeArgument, sb);
            if (i < actualTypeArguments.length - 1) {
                sb.append(", ");
            }
        }
    }
}
