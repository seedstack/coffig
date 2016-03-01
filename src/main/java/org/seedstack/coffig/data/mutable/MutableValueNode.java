package org.seedstack.coffig.data.mutable;

import org.seedstack.coffig.data.ValueNode;

public class MutableValueNode extends ValueNode {
    public MutableValueNode(String value) {
        super(value);
    }

    public MutableValueNode() {
        super(null);
    }

    public void setValue(String value) {
        this.value = value;
    }
}
