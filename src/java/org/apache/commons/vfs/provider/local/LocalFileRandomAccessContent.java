/*
 * Copyright 2002-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.provider.local;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractRandomAccessContent;
import org.apache.commons.vfs.util.RandomAccessMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.InputStream;
import java.io.EOFException;

/**
 * RandomAccess for local files
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
class LocalFileRandomAccessContent extends AbstractRandomAccessContent
{
    // private final LocalFile localFile;
    final private RandomAccessFile raf;
    final private InputStream rafis;

    LocalFileRandomAccessContent(final File localFile, final RandomAccessMode mode) throws FileSystemException
    {
        super(mode);

        StringBuffer modes = new StringBuffer(2);
        if (mode.requestRead())
        {
            modes.append('r');
        }
        if (mode.requestWrite())
        {
            modes.append('w');
        }

        try
        {
            raf = new RandomAccessFile(localFile, modes.toString());
            rafis = new InputStream()
            {
                public int read() throws IOException
                {
                    try
                    {
                        return raf.readByte();
                    }
                    catch (EOFException e)
                    {
                        return -1;
                    }
                }

                public long skip(long n) throws IOException
                {
                    raf.seek(raf.getFilePointer() + n);
                    return n;
                }

                public void close() throws IOException
                {
                    raf.close();
                }

                public int read(byte b[]) throws IOException
                {
                    return raf.read(b);
                }

                public int read(byte b[], int off, int len) throws IOException
                {
                    return raf.read(b, off, len);
                }
            };
        }
        catch (FileNotFoundException e)
        {
            throw new FileSystemException("vfs.provider/random-access-open-failed.error", localFile);
        }
    }

    public long getFilePointer() throws IOException
    {
        return raf.getFilePointer();
    }

    public void seek(long pos) throws IOException
    {
        raf.seek(pos);
    }

    public long length() throws IOException
    {
        return raf.length();
    }

    public void close() throws IOException
    {
        raf.close();
    }

    public byte readByte() throws IOException
    {
        return raf.readByte();
    }

    public char readChar() throws IOException
    {
        return raf.readChar();
    }

    public double readDouble() throws IOException
    {
        return raf.readDouble();
    }

    public float readFloat() throws IOException
    {
        return raf.readFloat();
    }

    public int readInt() throws IOException
    {
        return raf.readInt();
    }

    public int readUnsignedByte() throws IOException
    {
        return raf.readUnsignedByte();
    }

    public int readUnsignedShort() throws IOException
    {
        return raf.readUnsignedShort();
    }

    public long readLong() throws IOException
    {
        return raf.readLong();
    }

    public short readShort() throws IOException
    {
        return raf.readShort();
    }

    public boolean readBoolean() throws IOException
    {
        return raf.readBoolean();
    }

    public int skipBytes(int n) throws IOException
    {
        return raf.skipBytes(n);
    }

    public void readFully(byte b[]) throws IOException
    {
        raf.readFully(b);
    }

    public void readFully(byte b[], int off, int len) throws IOException
    {
        raf.readFully(b, off, len);
    }

    public String readUTF() throws IOException
    {
        return raf.readUTF();
    }

    public void writeDouble(double v) throws IOException
    {
        raf.writeDouble(v);
    }

    public void writeFloat(float v) throws IOException
    {
        raf.writeFloat(v);
    }

    public void write(int b) throws IOException
    {
        raf.write(b);
    }

    public void writeByte(int v) throws IOException
    {
        raf.writeByte(v);
    }

    public void writeChar(int v) throws IOException
    {
        raf.writeChar(v);
    }

    public void writeInt(int v) throws IOException
    {
        raf.writeInt(v);
    }

    public void writeShort(int v) throws IOException
    {
        raf.writeShort(v);
    }

    public void writeLong(long v) throws IOException
    {
        raf.writeLong(v);
    }

    public void writeBoolean(boolean v) throws IOException
    {
        raf.writeBoolean(v);
    }

    public void write(byte b[]) throws IOException
    {
        raf.write(b);
    }

    public void write(byte b[], int off, int len) throws IOException
    {
        raf.write(b, off, len);
    }

    public void writeBytes(String s) throws IOException
    {
        raf.writeBytes(s);
    }

    public void writeChars(String s) throws IOException
    {
        raf.writeChars(s);
    }

    public void writeUTF(String str) throws IOException
    {
        raf.writeUTF(str);
    }

    public InputStream getInputStream() throws IOException
    {
        return rafis;
    }
}
