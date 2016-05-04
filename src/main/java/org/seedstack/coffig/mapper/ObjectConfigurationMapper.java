/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.Config;
import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.PropertyNotFoundException;
import org.seedstack.coffig.MapNode;
import org.seedstack.coffig.MutableMapNode;
import org.seedstack.coffig.TreeNode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

class ObjectConfigurationMapper<T> {

    private final Class<? extends T> aClass;
    private final String prefix;
    private final List<FieldInfo> fieldInfo;
    private final T holder;

    public ObjectConfigurationMapper(Class<T> aClass) {
        this.aClass = aClass;
        this.prefix = this.aClass.isAnnotationPresent(Config.class) ? aClass.getAnnotation(Config.class).value() : null;
        this.fieldInfo = getFieldInfo();
        this.holder = getNewInstance();
    }

    @SuppressWarnings("unchecked")
    public ObjectConfigurationMapper(T object) {
        this.aClass = (Class<? extends T>) object.getClass();
        this.prefix = this.aClass.isAnnotationPresent(Config.class) ? aClass.getAnnotation(Config.class).value() : null;
        this.fieldInfo = getFieldInfo();
        this.holder = object;
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
        Optional<TreeNode> startNode;
        if (prefix != null) {
            startNode = rootNode.get(prefix);
        } else {
            startNode = Optional.of(rootNode);
        }

        if (startNode.isPresent()) {
            fieldInfo.stream().forEach(fieldInfo -> {
                try {
                    Optional<TreeNode> treeNode = startNode;
                    if (fieldInfo.prefix != null) {
                        treeNode = startNode.get().get(fieldInfo.prefix);
                    }
                    if (treeNode.isPresent()) {
                        Object fieldValue = MapperFactory.getInstance().map(treeNode.get().value(fieldInfo.name), fieldInfo.type);
                        if (fieldValue != null) {
                            Consumer<Object> fieldInitializer = fieldInfo.consumer;
                            fieldInitializer.accept(fieldValue);
                        }
                    }
                } catch (PropertyNotFoundException | UnsupportedOperationException exception) {
                    // Nothing to do here. Missing properties are ignored
                }
            });
        }
        return holder;
    }

    public TreeNode unmap() {
        MutableMapNode mapNode = new MutableMapNode();
        fieldInfo.stream().forEach(fieldInfo -> {
            TreeNode unmapped = MapperFactory.getInstance().unmap(fieldInfo.supplier.get());
            if (unmapped != null) {
                mapNode.put(fieldInfo.name, unmapped);
            }
        });
        return mapNode;
    }

    private class FieldInfo {
        String name;
        String prefix;
        Class<?> fieldClass;
        Type type;
        Consumer<Object> consumer;
        Supplier<Object> supplier;

        FieldInfo(Field field) {
            this.name = field.getName();
            this.prefix = field.isAnnotationPresent(Config.class) ? field.getAnnotation(Config.class).value() : null;
            this.fieldClass = field.getType();
            this.type = field.getGenericType();
            this.consumer = getPropertyConsumer(field);
            this.supplier = getPropertySupplier(field);
        }

        private Consumer<Object> getPropertyConsumer(Field field) {
            Optional<Method> setter = getSetter(field);
            if (setter.isPresent()) {
                return getSetterConsumer(setter.get());
            } else {
                return getFieldConsumer(field);
            }
        }

        private Supplier<Object> getPropertySupplier(Field field) {
            Optional<Method> getter = getGetter(field);
            if (getter.isPresent()) {
                return getGetterSupplier(getter.get());
            } else {
                return getFieldSupplier(field);
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

        private Optional<Method> getGetter(Field field) {
            String getterName = fieldToGetterName(field);
            try {
                Method getter = aClass.getDeclaredMethod(getterName);
                return Optional.of(getter);
            } catch (NoSuchMethodException e) {
                return Optional.empty();
            }
        }

        private String fieldToSetterName(Field field) {
            return "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        }

        private String fieldToGetterName(Field field) {
            return "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        }

        private Consumer<Object> getSetterConsumer(Method setter) {
            return o -> {
                try {
                    setter.invoke(holder, o);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new ConfigurationException(e);
                }
            };
        }

        private Consumer<Object> getFieldConsumer(Field field) {
            return o -> {
                field.setAccessible(true);
                try {
                    field.set(holder, o);
                } catch (IllegalAccessException e) {
                    throw new ConfigurationException(e);
                }
            };
        }

        private Supplier<Object> getGetterSupplier(Method getter) {
            return () -> {
                try {
                    return getter.invoke(holder);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new ConfigurationException(e);
                }
            };
        }

        private Supplier<Object> getFieldSupplier(Field field) {
            return () -> {
                field.setAccessible(true);
                try {
                    return field.get(holder);
                } catch (IllegalAccessException e) {
                    throw new ConfigurationException(e);
                }
            };
        }
    }
}
