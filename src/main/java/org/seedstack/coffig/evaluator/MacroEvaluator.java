/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.evaluator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MacroEvaluator implements ConfigurationEvaluator {
    public static final String VALUE_QUOTE = "'";
    public static final String VALUE_SEPARATOR = ":";
    private static final Logger LOGGER = LoggerFactory.getLogger(MacroEvaluator.class);
    private static final Pattern MACRO_PATTERN = Pattern.compile("\\\\?\\$\\{|}");

    @Override
    public MacroEvaluator fork() {
        return new MacroEvaluator();
    }

    @Override
    public TreeNode evaluate(TreeNode rootNode, TreeNode valueNode) {
        if (valueNode.type() == TreeNode.Type.VALUE_NODE && !valueNode.isEmpty()) {
            try {
                return new ValueNode(processValue(rootNode, valueNode.value()));
            } catch (Exception e) {
                LOGGER.error("Error when evaluating configuration macro: {}", valueNode.value(), e);
                return new ValueNode();
            }
        } else {
            return valueNode;
        }
    }

    private String processValue(TreeNode rootNode, String value) {
        if (value == null) {
            return null;
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
                boolean insideQuotes = false;
                for (String part : value.substring(matchingResult.startPos + 2, matchingResult.endPos)
                        .split(VALUE_SEPARATOR)) {
                    if (part.startsWith(VALUE_QUOTE) && part.endsWith(VALUE_QUOTE)) {
                        result.append(part.substring(1, part.length() - 1));
                        break;
                    } else if (!insideQuotes && part.startsWith(VALUE_QUOTE)) {
                        result.append(part.substring(1));
                        insideQuotes = true;
                    } else if (insideQuotes && part.endsWith(VALUE_QUOTE)) {
                        result.append(VALUE_SEPARATOR).append(part.substring(0, part.length() - 1));
                        insideQuotes = false;
                    } else if (insideQuotes) {
                        result.append(VALUE_SEPARATOR).append(part);
                    } else {
                        Optional<TreeNode> node = rootNode.get(processValue(rootNode, part));
                        if (node.isPresent()) {
                            result.append(processValue(rootNode, node.get().value()));
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

        return result.toString();
    }

    @SuppressFBWarnings("SF_SWITCH_FALLTHROUGH")
    private MatchingResult findMatchingCurlyBraces(String value, int startIndex) {
        int level = 0;
        int startPos = -1;
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
                    // falls through
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
