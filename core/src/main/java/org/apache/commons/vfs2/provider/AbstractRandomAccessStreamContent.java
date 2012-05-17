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
 * Implements the part usable for all stream base random access implementations.
 */
public abstract class AbstractRandomAccessStreamContent extends AbstractRandomAccessContent
{
    protected AbstractRandomAccessStreamContent(final RandomAccessMode mode)
    {
        super(mode);
    }

    protected abstract DataInputStream getDataInputStream() throws IOException;

    public byte readByte() throws IOException
    {
        return getDataInputStream().readByte();
    }

    public char readChar() throws IOException
    {
        return getDataInputStream().readChar();
    }

    public double readDouble() throws IOException
    {
        return getDataInputStream().readDouble();
    }

    public float readFloat() throws IOException
    {
        return getDataInputStream().readFloat();
    }

    public int readInt() throws IOException
    {
        return getDataInputStream().readInt();
    }

    public int readUnsignedByte() throws IOException
    {
        return getDataInputStream().readUnsignedByte();
    }

    public int readUnsignedShort() throws IOException
    {
        return getDataInputStream().readUnsignedShort();
    }

    public long readLong() throws IOException
    {
        return getDataInputStream().readLong();
    }

    public short readShort() throws IOException
    {
        return getDataInputStream().readShort();
    }

    public boolean readBoolean() throws IOException
    {
        return getDataInputStream().readBoolean();
    }

    public int skipBytes(int n) throws IOException
    {
        return getDataInputStream().skipBytes(n);
    }

    public void readFully(byte[] b) throws IOException
    {
        getDataInputStream().readFully(b);
    }

    public void readFully(byte[] b, int off, int len) throws IOException
    {
        getDataInputStream().readFully(b, off, len);
    }

    public String readUTF() throws IOException
    {
        return getDataInputStream().readUTF();
    }

    public InputStream getInputStream() throws IOException
    {
        return getDataInputStream();
    }
    
    public void setLength(long newLength) throws IOException
    {
        throw new UnsupportedOperationException();        
    }
}
