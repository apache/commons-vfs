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

/**
 * An enumerated type representing the modes of a random access content.
 */
public enum RandomAccessMode {
    /**
     * read.
     */
    READ(true, false),

    /**
     * read/write.
     */
    READWRITE(true, true);

    private final boolean read;
    private final boolean write;

    private RandomAccessMode(final boolean read, final boolean write) {
        this.read = read;
        this.write = write;
    }

    public boolean requestRead() {
        return read;
    }

    public boolean requestWrite() {
        return write;
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
        } else if (requestWrite()) {
            return "w"; // NON-NLS
        }

        return "";
    }
}
