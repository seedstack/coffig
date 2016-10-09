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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileProcessor implements ConfigurationProcessor {
    private static final String PROFILE_ATTRIBUTE = "profile";
    private static Pattern keyWithProfilePattern = Pattern.compile("(.*)<(\\w+)>");

    @Override
    public void process(MutableMapNode configuration) {
        Map<MutableMapNode, Map<String, String>> moves = new HashMap<>();

        configuration.stream()
                .filter(node -> node instanceof MutableMapNode)
                .map(node -> (MutableMapNode) node)
                .forEach((MutableMapNode mapNode) -> mapNode.keys().forEach(key -> {
                    Matcher matcher = keyWithProfilePattern.matcher(key);
                    if (matcher.matches()) {
                        mapNode.item(key).attributes().set(PROFILE_ATTRIBUTE, matcher.group(2));
                        Map<String, String> move = moves.get(mapNode);
                        if (move == null) {
                            moves.put(mapNode, move = new HashMap<>());
                        }
                        move.put(matcher.group(0), matcher.group(1));
                    }
                }));

        for (Map.Entry<MutableMapNode, Map<String, String>> movesEntry : moves.entrySet()) {
            for (Map.Entry<String, String> moveEntry : movesEntry.getValue().entrySet()) {
                movesEntry.getKey().move(moveEntry.getKey(), moveEntry.getValue());
            }
        }
    }
}
