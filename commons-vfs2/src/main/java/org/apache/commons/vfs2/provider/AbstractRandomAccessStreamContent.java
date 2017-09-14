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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * Implements the part usable for all stream-based random access.
 */
public abstract class AbstractRandomAccessStreamContent extends AbstractRandomAccessContent {
    protected AbstractRandomAccessStreamContent(final RandomAccessMode mode) {
        super(mode);
    }

    protected abstract DataInputStream getDataInputStream() throws IOException;

    @Override
    public byte readByte() throws IOException {
        return getDataInputStream().readByte();
    }

    @Override
    public char readChar() throws IOException {
        return getDataInputStream().readChar();
    }

    @Override
    public double readDouble() throws IOException {
        return getDataInputStream().readDouble();
    }

    @Override
    public float readFloat() throws IOException {
        return getDataInputStream().readFloat();
    }

    @Override
    public int readInt() throws IOException {
        return getDataInputStream().readInt();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return getDataInputStream().readUnsignedByte();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return getDataInputStream().readUnsignedShort();
    }

    @Override
    public long readLong() throws IOException {
        return getDataInputStream().readLong();
    }

    @Override
    public short readShort() throws IOException {
        return getDataInputStream().readShort();
    }

    @Override
    public boolean readBoolean() throws IOException {
        return getDataInputStream().readBoolean();
    }

    @Override
    public int skipBytes(final int n) throws IOException {
        return getDataInputStream().skipBytes(n);
    }

    @Override
    public void readFully(final byte[] b) throws IOException {
        getDataInputStream().readFully(b);
    }

    @Override
    public void readFully(final byte[] b, final int off, final int len) throws IOException {
        getDataInputStream().readFully(b, off, len);
    }

    @Override
    public String readUTF() throws IOException {
        return getDataInputStream().readUTF();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getDataInputStream();
    }

    @Override
    public void setLength(final long newLength) throws IOException {
        throw new UnsupportedOperationException();
    }
}
