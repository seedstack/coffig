/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.node;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PathTest {
    @Test
    public void testPath() throws Exception {
        Path path = new Path("a.b.c.d");
        assertThat(path.hasHead()).isTrue();
        assertThat(path.hasTail()).isTrue();
        assertThat(path.getHead()).isEqualTo("a");
        assertThat(path.getTail()).isEqualTo("b.c.d");
    }

    @Test
    public void testEmptyPath() throws Exception {
        Path path = new Path("");
        assertThat(path.hasHead()).isFalse();
        assertThat(path.hasTail()).isFalse();
    }

    @Test
    public void testPathWithSubscriptionTail() throws Exception {
        Path path = new Path("a[5]");
        assertThat(path.hasHead()).isTrue();
        assertThat(path.hasTail()).isTrue();
        assertThat(path.getHead()).isEqualTo("a");
        assertThat(path.getTail()).isEqualTo("[5]");
        assertThat(path.isArray()).isFalse();
    }

    @Test
    public void testPathWithSubscriptionHead() throws Exception {
        Path path = new Path("[5]");
        assertThat(path.hasHead()).isTrue();
        assertThat(path.hasTail()).isFalse();
        assertThat(path.getHead()).isEqualTo("5");
        assertThat(path.isArray()).isTrue();
        assertThat(path.getIndex()).isEqualTo(5);
    }

    @Test
    public void testPathWithoutTail() throws Exception {
        Path path = new Path("a");
        assertThat(path.hasHead()).isTrue();
        assertThat(path.hasTail()).isFalse();
        assertThat(path.getHead()).isEqualTo("a");
    }

    @Test(expected = IllegalStateException.class)
    public void testPathSubscriptionThrows() throws Exception {
        Path path = new Path("a");
        path.getIndex();
    }

    @Test(expected = IllegalStateException.class)
    public void testPathWithoutTailShouldThrow() throws Exception {
        Path path = new Path("a");
        path.getTail();
    }

    @Test(expected = IllegalStateException.class)
    public void testPathWithoutHeadShouldThrow() throws Exception {
        Path path = new Path("");
        path.getHead();
    }

    @Test
    public void testPathEscaping() throws Exception {
        Path path = new Path("a\\.b\\.c\\.d");
        assertThat(path.hasHead()).isTrue();
        assertThat(path.hasTail()).isFalse();
        assertThat(path.getHead()).isEqualTo("a.b.c.d");
    }
}