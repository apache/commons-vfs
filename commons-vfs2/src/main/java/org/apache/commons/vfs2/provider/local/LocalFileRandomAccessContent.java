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
package org.apache.commons.vfs2.provider.local;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractRandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * Implements {@link org.apache.commons.vfs2.RandomAccessContent RandomAccessContent} for local files.
 */
class LocalFileRandomAccessContent extends AbstractRandomAccessContent {
    // private final LocalFile localFile;
    private final RandomAccessFile raf;
    private final InputStream rafis;

    LocalFileRandomAccessContent(final File localFile, final RandomAccessMode mode) throws FileSystemException {
        super(mode);

        try {
            raf = new RandomAccessFile(localFile, mode.getModeString());
            rafis = new InputStream() {
                @Override
                public int read() throws IOException {
                    try {
                        return raf.readByte();
                    } catch (final EOFException e) {
                        return -1;
                    }
                }

                @Override
                public long skip(final long n) throws IOException {
                    raf.seek(raf.getFilePointer() + n);
                    return n;
                }

                @Override
                public void close() throws IOException {
                    raf.close();
                }

                @Override
                public int read(final byte[] b) throws IOException {
                    return raf.read(b);
                }

                @Override
                public int read(final byte[] b, final int off, final int len) throws IOException {
                    return raf.read(b, off, len);
                }

                @Override
                public int available() throws IOException {
                    final long available = raf.length() - raf.getFilePointer();
                    if (available > Integer.MAX_VALUE) {
                        return Integer.MAX_VALUE;
                    }

                    return (int) available;
                }
            };
        } catch (final FileNotFoundException e) {
            throw new FileSystemException("vfs.provider/random-access-open-failed.error", localFile);
        }
    }

    @Override
    public long getFilePointer() throws IOException {
        return raf.getFilePointer();
    }

    @Override
    public void seek(final long pos) throws IOException {
        raf.seek(pos);
    }

    @Override
    public long length() throws IOException {
        return raf.length();
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

    @Override
    public byte readByte() throws IOException {
        return raf.readByte();
    }

    @Override
    public char readChar() throws IOException {
        return raf.readChar();
    }

    @Override
    public double readDouble() throws IOException {
        return raf.readDouble();
    }

    @Override
    public float readFloat() throws IOException {
        return raf.readFloat();
    }

    @Override
    public int readInt() throws IOException {
        return raf.readInt();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return raf.readUnsignedByte();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return raf.readUnsignedShort();
    }

    @Override
    public long readLong() throws IOException {
        return raf.readLong();
    }

    @Override
    public short readShort() throws IOException {
        return raf.readShort();
    }

    @Override
    public boolean readBoolean() throws IOException {
        return raf.readBoolean();
    }

    @Override
    public int skipBytes(final int n) throws IOException {
        return raf.skipBytes(n);
    }

    @Override
    public void readFully(final byte[] b) throws IOException {
        raf.readFully(b);
    }

    @Override
    public void readFully(final byte[] b, final int off, final int len) throws IOException {
        raf.readFully(b, off, len);
    }

    @Override
    public String readUTF() throws IOException {
        return raf.readUTF();
    }

    @Override
    public void writeDouble(final double v) throws IOException {
        raf.writeDouble(v);
    }

    @Override
    public void writeFloat(final float v) throws IOException {
        raf.writeFloat(v);
    }

    @Override
    public void write(final int b) throws IOException {
        raf.write(b);
    }

    @Override
    public void writeByte(final int v) throws IOException {
        raf.writeByte(v);
    }

    @Override
    public void writeChar(final int v) throws IOException {
        raf.writeChar(v);
    }

    @Override
    public void writeInt(final int v) throws IOException {
        raf.writeInt(v);
    }

    @Override
    public void writeShort(final int v) throws IOException {
        raf.writeShort(v);
    }

    @Override
    public void writeLong(final long v) throws IOException {
        raf.writeLong(v);
    }

    @Override
    public void writeBoolean(final boolean v) throws IOException {
        raf.writeBoolean(v);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        raf.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        raf.write(b, off, len);
    }

    @Override
    public void writeBytes(final String s) throws IOException {
        raf.writeBytes(s);
    }

    @Override
    public void writeChars(final String s) throws IOException {
        raf.writeChars(s);
    }

    @Override
    public void writeUTF(final String str) throws IOException {
        raf.writeUTF(str);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return rafis;
    }

    @Override
    public void setLength(final long newLength) throws IOException {
        raf.setLength(newLength);
    }
}
