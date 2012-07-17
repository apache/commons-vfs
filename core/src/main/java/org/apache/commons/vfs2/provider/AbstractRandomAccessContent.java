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

import java.io.DataOutput;
import java.io.IOException;

import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * Implements the {@link DataOutput} part of the {@link RandomAccessContent} interface and throws
 * {@link UnsupportedOperationException} when one of these methods are called. For read-only random access
 * implementations.
 */
public abstract class AbstractRandomAccessContent implements RandomAccessContent
{
    private final RandomAccessMode mode;

    protected AbstractRandomAccessContent(final RandomAccessMode mode)
    {
        this.mode = mode;
    }

    /**
     * @deprecated see {@link java.io.DataInputStream#readLine()} This method will be removed when it is removed from
     *             the DataInput interface this class implements (which will probably never happen).
     * @return The line as a String.
     * @throws IOException
     *             if an error occurs.
     */
    @Override
    @Deprecated
    public String readLine() throws IOException
    {
        throw new UnsupportedOperationException("deprecated");
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(int b) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeBoolean(boolean v) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeByte(int v) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeBytes(String s) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeChar(int v) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeChars(String s) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeDouble(double v) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeFloat(float v) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeInt(int v) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeLong(long v) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeShort(int v) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeUTF(String str) throws IOException
    {
        throw new UnsupportedOperationException();
    }
}
