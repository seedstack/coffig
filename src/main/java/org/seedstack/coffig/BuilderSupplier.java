/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig;

import java.util.function.Supplier;

@FunctionalInterface
public interface BuilderSupplier<T> extends Supplier<T> {
    static <T> BuilderSupplier<T> of(T instance) {
        return new SimpleBuilderSupplier<>(instance);
    }

    class SimpleBuilderSupplier<T> implements BuilderSupplier<T> {
        private final T instance;

        private SimpleBuilderSupplier(T instance) {
            this.instance = instance;
        }

        @Override
        public T get() {
            return instance;
        }
    }
}
