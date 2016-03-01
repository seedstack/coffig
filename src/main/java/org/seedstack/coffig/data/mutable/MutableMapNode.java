package org.seedstack.coffig.data.mutable;

import org.seedstack.coffig.data.MapNode;
import org.seedstack.coffig.data.PairNode;

import java.util.HashMap;
import java.util.Map;

public class MutableMapNode extends MapNode implements MutableTreeNode {

    public MutableMapNode(PairNode... childNodes) {
        super(childNodes);
    }

    public MutableMapNode(Map<String, PairNode> newChildNodes) {
        super(newChildNodes);
    }

    public MutableMapNode() {
        super(new HashMap<>());
    }

    public PairNode put(String key, PairNode value) {
        return childNodes.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends PairNode> m) {
        childNodes.putAll(m);
    }

    public PairNode remove(Object key) {
        return childNodes.remove(key);
    }

    public void clear() {
        childNodes.clear();
    }

    @Override
    public MutableTreeNode doSet(String name, String value) {
        if (childNodes.containsKey(name)) {

        } else {
            childNodes.put(name, )
        }
        return null;
    }
}
