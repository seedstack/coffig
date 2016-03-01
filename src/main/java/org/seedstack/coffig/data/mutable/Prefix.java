package org.seedstack.coffig.data.mutable;

import java.util.Optional;

class Prefix {
    String head;
    Optional<String> tail;
    int index;
    boolean isArray;

    public Prefix(String prefix) {
        String[] splitPrefix = prefix.split("\\.", 2);
        head = splitPrefix[0];
        setTail(splitPrefix);
        setIndex();
    }

    private void setTail(String[] splitPrefix) {
        if (splitPrefix.length == 2) {
            tail = Optional.of(splitPrefix[1]);
        } else {
            tail = Optional.empty();
        }
    }

    private void setIndex() {
        try {
            index = Integer.valueOf(head);
            if (index >= 0) {
                isArray = true;
            } else {
                throw new ArrayIndexOutOfBoundsException();
            }
        } catch (NumberFormatException e) {
            isArray = false;
        }
    }
}