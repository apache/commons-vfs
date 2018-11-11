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
package org.apache.commons.vfs2.provider.ftp;

import org.apache.commons.net.ftp.FTP;

/**
 * The FTP file types.
 *
 * @since 2.1
 */
public enum FtpFileType {
    /**
     * The ASCII file type.
     */
    ASCII(FTP.ASCII_FILE_TYPE),

    /**
     * The binary file type.
     */
    BINARY(FTP.BINARY_FILE_TYPE),

    /**
     * The local file type.
     */
    LOCAL(FTP.LOCAL_FILE_TYPE),

    /**
     * The EBCDIC file type.
     */
    EBCDIC(FTP.EBCDIC_FILE_TYPE);

    /**
     * The Apache Commons Net FTP file type.
     */
    private final int value;

    /**
     * Constructs a file type.
     *
     * @param fileType The Apache Commons Net FTP file type.
     */
    private FtpFileType(final int fileType) {
        this.value = fileType;
    }

    /**
     * Gets the Apache Commons Net FTP file type.
     *
     * @return The Apache Commons Net FTP file type.
     */
    int getValue() {
        return this.value;
    }
}
