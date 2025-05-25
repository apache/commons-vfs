/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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

    /**
     * Aborts the current operation.
     *
     * @return true if aborted.
     * @throws IOException If an I/O error occurs
     */
    boolean abort() throws IOException;

    /**
     * Returns an OutputStream through which data can be written to append to a file on the server with the given name.
     *
     * @param relPath The name of the remote file.
     * @return An OutputStream through which the remote file can be appended.
     * @throws IOException If an I/O error occurs.
     */
    OutputStream appendFileStream(String relPath) throws IOException;

    /**
     * There are a few FTPClient methods that do not complete the entire sequence of FTP commands to complete a transaction.
     * These commands require some action by the programmer after the reception of a positive intermediate command. After
     * the programmer's code completes its actions, it must call this method to receive the completion reply from the server
     * and verify the success of the entire transaction.
     *
     * @return true if successfully completed, false if not.
     * @throws IOException If an I/O error occurs.
     */
    boolean completePendingCommand() throws IOException;

    /**
     * Deletes a file on the FTP server.
     *
     * @param relPath The relPath of the file to be deleted.
     * @return true if successfully completed, false if not.
     * @throws IOException If an I/O error occurs.
     */
    boolean deleteFile(String relPath) throws IOException;

    /**
     * Sends the FTP QUIT command to the server, receive the reply, and return the reply code.
     *
     * @throws IOException If an I/O error occurs.
     */
    void disconnect() throws IOException;

    /**
     * Gets the integer value of the reply code of the last FTP reply.
     *
     * @return The integer value of the reply code of the last FTP reply.
     * @throws IOException If an I/O error occurs.
     */
    @SuppressWarnings("unused")
    default int getReplyCode() throws IOException {
        return FTPReply.COMMAND_OK;
    }

    /**
     * Gets the entire text of the last FTP server response exactly as it was received, including all end of line markers in
     * NETASCII format.
     *
     * @return The entire text from the last FTP response as a String.
     * @throws IOException If an I/O error occurs.
     */
    String getReplyString() throws IOException;

    /**
     * Queries the server for a supported feature.
     *
     * @param feature the name of the feature, converted to upper case.
     * @return {@code true} if the feature is present, {@code false} if the feature is not present or the FTP command
     *         failed.
     *
     * @throws IOException If an I/O error occurs.
     * @since 2.8.0
     */
    boolean hasFeature(String feature) throws IOException;

    /**
     * Tests if the client is currently connected to a server.
     *
     * @return true if the client is currently connected to a server, false otherwise.
     * @throws FileSystemException If an I/O error occurs.
     */
    boolean isConnected() throws FileSystemException;

    /**
     * Using the default system autodetect mechanism, obtain a list of file information for the current working directory or
     * for just a single file.
     * <p>
     * TODO This interface should not leak Apache Commons NET types like FTPFile
     * </p>
     *
     * @param relPath The file or directory to list.
     * @return an array of FTPFile.
     * @throws IOException If an I/O error occurs.
     */
    FTPFile[] listFiles(String relPath) throws IOException;

    /**
     * Creates a new subdirectory on the FTP server in the current directory (if a relative pathname is given) or where
     * specified (if an absolute pathname is given).
     *
     * @param relPath The pathname of the directory to create.
     * @return true if successfully completed, false if not.
     * @throws IOException If an I/O error occurs.
     */
    boolean makeDirectory(String relPath) throws IOException;

    /**
     * Sends the MDTM command to get a file's date and time information after file transfer. It is typically more accurate
     * than the {@code "LIST"} command response. Time values are always represented in UTC (GMT), and in the Gregorian
     * calendar regardless of what calendar may have been in use at the date and time the file was last modified.
     * <p>
     * NOTE: not all remote FTP servers support {@code MDTM}.
     * </p>
     *
     * @param relPath The relative path of the file object to execute {@code MDTM} command against
     * @return new {@code Instant} object containing the {@code MDTM} timestamp.
     * @throws IOException If an I/O error occurs.
     * @since 2.8.0
     */
    @SuppressWarnings("unused")
    default Instant mdtmInstant(final String relPath) throws IOException {
        return null;
    }

    /**
     * Removes a directory on the FTP server (if empty).
     *
     * @param relPath The pathname of the directory to remove.
     * @return true if successfully completed, false if not.
     * @throws IOException If an I/O error occurs.
     */
    boolean removeDirectory(String relPath) throws IOException;

    /**
     * Renames a remote file.
     *
     * @param from The name of the remote file to rename.
     * @param to The new name of the remote file.
     * @return true if successfully completed, false if not.
     * @throws IOException If an I/O error occurs.
     */
    boolean rename(String from, String to) throws IOException;

    /**
     * Returns an InputStream from which a named file from the server can be read.
     *
     * @param relPath The name of the remote file.
     * @return An InputStream from which the remote file can be read.
     * @throws IOException If an I/O error occurs.
     */
    InputStream retrieveFileStream(String relPath) throws IOException;

    /**
     * Returns an InputStream from which a named file from the server can be read.
     *
     * @param relPath The name of the remote file.
     * @param bufferSize buffer size.
     * @return An InputStream from which the remote file can be read.
     * @throws IOException If an I/O error occurs.
     */
    default InputStream retrieveFileStream(final String relPath, final int bufferSize) throws IOException {
        // Backward compatibility: no buffer size.
        return retrieveFileStream(relPath);
    }

    /**
     * Returns an InputStream from which a named file from the server can be read.
     *
     * @param relPath The name of the remote file.
     * @param restartOffset restart offset.
     * @return An InputStream from which the remote file can be read.
     * @throws IOException If an I/O error occurs.
     */
    InputStream retrieveFileStream(String relPath, long restartOffset) throws IOException;

    /**
     * Sets the buffer size for buffered data streams.
     *
     * @param bufferSize The size of the buffer.
     * @throws FileSystemException If an I/O error occurs.
     */
    @SuppressWarnings("unused")
    default void setBufferSize(final int bufferSize) throws FileSystemException {
        // Backward compatibility: do nothing.
    }

    /**
     * Returns an OutputStream through which data can be written to store a file on the server using the given name.
     *
     * @param relPath The name to give the remote file.
     * @return An OutputStream through which the remote file can be written.
     * @throws IOException If an I/O error occurs.
     */
    OutputStream storeFileStream(String relPath) throws IOException;

}
