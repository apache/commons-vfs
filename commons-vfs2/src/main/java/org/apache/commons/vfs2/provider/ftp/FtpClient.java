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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.vfs2.FileSystemException;

/**
 * What VFS expects from an FTP client to provide.
 */
public interface FtpClient {

    boolean abort() throws IOException;

    OutputStream appendFileStream(String relPath) throws IOException;

    boolean completePendingCommand() throws IOException;

    boolean deleteFile(String relPath) throws IOException;

    void disconnect() throws IOException;

    default int getReplyCode() throws IOException {
        return FTPReply.COMMAND_OK;
    }

    String getReplyString() throws IOException;

    /**
     * Queries the server for a supported feature.
     *
     * @param feature the name of the feature, converted to upper case.
     * @return {@code true} if the feature is present, {@code false} if the feature is not present or the FTP command
     *         failed.
     *
     * @throws IOException on error
     * @since 2.8.0
     */
    boolean hasFeature(final String feature) throws IOException;

    boolean isConnected() throws FileSystemException;

    // This interface should not leak Apache Commons NET types like FTPFile
    FTPFile[] listFiles(String relPath) throws IOException;

    boolean makeDirectory(String relPath) throws IOException;

    /**
     * Sends the MDTM command to get a file's date and time information after file transfer. It is typically more
     * accurate than the {@code "LIST"} command response. Time values are always represented in UTC (GMT), and in the
     * Gregorian calendar regardless of what calendar may have been in use at the date and time the file was last
     * modified.
     * <p>
     * NOTE: not all remote FTP servers support {@code MDTM}.
     * </p>
     *
     * @param relPath The relative path of the file object to execute {@code MDTM} command against
     * @return new {@code Instant} object containing the {@code MDTM} timestamp.
     * @throws IOException If the underlying FTP client encountered an error.
     * @since 2.8.0
     */
    default Instant mdtmInstant(final String relPath) throws IOException {
        return null;
    }

    boolean removeDirectory(String relPath) throws IOException;

    boolean rename(String oldName, String newName) throws IOException;

    InputStream retrieveFileStream(String relPath) throws IOException;

    default InputStream retrieveFileStream(final String relPath, final int bufferSize) throws IOException {
        // Backward compatibility: no buffer size.
        return retrieveFileStream(relPath);
    }

    InputStream retrieveFileStream(String relPath, long restartOffset) throws IOException;

    default void setBufferSize(final int bufferSize) throws FileSystemException {
        // Backward compatibility: do nothing.
    }

    OutputStream storeFileStream(String relPath) throws IOException;

}
