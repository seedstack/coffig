/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import java.util.Optional;
import java.util.stream.Stream;
import org.seedstack.coffig.TreeNode;

public class UnmodifiableTreeNode implements TreeNode {
    private final TreeNode treeNode;

    private UnmodifiableTreeNode(TreeNode treeNode) {
        if (treeNode == null) {
            throw new IllegalArgumentException("Null tree node not allowed");
        }
        this.treeNode = treeNode;
    }

    public static TreeNode of(TreeNode treeNode) {
        if (treeNode instanceof UnmodifiableTreeNode) {
            return treeNode;
        } else {
            return new UnmodifiableTreeNode(treeNode);
        }
    }

    public static NamedNode of(NamedNode namedNode) {
        if (namedNode.node() instanceof UnmodifiableTreeNode) {
            return namedNode;
        } else {
            return new NamedNode(namedNode.name(), new UnmodifiableTreeNode(namedNode.node()));
        }
    }

    @Override
    public boolean isHidden() {
        return treeNode.isHidden();
    }

    @Override
    public void hide() {
        throw new UnsupportedOperationException("Attempt to alter an unmodifiable tree node");
    }

    @Override
    public Type type() {
        return treeNode.type();
    }

    @Override
    public String value() {
        return treeNode.value();
    }

    @Override
    public Stream<TreeNode> nodes() {
        return treeNode.nodes().map(UnmodifiableTreeNode::of);
    }

    @Override
    public Stream<NamedNode> namedNodes() {
        return treeNode.namedNodes()
                .map(namedNode -> new NamedNode(namedNode.name(), UnmodifiableTreeNode.of(namedNode.node())));
    }

    @Override
    public TreeNode node(String key) {
        return UnmodifiableTreeNode.of(treeNode.node(key));
    }

    @Override
    public Optional<TreeNode> get(String path) {
        return treeNode.get(path).map(UnmodifiableTreeNode::of);
    }

    @Override
    public Stream<TreeNode> walk() {
        return treeNode.walk().map(UnmodifiableTreeNode::of);
    }

    @Override
    public boolean isEmpty() {
        return treeNode.isEmpty();
    }

    @Override
    public TreeNode merge(TreeNode otherNode) {
        throw new UnsupportedOperationException("Attempt to alter an unmodifiable tree node");
    }

    @Override
    public TreeNode set(String path, TreeNode value) {
        throw new UnsupportedOperationException("Attempt to alter an unmodifiable tree node");
    }

    @Override
    public TreeNode remove(String path) {
        throw new UnsupportedOperationException("Attempt to alter an unmodifiable tree node");
    }

    @Override
    public TreeNode move(String sourcePath, String destinationPath) {
        throw new UnsupportedOperationException("Attempt to alter an unmodifiable tree node");
    }

    @Override
    public int hashCode() {
        return treeNode.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof UnmodifiableTreeNode) {
            return treeNode.equals(((UnmodifiableTreeNode) o).treeNode);
        } else {
            return o instanceof TreeNode && treeNode.equals(o);
        }
    }

    @Override
    public String toString() {
        return treeNode.toString();
    }
}
