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
package org.apache.commons.vfs2;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.Map;

import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * Represents the data content of a file.
 * <p>
 * To read from a file, use the {@code InputStream} returned by {@link #getInputStream()}.
 * </p>
 * <p>
 * To write to a file, use the {@code OutputStream} returned by {@link #getOutputStream()} method. This will create the
 * file, and the parent folder, if necessary.
 * </p>
 * <p>
 * A file may have multiple InputStreams open at the same time.
 * </p>
 *
 * @see FileObject#getContent
 */
public interface FileContent extends Closeable {
    /**
     * Returns the file which this is the content of.
     *
     * @return The FileObject this is the content of.
     */
    FileObject getFile();

    /**
     * Determines the size of the file, in bytes.
     *
     * @return The size of the file, in bytes.
     * @throws FileSystemException If the file does not exist, or is being written to, or on error determining the size.
     */
    long getSize() throws FileSystemException;

    /**
     * Determines the last-modified timestamp of the file.
     *
     * @return The last-modified timestamp.
     * @throws FileSystemException If the file does not exist, or is being written to, or on error determining the
     *             last-modified timestamp.
     */
    long getLastModifiedTime() throws FileSystemException;

    /**
     * Sets the last-modified timestamp of the file. Creates the file if it does not exist.
     *
     * @param modTime The time to set the last-modified timestamp to.
     * @throws FileSystemException If the file is read-only, or is being written to, or on error setting the
     *             last-modified timestamp.
     */
    void setLastModifiedTime(long modTime) throws FileSystemException;

    /**
     * Checks if an attribute of the file's content exists.
     *
     * @param attrName The name of the attribute.
     * @return true if the attribute exists, false otherwise.
     * @throws FileSystemException If the file does not exist, or does not support attributes.
     */
    boolean hasAttribute(String attrName) throws FileSystemException;

    /**
     * Returns a read-only map of this file's attributes.
     *
     * @return The attribute Map.
     * @throws FileSystemException If the file does not exist, or does not support attributes.
     */
    Map<String, Object> getAttributes() throws FileSystemException;

    /**
     * Lists the attributes of the file's content.
     *
     * @return The names of the attributes. Never returns null;
     * @throws FileSystemException If the file does not exist, or does not support attributes.
     */
    String[] getAttributeNames() throws FileSystemException;

    /**
     * Gets the value of an attribute of the file's content.
     *
     * @param attrName The name of the attribute. Attribute names are case insensitive.
     * @return The value of the attribute, or null if the attribute value is unknown.
     * @throws FileSystemException If the file does not exist, or does not support attributes.
     */
    Object getAttribute(String attrName) throws FileSystemException;

    /**
     * Sets the value of an attribute of the file's content. Creates the file if it does not exist.
     *
     * @param attrName The name of the attribute.
     * @param value The value of the attribute.
     * @throws FileSystemException If the file does not exist, or is read-only, or does not support attributes, or on
     *             error setting the attribute.
     */
    void setAttribute(String attrName, Object value) throws FileSystemException;

    /**
     * Removes the value of an attribute of the file's content.
     *
     * @param attrName The name of the attribute.
     * @throws FileSystemException If the file does not exist, or is read-only, or does not support attributes, or on
     *             error removing the attribute.
     */
    void removeAttribute(String attrName) throws FileSystemException;

    /**
     * Retrieves the certificates if any used to sign this file or folder.
     *
     * @return The certificates, or an empty array if there are no certificates or the file does not support signing.
     * @throws FileSystemException If the file does not exist, or is being written.
     */
    Certificate[] getCertificates() throws FileSystemException;

    /**
     * Returns an input stream for reading the file's content.
     * <p>
     * There may only be a single input or output stream open for the file at any time.
     *
     * @return An input stream to read the file's content from. The input stream is buffered, so there is no need to
     *         wrap it in a {@code BufferedInputStream}.
     * @throws FileSystemException If the file does not exist, or is being read, or is being written, or on error
     *             opening the stream.
     */
    InputStream getInputStream() throws FileSystemException;

    /**
     * Returns an output stream for writing the file's content.
     * <p>
     * If the file does not exist, this method creates it, and the parent folder, if necessary. If the file does exist,
     * it is replaced with whatever is written to the output stream.
     * <p>
     * There may only be a single input or output stream open for the file at any time.
     *
     * @return An output stream to write the file's content to. The stream is buffered, so there is no need to wrap it
     *         in a {@code BufferedOutputStream}.
     * @throws FileSystemException If the file is read-only, or is being read, or is being written, or on error opening
     *             the stream.
     */
    OutputStream getOutputStream() throws FileSystemException;

    /**
     * Returns an stream for reading/writing the file's content.
     * <p>
     * If the file does not exist, and you use one of the write* methods, this method creates it, and the parent folder,
     * if necessary. If the file does exist, parts of the file are replaced with whatever is written at a given
     * position.
     * <p>
     * There may only be a single input or output stream open for the file at any time.
     *
     * @param mode The mode to use to access the file.
     * @return the stream for reading and writing the file's content.
     * @throws FileSystemException If the file is read-only, or is being read, or is being written, or on error opening
     *             the stream.
     */
    RandomAccessContent getRandomAccessContent(final RandomAccessMode mode) throws FileSystemException;

    /**
     * Returns an output stream for writing the file's content.
     * <p>
     * If the file does not exist, this method creates it, and the parent folder, if necessary. If the file does exist,
     * it is replaced with whatever is written to the output stream.
     * <p>
     * There may only be a single input or output stream open for the file at any time.
     *
     * @param bAppend true if you would like to append to the file. This may not be supported by all implementations.
     * @return An output stream to write the file's content to. The stream is buffered, so there is no need to wrap it
     *         in a {@code BufferedOutputStream}.
     * @throws FileSystemException If the file is read-only, or is being read, or is being written, or bAppend is true
     *             and the implementation does not support it, or on error opening the stream.
     */
    OutputStream getOutputStream(boolean bAppend) throws FileSystemException;

    /**
     * Closes all resources used by the content, including any open stream. Commits pending changes to the file.
     * <p>
     * This method is a hint to the implementation that it can release resources. This object can continue to be used
     * after calling this method.
     *
     * @throws FileSystemException if an error occurs closing the file.
     */
    @Override
    void close() throws FileSystemException;

    /**
     * get the content info. e.g. type, encoding, ...
     *
     * @return the FileContentInfo
     * @throws FileSystemException if an error occurs.
     */
    FileContentInfo getContentInfo() throws FileSystemException;

    /**
     * check if this file has open streams.
     *
     * @return true if the file is open, false otherwise.
     */
    boolean isOpen();

    /**
     * Writes this content to another FileContent.
     *
     * @param output The target OutputStream.
     * @throws IOException if an error occurs writing the content.
     * @return the total number of bytes written
     * @since 2.1
     */
    long write(FileContent output) throws IOException;

    /**
     * Writes this content to another FileObject.
     *
     * @param file The target FileObject.
     * @throws IOException if an error occurs writing the content.
     * @return the total number of bytes written
     * @since 2.1
     */
    long write(FileObject file) throws IOException;

    /**
     * Writes this content to an OutputStream.
     *
     * @param output The target OutputStream.
     * @return the total number of bytes written
     * @throws IOException if an error occurs writing the content.
     * @since 2.1
     */
    long write(OutputStream output) throws IOException;

    /**
     * Writes this content to an OutputStream.
     *
     * @param output The target OutputStream.
     * @param bufferSize The buffer size to write data chunks.
     * @return the total number of bytes written
     * @throws IOException if an error occurs writing the file.
     * @since 2.1
     */
    long write(OutputStream output, int bufferSize) throws IOException;

}
