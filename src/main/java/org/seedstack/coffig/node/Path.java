/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import org.seedstack.coffig.TreeNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A path is a configuration tree path. It splits the path in two part.
 * The head and the tail, for instance the following path:
 *
 * <h2>Path with tail</h2>
 *
 * <pre>
 *     app.server.port
 * </pre>
 * will give:
 * <pre>
 *     path.head == "app"
 *     path.tail == Optional.of("server.port")
 *     path.index == -1
 *     path.isArray == false
 * </pre>
 *
 * <h2>Path without tail</h2>
 *
 * <pre>
 *     app
 * </pre>
 * will give:
 * <pre>
 *     path.head == "app"
 *     path.tail == Optional.empty()
 *     path.index == -1
 *     path.isArray == false
 * </pre>
 *
 * <h2>Path represents an array</h2>
 *
 * if the head correspond to an integer the index will be initialize.
 * <pre>
 *     0.custom.key
 * </pre>
 * will give:
 * <pre>
 *     path.head == "0"
 *     path.tail == Optional.of("custom.key")
 *     path.index == 0
 *     path.isArray == true
 * </pre>
 */
class Path {
    private static final Pattern PATH_REGEX = Pattern.compile("(?<!\\\\)" + Pattern.quote("."));
    private static final Pattern SUBSCRIPTION_PATTERN = Pattern.compile("(.*)\\[(\\d+)\\]");

    private final String head;
    private final String tail;
    private final int index;

    /**
     * Constructs a path based on a string.
     *
     * @param path the configuration path
     */
    Path(String path) {
        String[] splitPath = PATH_REGEX.split(path, 2);

        if (splitPath[0].contains("[")) {
            // Check for [ character for performance reasons
            Matcher matcher = SUBSCRIPTION_PATTERN.matcher(splitPath[0]);
            if (matcher.matches()) {
                if (matcher.group(1).isEmpty()) {
                    head = matcher.group(2);
                    tail = splitPath.length > 1 ? splitPath[1] : null;
                    index = Integer.parseInt(head);
                    return;
                } else {
                    head = matcher.group(1).replace("\\", "");
                    tail = String.format("[%s]", matcher.group(2)) + (splitPath.length > 1 ? "." + splitPath[1] : "");
                    index = -1;
                    return;
                }
            }
        }

        // no array subscription pattern
        head = splitPath[0].replace("\\", "");
        tail = splitPath.length > 1 ? splitPath[1] : null;
        index = -1;
    }

    boolean hasHead() {
        return head != null && !head.isEmpty();
    }

    String getHead() {
        if (!hasHead()) {
            throw new IllegalStateException("Path does not have a head");
        }
        return head;
    }

    boolean hasTail() {
        return tail != null && !tail.isEmpty();
    }

    String getTail() {
        if (!hasTail()) {
            throw new IllegalStateException("Path does not have a tail");
        }
        return tail;
    }

    boolean isArray() {
        return index != -1;
    }

    int getIndex() {
        if (!isArray()) {
            throw new IllegalStateException("Path does not denote an array");
        }
        return index;
    }

    TreeNode createNode() {
        TreeNode treeNode;
        if (isArray()) {
            treeNode = new ArrayNode();
        } else {
            treeNode = new MapNode();
        }
        return treeNode;
    }

}