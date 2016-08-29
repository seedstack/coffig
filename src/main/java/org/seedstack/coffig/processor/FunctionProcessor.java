/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.processor;

import org.seedstack.coffig.ConfigurationException;
import org.seedstack.coffig.LRUCache;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.mapper.MapperFactory;
import org.seedstack.coffig.node.MutableMapNode;
import org.seedstack.coffig.node.MutableValueNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationProcessor;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionProcessor implements ConfigurationProcessor {
    private static final Pattern CALL_SITE_PATTERN = Pattern.compile("\\$([_a-zA-Z]\\w*)\\(|\\)");
    private final ConcurrentMap<String, FunctionRegistration> functions = new ConcurrentHashMap<>();
    private final LRUCache<String, String> cache = new LRUCache<>(10000);
    private final MapperFactory mapperFactory;

    public FunctionProcessor(MapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
    }

    @Override
    public void process(MutableMapNode configuration) {
        configuration.stream()
                .filter(node -> node instanceof MutableValueNode)
                .forEach((valueNode) -> ((MutableValueNode) valueNode).value(processValue(configuration, valueNode.value())));
    }

    public void registerFunction(String name, Method method, Object instance) {
        if (functions.putIfAbsent(name, new FunctionRegistration(method, instance)) != null) {
            throw new IllegalStateException("Function " + name + " already registered");
        } else {
            method.setAccessible(true);
        }
    }

    private String processValue(TreeNode tree, String value) {
        // Try the cache before doing any processing
        String cachedResult = cache.get(value);
        if (cachedResult != null) {
            return cachedResult;
        }

        int currentPos = 0;
        StringBuilder result = new StringBuilder();
        CallSiteInfo callSiteInfo;

        // Iterate through all call sites in the value
        while ((callSiteInfo = findFunctionCall(value, currentPos)) != null) {
            result.append(value.substring(currentPos, callSiteInfo.start));
            result.append(invokeFunction(
                    callSiteInfo.name,
                    Arrays.stream(callSiteInfo.arguments).map(arg -> processArgument(tree, arg)).toArray(TreeNode[]::new)
            ));
            currentPos = callSiteInfo.end;
        }
        result.append(value.substring(currentPos));

        // Populate the cache with the result
        cachedResult = result.toString();
        cache.put(value, cachedResult);

        return cachedResult;
    }

    private TreeNode processArgument(TreeNode tree, String value) {
        if (value.startsWith("'") && value.endsWith("'")) {
            return new ValueNode(value.substring(1, value.length() - 1));
        }

        CallSiteInfo callSiteInfo = findFunctionCall(value, 0);
        if (callSiteInfo == null) {
            TreeNode refNode = tree.get(value).orElse(new ValueNode(""));
            if (refNode instanceof ValueNode) {
                // References value nodes can be processed...
                return new ValueNode(processValue(tree, refNode.value()));
            } else {
                // ... whereas other node types are passed directly
                return refNode;
            }
        } else {
            return new ValueNode(processValue(tree, value));
        }
    }

    private String invokeFunction(String functionName, TreeNode[] arguments) {
        FunctionRegistration functionRegistration = functions.get(functionName);
        if (functionRegistration == null) {
            throw new IllegalArgumentException("Unknown configuration function " + functionName);
        }

        try {
            // Map arguments according to the function parameter types
            Object[] mappedArguments = new Object[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                mappedArguments[i] = mapperFactory.map(arguments[i], functionRegistration.argTypes[i]);
            }

            // Invoke the function
            Object result = functionRegistration.method.invoke(functionRegistration.instance, mappedArguments);
            if (result != null) {
                return result.toString();
            } else {
                return "";
            }
        } catch (Exception e) {
            throw new ConfigurationException("Error when invoking function " + functionName, e);
        }
    }

    private CallSiteInfo findFunctionCall(String value, int startIndex) {
        int level = 0, argumentPos = 0;
        Matcher matcher = CALL_SITE_PATTERN.matcher(value);
        CallSiteInfo callSiteInfo = new CallSiteInfo();
        while (matcher.find()) {
            if (matcher.start() < startIndex) {
                continue;
            }

            if (matcher.group(1) != null) {
                if (level == 0) {
                    callSiteInfo.name = matcher.group(1).trim();
                    callSiteInfo.start = matcher.start();
                    argumentPos = matcher.end(1) + 1;
                }
                level++;
            } else {
                level--;
                if (level == 0) {
                    String allArgs = value.substring(argumentPos, matcher.start()).trim();
                    if (allArgs.isEmpty()) {
                        callSiteInfo.arguments = new String[0];
                    } else {
                        callSiteInfo.arguments = Arrays.stream(allArgs.split(",")).map(String::trim).toArray(String[]::new);
                    }
                    callSiteInfo.end = matcher.end();
                    return callSiteInfo;
                }
            }
        }
        return null;
    }

    private static class CallSiteInfo {
        private int start;
        private int end;
        private String name;
        private String[] arguments;
    }

    private static class FunctionRegistration {
        private final Method method;
        private final Object instance;
        private final Type[] argTypes;

        private FunctionRegistration(Method method, Object instance) {
            this.method = method;
            this.instance = instance;
            this.argTypes = method.getGenericParameterTypes();
        }
    }
}
