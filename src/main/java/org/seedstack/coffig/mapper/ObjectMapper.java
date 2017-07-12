/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.SingleValue;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.spi.ConfigurationComponent;
import org.seedstack.shed.reflect.Classes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static org.seedstack.shed.reflect.Classes.instantiateDefault;

class ObjectMapper<T> implements ConfigurationComponent {
    private final Class<T> aClass;
    private final List<FieldInfo> fieldInfo;
    private final FieldInfo valueFieldInfo;
    private final T holder;
    private Coffig coffig;

    ObjectMapper(Class<T> aClass) {
        this.aClass = aClass;
        this.fieldInfo = getFieldInfo();
        this.valueFieldInfo = getValueFieldInfo();
        this.holder = instantiateDefault(aClass);
    }

    @SuppressWarnings("unchecked")
    ObjectMapper(T object) {
        this.aClass = (Class<T>) object.getClass();
        this.fieldInfo = getFieldInfo();
        this.valueFieldInfo = getValueFieldInfo();
        this.holder = object;
    }

    @Override
    public void initialize(Coffig coffig) {
        this.coffig = coffig;
    }

    T map(TreeNode rootNode) {
        if (rootNode.type() == TreeNode.Type.VALUE_NODE && valueFieldInfo != null) {
            Optional.ofNullable(coffig.getMapper().map(rootNode, valueFieldInfo.type))
                    .ifPresent(valueFieldInfo.consumer);
        } else if (rootNode.type() == TreeNode.Type.MAP_NODE) {
            fieldInfo.forEach(fieldInfo -> rootNode.get(fieldInfo.alias != null ? fieldInfo.alias : fieldInfo.name)
                    .map(treeNode -> coffig.getMapper().map(treeNode, fieldInfo.type))
                    .ifPresent(fieldInfo.consumer));
        }
        return holder;
    }

    TreeNode unmap() {
        MapNode rootNode = new MapNode();
        fieldInfo.forEach(fieldInfo -> Optional.ofNullable(coffig.getMapper().unmap(fieldInfo.supplier.get(), fieldInfo.type))
                .ifPresent(treeNode -> rootNode.set(fieldInfo.alias != null ? fieldInfo.alias : fieldInfo.name, treeNode)));
        return rootNode;
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

    private List<FieldInfo> getFieldInfo() {
        return Classes.from(aClass)
                .traversingSuperclasses()
                .fields()
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(FieldInfo::new)
                .collect(toList());
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
            return setter.map(this::getSetterConsumer).orElseGet(() -> getFieldConsumer(field));
        }

        private Supplier<Object> getPropertySupplier(Field field) {
            Optional<Method> getter = getGetter(field);
            return getter.map(this::getGetterSupplier).orElseGet(() -> getFieldSupplier(field));
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
            return "set" + field.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + field.getName().substring(1);
        }

        private String fieldToGetterName(Field field) {
            return "get" + field.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + field.getName().substring(1);
        }

        private Consumer<Object> getSetterConsumer(Method setter) {
            return o -> {
                try {
                    setter.invoke(holder, o);
                } catch (Exception e) {
                    throw ConfigurationException.wrap(e, ConfigurationErrorCode.ERROR_DURING_SETTER_INVOCATION)
                            .put("class", holder.getClass().getName())
                            .put("setter", setter.getName());
                }
            };
        }

        private Consumer<Object> getFieldConsumer(Field field) {
            return o -> {
                field.setAccessible(true);
                try {
                    field.set(holder, o);
                } catch (Exception e) {
                    throw ConfigurationException.wrap(e, ConfigurationErrorCode.ERROR_DURING_FIELD_INJECTION)
                            .put("class", holder.getClass().getName())
                            .put("field", field.getName());
                }
            };
        }

        private Supplier<Object> getGetterSupplier(Method getter) {
            return () -> {
                try {
                    return getter.invoke(holder);
                } catch (Exception e) {
                    throw ConfigurationException.wrap(e, ConfigurationErrorCode.ERROR_DURING_GETTER_INVOCATION)
                            .put("class", holder.getClass().getName())
                            .put("getter", getter.getName());
                }
            };
        }

        private Supplier<Object> getFieldSupplier(Field field) {
            return () -> {
                field.setAccessible(true);
                try {
                    return field.get(holder);
                } catch (Exception e) {
                    throw ConfigurationException.wrap(e, ConfigurationErrorCode.ERROR_DURING_FIELD_ACCESS)
                            .put("class", holder.getClass().getName())
                            .put("field", field.getName());
                }
            };
        }
    }
}
