/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.evaluator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationEvaluator;
import org.seedstack.coffig.util.LRUCache;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacroEvaluator implements ConfigurationEvaluator {
    private static final Pattern MACRO_PATTERN = Pattern.compile("\\\\?\\$\\{|\\}");
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
    public TreeNode evaluate(TreeNode rootNode, TreeNode valueNode) {
        if (valueNode.type() == TreeNode.Type.VALUE_NODE) {
            return new ValueNode(processValue(rootNode, valueNode.value()));
        } else {
            return valueNode;
        }
    }

    private String processValue(TreeNode rootNode, String value) {
        String cachedResult = cache.get(value);
        if (cachedResult != null) {
            return cachedResult;
        }

        StringBuilder result = new StringBuilder();
        int currentPos = 0;
        MatchingResult matchingResult;

        // Process all macros
        while ((matchingResult = findMatchingCurlyBraces(value, currentPos)) != null) {
            // Add the beginning of the string (before the macro)
            result.append(value.substring(currentPos, matchingResult.startPos));

            if (matchingResult.escaped) {
                // Add the macro as-is (without resolving it)
                result.append(value.substring(matchingResult.startPos + 1, matchingResult.endPos + 1));
            } else {
                // Process the macro
                for (String part : value.substring(matchingResult.startPos + 2, matchingResult.endPos).split(":")) {
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
            }

            // Advance currentPos to after the macro
            currentPos = matchingResult.endPos + 1;
        }

        // Add the remaining of the string (after all macros)
        result.append(value.substring(currentPos));

        cachedResult = result.toString();
        cache.put(value, cachedResult);

        return cachedResult;
    }

    @SuppressFBWarnings("SF_SWITCH_FALLTHROUGH")
    private MatchingResult findMatchingCurlyBraces(String value, int startIndex) {
        int level = 0, startPos = -1;
        boolean escaped = false;
        Matcher matcher = MACRO_PATTERN.matcher(value);
        while (matcher.find()) {
            if (matcher.start() < startIndex) {
                continue;
            }
            switch (matcher.group()) {
                case "\\${":
                    if (level == 0) {
                        escaped = true;
                    }
                case "${":
                    if (level == 0) {
                        startPos = matcher.start();
                    }
                    level++;
                    break;
                case "}":
                    level--;
                    if (level == 0) {
                        return new MatchingResult(startPos, matcher.start(), escaped);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected string in macro " + matcher.group());
            }
        }
        return null;
    }

    private static class MatchingResult {
        final int startPos;
        final int endPos;
        final boolean escaped;

        MatchingResult(int startPos, int endPos, boolean escaped) {
            this.startPos = startPos;
            this.endPos = endPos;
            this.escaped = escaped;
        }
    }
}
