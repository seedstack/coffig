/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.processor;

import org.seedstack.coffig.node.MutableMapNode;
import org.seedstack.coffig.spi.ConfigurationProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemovalProcessor implements ConfigurationProcessor {
    @Override
    public void process(MutableMapNode configuration) {
        Map<MutableMapNode, List<String>> toRemove = new HashMap<>();

        configuration.stream()
                .filter(node -> node instanceof MutableMapNode)
                .forEach(mapNode -> {
                    ((MutableMapNode) mapNode).keys().stream().filter(key -> key.startsWith("-")).forEach(key -> {
                        List<String> strings = toRemove.get(mapNode);
                        if (strings == null) {
                            toRemove.put((MutableMapNode) mapNode, strings = new ArrayList<>());
                        }
                        strings.add(key);
                        strings.add(key.substring(1));
                    });
                });

        toRemove.forEach((node, list) -> list.forEach(node::remove));
    }
}
