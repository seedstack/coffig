/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.evaluator;

import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationEvaluator;
import org.seedstack.coffig.utils.LRUCache;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacroEvaluator implements ConfigurationEvaluator {
    private static final Pattern MACRO_PATTERN = Pattern.compile("\\$\\{|\\}");
    private final LRUCache<String, String> cache = new LRUCache<>(10000);

    @Override
    public void invalidate() {
        cache.clear();
    }

    @Override
    public MacroEvaluator fork() {
        return new MacroEvaluator();
    }

    @Override
    public ValueNode evaluate(TreeNode rootNode, ValueNode valueNode) {
        return new ValueNode(processValue(rootNode, valueNode.value()));
    }

    private String processValue(TreeNode rootNode, String value) {
        String cachedResult = cache.get(value);
        if (cachedResult != null) {
            return cachedResult;
        }

        StringBuilder result = new StringBuilder();
        int currentPos = 0;
        int[] indices;

        // Process all macros
        while ((indices = findMatchingCurlyBraces(value, currentPos)) != null) {
            // Add the beginning of the string (before the macro)
            result.append(value.substring(currentPos, indices[0]));

            // Process the macro
            for (String part : value.substring(indices[0] + 2, indices[1]).split(":")) {
                if (part.startsWith("'") && part.endsWith("'")) {
                    result.append(part.substring(1, part.length() - 1));
                    break;
                } else {
                    Optional<TreeNode> node = rootNode.get(processValue(rootNode, part));
                    if (node.isPresent()) {
                        result.append(node.get().value());
                        break;
                    }
                }
            }
            currentPos = indices[1] + 1;
        }

        // Add the remaining of the string (after all macros)
        result.append(value.substring(currentPos));

        cachedResult = result.toString();
        cache.put(value, cachedResult);

        return cachedResult;
    }

    private int[] findMatchingCurlyBraces(String value, int startIndex) {
        int level = 0, startPos = -1;
        Matcher matcher = MACRO_PATTERN.matcher(value);
        while (matcher.find()) {
            if (matcher.start() < startIndex) {
                continue;
            }
            switch (matcher.group()) {
                case "${":
                    if (level == 0) {
                        startPos = matcher.start();
                    }
                    level++;
                    break;
                case "}":
                    level--;
                    if (level == 0) {
                        return new int[]{startPos, matcher.start()};
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected string in macro " + matcher.group());
            }
        }
        return null;
    }
}
