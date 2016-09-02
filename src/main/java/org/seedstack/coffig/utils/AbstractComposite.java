/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.utils;

import org.seedstack.coffig.spi.ChangeDetectable;
import org.seedstack.coffig.spi.Forkable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractComposite<T extends ChangeDetectable & Forkable> implements ChangeDetectable, Forkable {
    protected final Map<Class<?>, T> items = new LinkedHashMap<>();
    protected volatile boolean dirty = true;

    @SafeVarargs
    public AbstractComposite(T... items) {
        Arrays.stream(items).forEachOrdered(this::add);
    }

    @Override
    public boolean isDirty() {
        return dirty || items.values().stream().filter(ChangeDetectable::isDirty).count() > 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T fork() {
        AbstractComposite fork = (AbstractComposite) doFork();
        for (T item : items.values()) {
            fork.add((T) item.fork());
        }
        return (T) fork;
    }

    protected abstract T doFork();

    public void clear() {
        items.clear();
        dirty = true;
    }

    public void add(T item) {
        items.put(item.getClass(), item);
        dirty = true;
    }

    public T remove(Class<T> item) {
        T removed = items.remove(item);
        if (removed != null) {
            dirty = true;
        }
        return removed;
    }

    public T get(Class<T> item) {
        return items.get(item);
    }
}
