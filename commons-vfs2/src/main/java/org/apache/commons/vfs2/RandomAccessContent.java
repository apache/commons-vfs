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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides random access over content.
 */
public interface RandomAccessContent extends DataOutput, DataInput {
    /**
     * Closes this random access file stream and releases any system resources associated with the stream.
     * <p>
     * A closed random access file cannot perform input or output operations and cannot be reopened.
     * </p>
     * <p>
     * If this file has an associated channel then the channel is closed as well.
     * <p>
     *
     * @throws IOException if an I/O error occurs.
     */
    void close() throws IOException;

    /**
     * Returns the current offset in this file.
     *
     * @return the offset from the beginning of the file, in bytes, at which the next read or write occurs.
     * @throws IOException if an I/O error occurs.
     */
    long getFilePointer() throws IOException;

    /**
     * Get the input stream.
     * <p>
     * <b>Notice: If you use {@link #seek(long)} you have to re-get the InputStream</b>
     * </p>
     *
     * @return the InputStream.
     * @throws IOException if an I/O error occurs.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns the length of this file.
     *
     * @return the length of this file, measured in bytes.
     * @throws IOException if an I/O error occurs.
     */
    long length() throws IOException;

    /**
     * Sets the file-pointer offset, measured from the beginning of this file, at which the next read or write occurs.
     * <p>
     * The offset may be set beyond the end of the file. Setting the offset beyond the end of the file does not change
     * the file length. The file length will change only by writing after the offset has been set beyond the end of the
     * file.
     * </p>
     * <p>
     * <b>Notice: If you use {@link #getInputStream()} you have to re-get the InputStream after calling
     * {@link #seek(long)}</b>
     * </p>
     *
     * @param pos the offset position, measured in bytes from the beginning of the file, at which to set the file
     *            pointer.
     * @throws IOException if {@code pos} is less than {@code 0} or if an I/O error occurs.
     */
    void seek(long pos) throws IOException;

    /**
     * Sets the length of this content.
     *
     * <p>
     * If the the {@code newLength} argument is smaller than {@link #length()}, the content is truncated.
     * </p>
     *
     * <p>
     * If the the {@code newLength} argument is greater than {@link #length()}, the content grows with undefined data.
     * </p>
     *
     * @param newLength The desired content length
     * @throws IOException If an I/O error occurs
     * @since 2.1
     */
    void setLength(long newLength) throws IOException;

}
