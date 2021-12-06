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

import java.nio.file.AccessMode;
import java.util.Arrays;
import java.util.Objects;

/**
 * An enumerated type representing the modes of a random access content.
 * <p>
 * TODO Replace with {@link AccessMode}.
 * </p>
 */
public enum RandomAccessMode {

    /**
     * Read access mode.
     */
    READ(true, false),

    /**
     * Read-write access mode.
     */
    READWRITE(true, true);

    private final boolean read;
    private final boolean write;

    RandomAccessMode(final boolean read, final boolean write) {
        this.read = read;
        this.write = write;
    }

    /**
     * Converts an array of {@link AccessMode} into a RandomAccessMode.
     *
     * @param accessModes AccessMode array, only {@link AccessMode#READ} and {@link AccessMode#WRITE} are supported.
     * @return A RandomAccessMode.
     * @since 2.10.0
     */
    public static RandomAccessMode from(final AccessMode... accessModes) {
        Objects.requireNonNull(accessModes, "accessModes");
        if (accessModes.length == 0) {
            throw new IllegalArgumentException("Empty AccessMode[].");
        }
        final AccessMode modes[] = accessModes.clone();
        Arrays.sort(modes);
        if (Arrays.binarySearch(modes, AccessMode.WRITE) >= 0) {
            return READWRITE;
        }
        if (Arrays.binarySearch(modes, AccessMode.READ) >= 0) {
            return READ;
        }
        throw new IllegalArgumentException(Arrays.toString(accessModes));
    }

    /**
     * @return The mode String.
     * @since 2.0
     */
    public String getModeString() {
        if (requestRead()) {
            if (requestWrite()) {
                return "rw"; // NON-NLS
            }
            return "r"; // NON-NLS
        }
        if (requestWrite()) {
            return "w"; // NON-NLS
        }

        return "";
    }

    /**
     * Tests read.
     *
     * @return true for read.
     */
    public boolean requestRead() {
        return read;
    }

    /**
     * Tests write.
     *
     * @return true for write.
     */
    public boolean requestWrite() {
        return write;
    }
}
