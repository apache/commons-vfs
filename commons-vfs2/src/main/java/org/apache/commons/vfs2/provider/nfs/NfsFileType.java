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
package org.apache.commons.vfs2.provider.nfs;

/**
 * The NFS file types.
 *
 * @since 2.1
 */
public enum NfsFileType {

    /**
     * The ASCII file type.
     */
    ASCII(0),

    /**
     * The binary file type.
     */
    BINARY(2),

    /**
     * The local file type.
     */
    LOCAL(3),

    /**
     * The EBCDIC file type.
     */
    EBCDIC(1);

    /**
     * The Apache Commons Net NFS file type.
     */
    private final int value;

    /**
     * Constructs a file type.
     *
     * @param fileType The Apache Commons Net NFS file type.
     */
    NfsFileType(final int fileType) {
        this.value = fileType;
    }

    /**
     * Gets the Apache Commons Net NFS file type.
     *
     * @return The Apache Commons Net SMB file type.
     */
    int getValue() {
        return this.value;
    }
}
