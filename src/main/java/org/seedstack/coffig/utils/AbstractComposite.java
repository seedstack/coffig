/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.utils;

import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.spi.ConfigurationComponent;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractComposite<T extends ConfigurationComponent> implements ConfigurationComponent {
    protected final Map<Class<?>, T> items = new LinkedHashMap<>();
    protected Coffig coffig;
    protected boolean dirty = true;

    @SafeVarargs
    public AbstractComposite(T... items) {
        Arrays.stream(items).forEachOrdered(this::add);
    }

    @Override
    public void initialize(Coffig coffig) {
        this.coffig = coffig;
        items.values().forEach(item -> item.initialize(coffig));
    }

    @Override
    public void invalidate() {
        items.values().forEach(ConfigurationComponent::invalidate);
    }

    @Override
    public boolean isDirty() {
        return dirty || items.values().stream().filter(ConfigurationComponent::isDirty).count() > 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T fork() {
        AbstractComposite fork = (AbstractComposite) doFork();
        items.values().stream().map(ConfigurationComponent::fork).forEach(fork::add);
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

    @SuppressWarnings("unchecked")
    public <U extends T> U get(Class<U> item) {
        return (U) items.get(item);
    }
}
