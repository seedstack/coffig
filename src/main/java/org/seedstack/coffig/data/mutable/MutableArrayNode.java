package org.seedstack.coffig.data.mutable;

import org.seedstack.coffig.data.ArrayNode;
import org.seedstack.coffig.data.TreeNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MutableArrayNode extends ArrayNode {
    public MutableArrayNode(TreeNode... childNodes) {
        super(childNodes);
    }

    public MutableArrayNode(String... childNodes) {
        super(childNodes);
    }

    public MutableArrayNode(List<TreeNode> childNodes) {
        super(childNodes);
    }

    public MutableArrayNode() {
        super(new ArrayList<>());
    }

    public void add(TreeNode treeNode) {
        childNodes.add(treeNode);
    }

    public void add(int index, TreeNode treeNode) {
        childNodes.add(index, treeNode);
    }

    public boolean addAll(Collection<? extends TreeNode> c) {
        return childNodes.addAll(c);
    }

    public void remove(TreeNode treeNode) {
        childNodes.remove(treeNode);
    }

    public void clear() {
        childNodes.clear();
    }
}
