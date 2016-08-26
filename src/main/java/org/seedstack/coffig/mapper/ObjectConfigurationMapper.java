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
import org.seedstack.coffig.SingleValue;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.MutableMapNode;
import org.seedstack.coffig.node.ValueNode;

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
    private final Class<T> aClass;
    private final List<FieldInfo> fieldInfo;
    private final FieldInfo valueFieldInfo;
    private final T holder;

    ObjectConfigurationMapper(MapperFactory mapperFactory, Class<T> aClass) {
        this.mapperFactory = mapperFactory;
        this.aClass = aClass;
        this.fieldInfo = getFieldInfo();
        this.valueFieldInfo = getValueFieldInfo();
        this.holder = getNewInstance();
    }

    @SuppressWarnings("unchecked")
    ObjectConfigurationMapper(MapperFactory mapperFactory, T object) {
        this.mapperFactory = mapperFactory;
        this.aClass = (Class<T>) object.getClass();
        this.fieldInfo = getFieldInfo();
        this.valueFieldInfo = getValueFieldInfo();
        this.holder = object;
    }

    private FieldInfo getValueFieldInfo() {
        FieldInfo valueFieldInfo = null;
        for (FieldInfo fieldInfo : this.fieldInfo) {
            if (fieldInfo.singleValue) {
                valueFieldInfo = fieldInfo;
                break;
            }
        }
        return valueFieldInfo;
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

    T map(TreeNode rootNode) {
        if (rootNode instanceof ValueNode && valueFieldInfo != null) {
            try {
                Object fieldValue = mapperFactory.map(rootNode, valueFieldInfo.type);
                if (fieldValue != null) {
                    valueFieldInfo.consumer.accept(fieldValue);
                }
            } catch (Exception e) {
                throw new ConfigurationException(String.format("Unable to inject value in field '%s' of class '%s'", valueFieldInfo.name, aClass.getCanonicalName()), e);
            }
        } else if (rootNode instanceof MapNode) {
            fieldInfo.forEach(fieldInfo -> {
                try {
                    Object fieldValue = mapperFactory.map(
                            rootNode.get(fieldInfo.alias != null ? fieldInfo.alias : fieldInfo.name).orElse(null),
                            fieldInfo.type
                    );
                    if (fieldValue != null) {
                        fieldInfo.consumer.accept(fieldValue);
                    }
                } catch (Exception e) {
                    throw new ConfigurationException(String.format("Unable to inject value in field '%s' of class '%s'", fieldInfo.name, aClass.getCanonicalName()), e);
                }
            });
        }

        return holder;
    }

    TreeNode unmap() {
        MutableMapNode rootNode = new MutableMapNode();
        fieldInfo.forEach(fieldInfo -> {
            try {
                TreeNode unmapped = mapperFactory.unmap(fieldInfo.supplier.get(), fieldInfo.type);
                if (unmapped != null) {
                    rootNode.set(fieldInfo.alias != null ? fieldInfo.alias : fieldInfo.name, unmapped);
                }
            } catch (Exception e) {
                throw new ConfigurationException(String.format("Unable to extract value from field '%s' of class '%s'", fieldInfo.name, aClass.getCanonicalName()), e);
            }
        });
        return rootNode;
    }

    private class FieldInfo {
        final String name;
        final String alias;
        final Type type;
        final Consumer<Object> consumer;
        final Supplier<Object> supplier;
        final boolean singleValue;

        FieldInfo(Field field) {
            this.name = field.getName();
            this.alias = resolveAlias(field);
            this.type = field.getGenericType();
            this.consumer = getPropertyConsumer(field);
            this.supplier = getPropertySupplier(field);
            this.singleValue = field.isAnnotationPresent(SingleValue.class);
        }

        private String resolveAlias(Field field) {
            Config annotation = field.getAnnotation(Config.class);
            if (annotation != null) {
                return annotation.value();
            }
            annotation = field.getType().getAnnotation(Config.class);
            if (annotation != null) {
                return annotation.value();
            }
            return null;
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
