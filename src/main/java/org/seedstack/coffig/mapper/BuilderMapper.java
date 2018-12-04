/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.mapper;

import static org.seedstack.shed.reflect.Classes.instantiateDefault;
import static org.seedstack.shed.reflect.Types.rawClassOf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.seedstack.coffig.BuilderSupplier;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.spi.ConfigurationMapper;

public class BuilderMapper implements ConfigurationMapper {
    private Coffig coffig;

    @Override
    public void initialize(Coffig coffig) {
        this.coffig = coffig;
    }

    @Override
    public boolean canHandle(Type type) {
        return type != null && BuilderSupplier.class.isAssignableFrom(rawClassOf(type));
    }

    @Override
    public Object map(TreeNode treeNode, Type type) {
        BuilderSupplier<?> supplier = instantiateBuilderSupplier(type);
        Object builder = supplier.get();
        Map<String, Method> builderMethods = getBuilderMethods(builder);

        treeNode.namedNodes().forEach(namedNode -> {
            Method method = builderMethods.get(namedNode.name());
            if (method != null) {
                try {
                    method.invoke(builder,
                            coffig.getMapper().map(namedNode.node(), method.getGenericParameterTypes()[0]));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw ConfigurationException.wrap(e, ConfigurationErrorCode.ERROR_DURING_METHOD_INVOCATION)
                            .put("method", method.toString());
                }
            }
        });

        return supplier;
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        throw ConfigurationException.createNew(ConfigurationErrorCode.UNMAPPING_IS_NOT_SUPPORTED)
                .put("type", type.getTypeName());
    }

    @SuppressWarnings("unchecked")
    private BuilderSupplier<?> instantiateBuilderSupplier(Type type) {
        Class<?> rawClass = rawClassOf(type);
        if (rawClass.equals(BuilderSupplier.class)) {
            // If the type equals to the supplier interface, instantiate a default supplier
            return BuilderSupplier.of(
                    instantiateDefault(rawClassOf(((ParameterizedType) type).getActualTypeArguments()[0]))
            );
        } else {
            // Otherwise try to instantiate the supplier
            return (BuilderSupplier<?>) instantiateDefault(rawClass);
        }
    }

    private Map<String, Method> getBuilderMethods(Object builder) {
        Map<String, Method> methods = new HashMap<>();
        Arrays.stream(builder.getClass().getMethods()).forEach(method -> methods.put(method.getName(), method));
        return methods;
    }

}
