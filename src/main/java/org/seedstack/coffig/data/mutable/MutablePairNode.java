package org.seedstack.coffig.data.mutable;

import org.seedstack.coffig.data.PairNode;
import org.seedstack.coffig.data.TreeNode;

public class MutablePairNode extends PairNode {
    public MutablePairNode(String name, TreeNode value) {
        super(name, value);
    }

    public MutablePairNode(String name, String value) {
        super(name, value);
    }

    public MutablePairNode(String name, String... values) {
        super(name, values);
    }

    public MutablePairNode() {
        super(null, (TreeNode) null);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(TreeNode value) {
        this.value = value;
    }
}
