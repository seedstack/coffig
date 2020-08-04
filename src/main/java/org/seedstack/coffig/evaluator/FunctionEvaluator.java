/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.evaluator;

import static java.util.stream.Collectors.toList;
import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigFunction;
import org.seedstack.coffig.spi.ConfigFunctionHolder;
import org.seedstack.coffig.spi.ConfigurationComponent;
import org.seedstack.coffig.spi.ConfigurationEvaluator;
import org.seedstack.shed.ClassLoaders;
import org.seedstack.shed.exception.Throwing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionEvaluator implements ConfigurationEvaluator {
    private static final ClassLoader MOST_COMPLETE_CLASS_LOADER = ClassLoaders.findMostCompleteClassLoader
            (FunctionEvaluator.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionEvaluator.class);
    private static final Pattern CALL_SITE_PATTERN = Pattern.compile("\\\\?\\$([_a-zA-Z]\\w*)\\(|\\)");
    private final AtomicBoolean scanned = new AtomicBoolean();
    private final List<ConfigFunctionHolder> configFunctionHolders = new ArrayList<>();
    private final ConcurrentMap<String, FunctionRegistration> functions = new ConcurrentHashMap<>();
    private Coffig coffig;

    @Override
    public void initialize(Coffig coffig) {
        this.coffig = coffig;
        if (!scanned.getAndSet(true)) {
            for (ConfigFunctionHolder configFunctionHolder : ServiceLoader.load(ConfigFunctionHolder.class,
                    MOST_COMPLETE_CLASS_LOADER)) {
                configFunctionHolders.add(configFunctionHolder);
                detectFunctionsOfHolder(configFunctionHolder);
            }
        }
        for (ConfigFunctionHolder configFunctionHolder : configFunctionHolders) {
            configFunctionHolder.initialize(coffig);
        }
    }

    @Override
    public void invalidate() {
        configFunctionHolders.forEach(ConfigurationComponent::invalidate);
    }

    @Override
    public boolean isDirty() {
        return configFunctionHolders.stream().anyMatch(ConfigurationComponent::isDirty);
    }

    @Override
    public FunctionEvaluator fork() {
        FunctionEvaluator fork = new FunctionEvaluator();
        fork.configFunctionHolders.addAll(configFunctionHolders.stream().map(ConfigurationComponent::fork)
                .map(ConfigFunctionHolder.class::cast).collect(toList()));
        fork.functions.putAll(functions);
        fork.scanned.getAndSet(scanned.get());
        return fork;
    }

    @Override
    public TreeNode evaluate(TreeNode rootNode, TreeNode valueNode) {
        if (valueNode.type() == TreeNode.Type.VALUE_NODE && !valueNode.isEmpty()) {
            try {
                return new ValueNode(processValue(rootNode, valueNode.value()));
            } catch (Exception e) {
                LOGGER.error("Error when evaluating configuration function: {}", valueNode.value(), e);
                return new ValueNode();
            }
        } else {
            return valueNode;
        }
    }

    private void detectFunctionsOfHolder(ConfigFunctionHolder configFunctionHolder) {
        for (Method method : configFunctionHolder.getClass().getDeclaredMethods()) {
            ConfigFunction annotation = method.getAnnotation(ConfigFunction.class);
            if (annotation != null) {
                registerFunction(annotation.value().isEmpty() ? method.getName() : annotation.value(), method,
                        configFunctionHolder);
            }
        }
    }

    public void registerFunction(String name, Method method, Object instance) {
        if (functions.putIfAbsent(name, new FunctionRegistration(method, instance)) != null) {
            throw new IllegalStateException("Function " + name + " already registered");
        } else {
            makeAccessible(method);
        }
    }

    private String processValue(TreeNode rootNode, String value) throws Exception {
        int currentPos = 0;
        StringBuilder result = new StringBuilder();
        CallSiteInfo callSiteInfo;

        // Iterate through all call sites in the value
        while ((callSiteInfo = findFunctionCall(value, currentPos)) != null) {
            result.append(value.substring(currentPos, callSiteInfo.startPos));

            if (callSiteInfo.escaped) {
                result.append(value.substring(callSiteInfo.startPos + 1, callSiteInfo.endPos));
            } else {
                result.append(invokeFunction(
                        callSiteInfo.name,
                        Arrays.stream(callSiteInfo.arguments)
                                .map((Throwing.Function<String, TreeNode, Exception>) arg ->
                                        processArgument(rootNode, arg))
                                .toArray(TreeNode[]::new)
                ));
            }

            currentPos = callSiteInfo.endPos;
        }
        result.append(value.substring(currentPos));

        return result.toString();
    }

    private TreeNode processArgument(TreeNode tree, String value) throws Exception {
        if (value.startsWith("'") && value.endsWith("'")) {
            return new ValueNode(value.substring(1, value.length() - 1));
        }

        CallSiteInfo callSiteInfo = findFunctionCall(value, 0);
        if (callSiteInfo == null) {
            TreeNode refNode = tree.get(value).orElse(new ValueNode(""));
            if (refNode.type() == TreeNode.Type.VALUE_NODE) {
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

    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    private String invokeFunction(String functionName, TreeNode[] arguments) throws Exception {
        FunctionRegistration functionRegistration = functions.get(functionName);
        if (functionRegistration == null) {
            throw new IllegalArgumentException("Unknown function " + functionName);
        }

        try {
            // Map arguments according to the function parameter types
            Object[] mappedArguments = new Object[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                mappedArguments[i] = coffig.getMapper().map(arguments[i], functionRegistration.argTypes[i]);
            }

            // Invoke the function
            Object result = functionRegistration.method.invoke(functionRegistration.instance, mappedArguments);
            if (result != null) {
                return result.toString();
            } else {
                return "";
            }
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable targetException = ((InvocationTargetException) e).getTargetException();
                if (targetException != null && targetException instanceof Exception) {
                    throw ((Exception) targetException);
                }
            }
            throw e;
        }
    }

    private CallSiteInfo findFunctionCall(String value, int startIndex) {
        int level = 0;
        int argumentPos = 0;
        Matcher matcher = CALL_SITE_PATTERN.matcher(value);
        CallSiteInfo callSiteInfo = new CallSiteInfo();
        while (matcher.find()) {
            if (matcher.start() < startIndex) {
                continue;
            }

            if (matcher.group(1) != null) {
                if (matcher.group().startsWith("\\")) {
                    callSiteInfo.escaped = true;
                }
                if (level == 0) {
                    callSiteInfo.name = matcher.group(1).trim();
                    callSiteInfo.startPos = matcher.start();
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
                        callSiteInfo.arguments = Arrays.stream(allArgs.split(",")).map(String::trim)
                                .toArray(String[]::new);
                    }
                    callSiteInfo.endPos = matcher.end();
                    return callSiteInfo;
                }
            }
        }
        return null;
    }

    private static class CallSiteInfo {
        private int startPos;
        private int endPos;
        private boolean escaped;
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
