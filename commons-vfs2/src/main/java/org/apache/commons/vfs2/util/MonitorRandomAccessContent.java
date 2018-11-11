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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.RandomAccessContent;

/**
 * A RandomAccessContent that provides end-of-stream monitoring.
 */
public class MonitorRandomAccessContent implements RandomAccessContent {
    private final RandomAccessContent content;
    private boolean finished;

    public MonitorRandomAccessContent(final RandomAccessContent content) {
        this.content = content;
    }

    /**
     * Called after this stream is closed.
     *
     * @throws IOException if subclass throws it.
     */
    @SuppressWarnings("unused") // IOException is needed because subclasses may need to throw it
    protected void onClose() throws IOException {
    }

    /**
     * Closes this content.
     *
     * @throws IOException if an error occurs.
     */
    @Override
    public void close() throws IOException {
        if (finished) {
            return;
        }

        // Close the output stream
        IOException exc = null;
        try {
            content.close();
        } catch (final IOException ioe) {
            exc = ioe;
        }

        // Notify of end of output
        exc = null;
        try {
            onClose();
        } catch (final IOException ioe) {
            exc = ioe;
        }

        finished = true;

        if (exc != null) {
            throw exc;
        }
    }

    @Override
    public long getFilePointer() throws IOException {
        return content.getFilePointer();
    }

    @Override
    public void seek(final long pos) throws IOException {
        content.seek(pos);
    }

    @Override
    public long length() throws IOException {
        return content.length();
    }

    @Override
    public void write(final int b) throws IOException {
        content.write(b);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        content.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        content.write(b, off, len);
    }

    @Override
    public void writeBoolean(final boolean v) throws IOException {
        content.writeBoolean(v);
    }

    @Override
    public void writeByte(final int v) throws IOException {
        content.writeByte(v);
    }

    @Override
    public void writeShort(final int v) throws IOException {
        content.writeShort(v);
    }

    @Override
    public void writeChar(final int v) throws IOException {
        content.writeChar(v);
    }

    @Override
    public void writeInt(final int v) throws IOException {
        content.writeInt(v);
    }

    @Override
    public void writeLong(final long v) throws IOException {
        content.writeLong(v);
    }

    @Override
    public void writeFloat(final float v) throws IOException {
        content.writeFloat(v);
    }

    @Override
    public void writeDouble(final double v) throws IOException {
        content.writeDouble(v);
    }

    @Override
    public void writeBytes(final String s) throws IOException {
        content.writeBytes(s);
    }

    @Override
    public void writeChars(final String s) throws IOException {
        content.writeChars(s);
    }

    @Override
    public void writeUTF(final String str) throws IOException {
        content.writeUTF(str);
    }

    @Override
    public void readFully(final byte[] b) throws IOException {
        content.readFully(b);
    }

    @Override
    public void readFully(final byte[] b, final int off, final int len) throws IOException {
        content.readFully(b, off, len);
    }

    @Override
    public int skipBytes(final int n) throws IOException {
        return content.skipBytes(n);
    }

    @Override
    public void setLength(final long newLength) throws IOException {
        content.setLength(newLength);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return content.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return content.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return content.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return content.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return content.readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return content.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return content.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return content.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return content.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return content.readDouble();
    }

    @Override
    public String readLine() throws IOException {
        return content.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return content.readUTF();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return content.getInputStream();
    }

}
