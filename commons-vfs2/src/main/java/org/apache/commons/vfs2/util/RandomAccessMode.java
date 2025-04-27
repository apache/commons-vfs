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

import org.apache.commons.lang3.ArraySorter;

/**
 * An enumerated type representing the modes of a random access content.
 * <p>
 * TODO Replace with {@link AccessMode}.
 * </p>
 */
public enum RandomAccessMode {

    /**
     * The read access mode.
     */
    READ(true, false) {

        /**
         * Returns a defensive copy of an internal constant array.
         */
        @Override
        public AccessMode[] toAccessModes() {
            return ACCESS_MODE_READ.clone();
        }
    },

    /**
     * The read-write access mode.
     */
    READWRITE(true, true) {

        /**
         * Returns a defensive copy of an internal constant array.
         */
        @Override
        public AccessMode[] toAccessModes() {
            return ACCESS_MODE_READ_WRITE.clone();
        }
    };

    private static final AccessMode[] ACCESS_MODE_READ = {AccessMode.READ};
    private static final AccessMode[] ACCESS_MODE_READ_WRITE = {AccessMode.READ, AccessMode.WRITE};

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
        final AccessMode[] modes = ArraySorter.sort(accessModes.clone());
        if (Arrays.binarySearch(modes, AccessMode.WRITE) >= 0) {
            return READWRITE;
        }
        if (Arrays.binarySearch(modes, AccessMode.READ) >= 0) {
            return READ;
        }
        throw new IllegalArgumentException(Arrays.toString(accessModes));
    }
    private final boolean read;

    private final boolean write;

    RandomAccessMode(final boolean read, final boolean write) {
        this.read = read;
        this.write = write;
    }

    /**
     * Gets this instance as an access mode string suitable for other APIs, like {@code "r"} for {@link #READ} and
     * {@code "rw"} for {@link #READWRITE}.
     *
     * @return An access mode String, {@code "r"} for {@link #READ} and {@code "rw"} for {@link #READWRITE}.
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
     * Tests the read flag.
     *
     * @return true for read.
     */
    public boolean requestRead() {
        return read;
    }

    /**
     * Tests the write flag.
     *
     * @return true for write.
     */
    public boolean requestWrite() {
        return write;
    }

    /**
     * Converts this instance to an array of {@link AccessMode}.
     *
     * @return an array of {@link AccessMode}.
     * @since 2.10.0
     */
    public AccessMode[] toAccessModes() {
        // TODO If this method is abstract, JApiCmp reports:
        // METHOD_ABSTRACT_ADDED_TO_CLASS,org.apache.commons.vfs2.util.RandomAccessMode:CLASS_NOW_ABSTRACT
        return null;
    }

}
