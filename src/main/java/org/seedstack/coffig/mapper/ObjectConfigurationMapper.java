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
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.MutableMapNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

class ObjectConfigurationMapper<T> {
    private final MapperFactory mapperFactory;
    private final Class<? extends T> aClass;
    private final String prefix;
    private final List<FieldInfo> fieldInfo;
    private final T holder;

    ObjectConfigurationMapper(MapperFactory mapperFactory, Class<T> aClass) {
        this.mapperFactory = mapperFactory;
        this.aClass = aClass;
        this.prefix = this.aClass.isAnnotationPresent(Config.class) ? aClass.getAnnotation(Config.class).value() : null;
        this.fieldInfo = getFieldInfo();
        this.holder = getNewInstance();
    }

    @SuppressWarnings("unchecked")
    ObjectConfigurationMapper(MapperFactory mapperFactory, T object) {
        this.mapperFactory = mapperFactory;
        this.aClass = (Class<? extends T>) object.getClass();
        this.prefix = this.aClass.isAnnotationPresent(Config.class) ? aClass.getAnnotation(Config.class).value() : null;
        this.fieldInfo = getFieldInfo();
        this.holder = object;
    }

    private T getNewInstance() {
        try {
            Constructor<? extends T> constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    private List<FieldInfo> getFieldInfo() {
        return Arrays.stream(aClass.getDeclaredFields()).map(FieldInfo::new).collect(toList());
    }

    T map(MapNode rootNode) {
        Optional<TreeNode> startNode;
        if (prefix != null) {
            startNode = rootNode.get(prefix);
        } else {
            startNode = Optional.of(rootNode);
        }

        if (startNode.isPresent()) {
            fieldInfo.stream().forEach(fieldInfo -> {
                String path;
                if (fieldInfo.prefix != null) {
                    path = String.format("%s.%s", fieldInfo.prefix, fieldInfo.name);
                } else {
                    path = fieldInfo.name;
                }

                Object fieldValue = mapperFactory.map(startNode.get().get(path).orElse(null), fieldInfo.type);
                if (fieldValue != null) {
                    Consumer<Object> fieldInitializer = fieldInfo.consumer;
                    fieldInitializer.accept(fieldValue);
                }
            });
        }

        return holder;
    }

    TreeNode unmap() {
        MutableMapNode rootNode = new MutableMapNode();
        MutableMapNode startNode;
        if (prefix != null) {
            startNode = new MutableMapNode();
            rootNode.set(prefix, startNode);
        } else {
            startNode = rootNode;
        }

        fieldInfo.stream().forEach(fieldInfo -> {
            TreeNode unmapped = mapperFactory.unmap(fieldInfo.supplier.get(), fieldInfo.type);
            if (unmapped != null) {
                String path;
                if (fieldInfo.prefix != null) {
                    path = String.format("%s.%s", fieldInfo.prefix, fieldInfo.name);
                } else {
                    path = fieldInfo.name;
                }
                startNode.set(path, unmapped);
            }
        });

        return rootNode;
    }

    private class FieldInfo {
        String name;
        String prefix;
        Type type;
        Consumer<Object> consumer;
        Supplier<Object> supplier;

        FieldInfo(Field field) {
            this.name = field.getName();
            this.prefix = field.isAnnotationPresent(Config.class) ? field.getAnnotation(Config.class).value() : null;
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
                } catch (Exception e) {
                    throw new ConfigurationException(e);
                }
            };
        }

        private Consumer<Object> getFieldConsumer(Field field) {
            return o -> {
                field.setAccessible(true);
                try {
                    field.set(holder, o);
                } catch (Exception e) {
                    throw new ConfigurationException(e);
                }
            };
        }

        private Supplier<Object> getGetterSupplier(Method getter) {
            return () -> {
                try {
                    return getter.invoke(holder);
                } catch (Exception e) {
                    throw new ConfigurationException(e);
                }
            };
        }

        private Supplier<Object> getFieldSupplier(Field field) {
            return () -> {
                field.setAccessible(true);
                try {
                    return field.get(holder);
                } catch (Exception e) {
                    throw new ConfigurationException(e);
                }
            };
        }
    }
}
