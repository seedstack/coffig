package org.seedstack.coffig.data;

import org.seedstack.coffig.PropertyNotFoundException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Pierre THIROUIN (pierre.thirouin@ext.inetpsa.com)
 */
public abstract class AbstractTreeNode implements TreeNode {

    public TreeNode search(String prefix) {
        String[] split = prefix.split("\\.", 2);
        try {
            TreeNode treeNode = doSearch(split[0]);
            if (split.length == 2) {
                treeNode = treeNode.search(split[1]);
            }
            return treeNode;
        } catch (PropertyNotFoundException e) {
            if (e.getCause() == null) {
                throw new PropertyNotFoundException(prefix);
            } else {
                throw new PropertyNotFoundException(e.getCause(), prefix);
            }
        }
    }

    protected String indent(String s) {
        return Arrays.stream(s.split("\n")).map(line -> "  " + line).collect(Collectors.joining("\n"));
    }

    protected TreeNode doSearch(String name) {
        throw new PropertyNotFoundException(name);
    }

    public TreeNode value(String name) {
        throw new UnsupportedOperationException();
    }

    public String value() {
        throw new UnsupportedOperationException();
    }

    public TreeNode[] values() {
        throw new UnsupportedOperationException();
    }

    public TreeNode merge(TreeNode otherNode) {
        return otherNode;
    }
}
