package org.seedstack.coffig.data.mutable;

import org.seedstack.coffig.data.TreeNode;

public interface MutableTreeNode {

    default MutableTreeNode set(String prefix, String value) {
        String[] split = prefix.split("\\.", 2);
        MutableTreeNode treeNode = doSet(split[0], value);
        if (treeNode != null && split.length == 2) {
            treeNode.set(split[1], value);
        }
        return treeNode;
    }

    MutableTreeNode doSet(String name, String value);
}
