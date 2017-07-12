/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.util;

import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.spi.ConfigurationComponent;

import java.lang.reflect.Array;
import java.util.Arrays;

public abstract class AbstractComposite<T extends ConfigurationComponent> implements ConfigurationComponent {
    protected final T[] items;
    private final Class<T> itemClass;

    @SafeVarargs
    public AbstractComposite(Class<T> itemClass, T... items) {
        this.itemClass = itemClass;
        this.items = createArray(items.length);
        System.arraycopy(items, 0, this.items, 0, this.items.length);
    }

    @Override
    public void initialize(Coffig coffig) {
        Arrays.stream(items).forEach(item -> item.initialize(coffig));
    }

    @Override
    public void invalidate() {
        Arrays.stream(items).forEach(ConfigurationComponent::invalidate);
    }

    @Override
    public boolean isDirty() {
        return Arrays.stream(items).filter(ConfigurationComponent::isDirty).count() > 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T fork() {
        return doFork(Arrays.stream(items).map(ConfigurationComponent::fork).toArray(this::createArray));
    }

    protected abstract T doFork(T[] items);

    @SuppressWarnings("unchecked")
    public <U extends T> U get(Class<U> itemClass) {
        for (T item : items) {
            if (itemClass.isAssignableFrom(item.getClass())) {
                return (U) item;
            }
        }
        throw ConfigurationException.createNew(ConfigurationErrorCode.SPECIFIED_ITEM_CLASS_NOT_FOUND)
                .put("itemClass", itemClass.getCanonicalName());
    }

    @SuppressWarnings("unchecked")
    private T[] createArray(int length) {
        return (T[]) Array.newInstance(itemClass, length);
    }
}
