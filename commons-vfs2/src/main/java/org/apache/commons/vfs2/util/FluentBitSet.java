/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.util;

import java.util.BitSet;

/** Move to Apache Commons Lang. */
final class FluentBitSet {

    private final BitSet bitSet;

    FluentBitSet(final BitSet bitSet) {
        this.bitSet = bitSet;
    }

    FluentBitSet(final int nbits) {
        this(new BitSet(nbits));
    }

    FluentBitSet andNot(final FluentBitSet fBitSet) {
        this.bitSet.andNot(fBitSet.bitSet);
        return this;
    }

    BitSet bitSet() {
        return bitSet;
    }

    FluentBitSet clear(final int... bitIndexArray) {
        for (final int e : bitIndexArray) {
            this.bitSet.clear(e);
        }
        return this;
    }

    boolean get(final int bitIndex) {
        return bitSet.get(bitIndex);
    }

    FluentBitSet or(final FluentBitSet... fBitSets) {
        for (final FluentBitSet e : fBitSets) {
            this.bitSet.or(e.bitSet);
        }
        return this;
    }

    FluentBitSet or(final FluentBitSet fBitSet) {
        this.bitSet.or(fBitSet.bitSet);
        return this;
    }

    FluentBitSet set(final int... bitIndexArray) {
        for (final int e : bitIndexArray) {
            bitSet.set(e);
        }
        return this;
    }

    FluentBitSet set(final int bitIndex) {
        bitSet.set(bitIndex);
        return this;
    }

    FluentBitSet setInclusive(final int startIncl, final int endIncl) {
        bitSet.set(startIncl, endIncl + 1);
        return this;
    }

}
