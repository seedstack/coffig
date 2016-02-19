/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.configuration.mapper;

import org.seedstack.configuration.ConfigurationException;
import org.seedstack.configuration.data.MapNode;
import org.seedstack.configuration.PropertyNotFoundException;
import org.seedstack.configuration.data.TreeNode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

class ObjectMapper<T> {

    private final Class<T> aClass;
    private final List<FieldInfo> fieldInfos;
    private T holder;

    public ObjectMapper(Class<T> aClass) {
        this.aClass = aClass;
        this.holder = getNewInstance();
        this.fieldInfos = getFieldInfo();
    }

    private T getNewInstance() {
        try {
            return aClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ConfigurationException(e);
        }
    }

    private List<FieldInfo> getFieldInfo() {
        return Arrays.stream(aClass.getDeclaredFields()).map(FieldInfo::new).collect(toList());
    }

    public T map(MapNode rootNode) {
        fieldInfos.stream().forEach(fieldInfo -> {
            try {
                TreeNode treeNode = rootNode.value(fieldInfo.name);
                Object fieldValue = MapperFactory.getInstance().map(treeNode, fieldInfo.type);
                if (fieldValue != null) {
                    Consumer<Object> fieldInitializer = fieldInfo.initializer;
                    fieldInitializer.accept(fieldValue);
                }
            } catch (PropertyNotFoundException | UnsupportedOperationException exception) {
                // Nothing to do here. Missing properties are ignored
            }
        });
        return holder;
    }

    class FieldInfo {
        String name;
        Class<?> fieldClass;
        Type type;
        Consumer<Object> initializer;

        FieldInfo(Field field) {
            this.name = field.getName();
            this.fieldClass = field.getType();
            this.type = field.getGenericType();
            this.initializer = getPropertyInitializer(field);
        }

        private Consumer<Object> getPropertyInitializer(Field field) {
            Optional<Method> setter = getSetter(field);
            if (setter.isPresent()) {
                return getSetterInitializer(setter.get());
            } else {
                return getFieldInitializer(field);
            }
        }

        private Optional<Method> getSetter(Field field) {
            String setterName = fieldToSetterName(field);
            try {
                Method setter = aClass.getDeclaredMethod(setterName, field.getType());
                return Optional.of(setter);
            } catch (NoSuchMethodException e) {
                return Optional.empty();
            }
        }

        private String fieldToSetterName(Field field) {
            return "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        }

        private Consumer<Object> getSetterInitializer(Method setter) {
            return o -> {
                try {
                    setter.invoke(holder, o);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new ConfigurationException(e);
                }
            };
        }

        private Consumer<Object> getFieldInitializer(Field field) {
            return o -> {
                field.setAccessible(true);
                try {
                    field.set(holder, o);
                } catch (IllegalAccessException e) {
                    throw new ConfigurationException(e);
                }
            };
        }
    }
}
