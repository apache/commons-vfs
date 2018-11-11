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
package org.apache.commons.vfs2.provider;

import org.apache.commons.vfs2.FileSystemOptions;

/**
 * Used to identify a filesystem
 */
class FileSystemKey implements Comparable<FileSystemKey> {

    private static final FileSystemOptions EMPTY_OPTIONS = new FileSystemOptions();

    private final Comparable<?> key;
    private final FileSystemOptions fileSystemOptions;

    /**
     * Create the FS key.
     *
     * @param key must implement Comparable, and must be self-comparable
     * @param fileSystemOptions the required options
     */
    FileSystemKey(final Comparable<?> key, final FileSystemOptions fileSystemOptions) {
        this.key = key;
        if (fileSystemOptions != null) {
            this.fileSystemOptions = fileSystemOptions;
        } else {
            this.fileSystemOptions = EMPTY_OPTIONS;
        }
    }

    @Override
    public int compareTo(final FileSystemKey o) {
        @SuppressWarnings("unchecked") // Keys must implement comparable, and be comparable to themselves
        final Comparable<Comparable<?>> comparable = (Comparable<Comparable<?>>) key;
        final int ret = comparable.compareTo(o.key);
        if (ret != 0) {
            // other filesystem
            return ret;
        }

        return fileSystemOptions.compareTo(o.fileSystemOptions);
    }

    @Override
    public String toString() {
        return super.toString() + " [key=" + key + ", fileSystemOptions=" + fileSystemOptions + "]";
    }
}
