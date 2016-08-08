/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.processor;

import org.seedstack.coffig.MutableTreeNode;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.MutableMapNode;
import org.seedstack.coffig.node.MutableValueNode;
import org.seedstack.coffig.spi.ConfigurationProcessor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacroProcessor implements ConfigurationProcessor {
    @Override
    public void process(MutableMapNode configuration) {
        configuration.stream()
                .filter(node -> node instanceof MutableValueNode)
                .forEach((valueNode) -> ((MutableValueNode) valueNode).value(processValue(configuration, valueNode.value())));
    }

    private String processValue(MutableTreeNode tree, String value) {
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
                    Optional<TreeNode> node = tree.get(processValue(tree, part));
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

        return result.toString();
    }

    private int[] findMatchingCurlyBraces(String value, int startIndex) {
        Pattern pattern = Pattern.compile("\\$\\{|\\}");
        int level = 0, startPos = -1;
        Matcher matcher = pattern.matcher(value);
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
