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
package org.apache.commons.vfs2.provider.tftp;

/**
 * The tftp file types.
 *
 * @since 2.1
 */
public enum TftpFileType {

    /**
     * The ASCII file type.
     */
    NETASCII(0),

    /**
     * The binary file type.
     */
    OCTET(2),

    /**
     * The local file type.
     */
    LOCAL(3);

    /**
     * The Apache Commons Net tftp file type.
     */
    private final int value;

    /**
     * Constructs a file type.
     *
     * @param fileType The Apache Commons Net tftp file type.
     */
    TftpFileType(final int fileType) {
        this.value = fileType;
    }

    /**
     * Gets the Apache Commons Net TFTP file type.
     *
     * @return The Apache Commons Net TFTP file type.
     */
    int getValue() {
        return this.value;
    }
}
