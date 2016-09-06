/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.utils;

import org.seedstack.coffig.Config;
import org.seedstack.coffig.ConfigurationException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

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
        if (Boolean.class.equals(configurationClass)) {
            return (T) Boolean.FALSE;
        } else if (Integer.class.equals(configurationClass)) {
            return (T) new Integer(0);
        } else if (Long.class.equals(configurationClass)) {
            return (T) new Long(0L);
        } else if (Short.class.equals(configurationClass)) {
            return (T) new Short((short) 0);
        } else if (Float.class.equals(configurationClass)) {
            return (T) new Float(0f);
        } else if (Double.class.equals(configurationClass)) {
            return (T) new Double(0d);
        } else if (Byte.class.equals(configurationClass)) {
            return (T) new Byte((byte) 0);
        } else if (Character.class.equals(configurationClass)) {
            return (T) new Character((char) 0);
        } else {
            try {
                return configurationClass.newInstance();
            } catch (Exception e) {
                throw new ConfigurationException("Cannot instantiate default value", e);
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
}
