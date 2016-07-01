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

public class MacroProcessor implements ConfigurationProcessor {
    @Override
    public void process(MutableMapNode configuration) {
        configuration.stream()
                .filter(node -> node instanceof MutableValueNode)
                .forEach((valueNode) -> ((MutableValueNode) valueNode).value(evaluate(configuration, valueNode.value())));
    }

    private String evaluate(MutableTreeNode tree, String value) {
        int startIndex = value.indexOf("${");
        int endIndex = value.lastIndexOf("}");

        if (startIndex != -1 && endIndex != -1) {
            String result = "";

            for (String part : value.substring(startIndex + 2, endIndex).split(":")) {
                if (part.startsWith("'") && part.endsWith("'")) {
                    result = part.substring(1, part.length() - 1);
                    break;
                } else {
                    Optional<TreeNode> node = tree.get(evaluate(tree, part));
                    if (node.isPresent()) {
                        result = node.get().value();
                        break;
                    }
                }
            }

            return value.substring(0, startIndex) + result + value.substring(endIndex + 1);
        } else {
            return value;
        }
    }
}
