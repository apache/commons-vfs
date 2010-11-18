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
package org.apache.commons.vfs2.provider.ram;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * RAM File Random Access Content.
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public class RamFileRandomAccessContent implements RandomAccessContent
{
    /**
     * File Pointer
     */
    protected int filePointer = 0;

    /**
     * Buffer
     */
    private byte[] buf;

    /**
     * buffer
     */
    private final byte[] buffer8 = new byte[8];

    /**
     * buffer
     */
    private final byte[] buffer4 = new byte[4];

    /**
     * buffer
     */
    private final byte[] buffer2 = new byte[2];

    /**
     * buffer
     */
    private final byte[] buffer1 = new byte[1];

    /**
     * Mode
     */
    private final RandomAccessMode mode;

    /**
     * File
     */
    private final RamFileObject file;

    private final InputStream rafis;

    /**
     * @param file The file to access.
     * @param mode The access mode.
     */
    public RamFileRandomAccessContent(RamFileObject file, RandomAccessMode mode)
    {
        super();
        this.buf = file.getData().getBuffer();
        this.file = file;
        this.mode = mode;

        rafis = new InputStream()
        {
            @Override
            public int read() throws IOException
            {
                try
                {
                    return readByte();
                }
                catch (EOFException e)
                {
                    return -1;
                }
            }

            @Override
            public long skip(long n) throws IOException
            {
                seek(getFilePointer() + n);
                return n;
            }

            @Override
            public void close() throws IOException
            {
            }

            @Override
            public int read(byte[] b) throws IOException
            {
                return read(b, 0, b.length);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException
            {
                int retLen = Math.min(len, getLeftBytes());
                RamFileRandomAccessContent.this.readFully(b, off, retLen);
                return retLen;
            }

            @Override
            public int available() throws IOException
            {
                return getLeftBytes();
            }
        };
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.RandomAccessContent#getFilePointer()
     */
    public long getFilePointer() throws IOException
    {
        return this.filePointer;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.RandomAccessContent#seek(long)
     */
    public void seek(long pos) throws IOException
    {
        this.filePointer = (int) pos;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.RandomAccessContent#length()
     */
    public long length() throws IOException
    {
        return buf.length;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.RandomAccessContent#close()
     */
    public void close() throws IOException
    {

    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readByte()
     */
    public byte readByte() throws IOException
    {
        return (byte) this.readUnsignedByte();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readChar()
     */
    public char readChar() throws IOException
    {
        int ch1 = this.readUnsignedByte();
        int ch2 = this.readUnsignedByte();
        return (char) ((ch1 << 8) + (ch2 << 0));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readDouble()
     */
    public double readDouble() throws IOException
    {
        return Double.longBitsToDouble(this.readLong());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readFloat()
     */
    public float readFloat() throws IOException
    {
        return Float.intBitsToFloat(this.readInt());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readInt()
     */
    public int readInt() throws IOException
    {
        return (readUnsignedByte() << 24) | (readUnsignedByte() << 16)
                | (readUnsignedByte() << 8) | readUnsignedByte();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readUnsignedByte()
     */
    public int readUnsignedByte() throws IOException
    {
        if (filePointer < buf.length)
        {
            return buf[filePointer++] & 0xFF;
        }
        else
        {
            throw new EOFException();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readUnsignedShort()
     */
    public int readUnsignedShort() throws IOException
    {
        this.readFully(buffer2);
        return toUnsignedShort(buffer2);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readLong()
     */
    public long readLong() throws IOException
    {
        this.readFully(buffer8);
        return toLong(buffer8);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readShort()
     */
    public short readShort() throws IOException
    {
        this.readFully(buffer2);
        return toShort(buffer2);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readBoolean()
     */
    public boolean readBoolean() throws IOException
    {
        return (this.readUnsignedByte() != 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#skipBytes(int)
     */
    public int skipBytes(int n) throws IOException
    {
        if (n < 0)
        {
            throw new IndexOutOfBoundsException(
                    "The skip number can't be negative");
        }

        long newPos = filePointer + n;

        if (newPos > buf.length)
        {
            throw new IndexOutOfBoundsException("Tyring to skip too much bytes");
        }

        seek(newPos);

        return n;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readFully(byte[])
     */
    public void readFully(byte[] b) throws IOException
    {
        this.readFully(b, 0, b.length);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readFully(byte[], int, int)
     */
    public void readFully(byte[] b, int off, int len) throws IOException
    {
        if (len < 0)
        {
            throw new IndexOutOfBoundsException("Length is lower than 0");
        }

        if (len > this.getLeftBytes())
        {
            throw new IndexOutOfBoundsException("Read length (" + len
                    + ") is higher than buffer left bytes ("
                    + this.getLeftBytes() + ") ");
        }

        System.arraycopy(buf, filePointer, b, off, len);

        filePointer += len;
    }

    private int getLeftBytes()
    {
        return buf.length - filePointer;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readUTF()
     */
    public String readUTF() throws IOException
    {
        return DataInputStream.readUTF(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#write(byte[], int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException
    {
        if (this.getLeftBytes() < len)
        {
            int newSize = this.buf.length + len - this.getLeftBytes();
            this.file.resize(newSize);
            this.buf = this.file.getData().getBuffer();
        }
        System.arraycopy(b, off, this.buf, filePointer, len);
        this.filePointer += len;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#write(byte[])
     */
    public void write(byte[] b) throws IOException
    {
        this.write(b, 0, b.length);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#writeByte(int)
     */
    public void writeByte(int i) throws IOException
    {
        this.write(i);
    }

    /**
     * Build a long from first 8 bytes of the array.
     *
     * @param b The byte[] to convert.
     * @return A long.
     */
    public static long toLong(byte[] b)
    {
        return ((((long) b[7]) & 0xFF) + ((((long) b[6]) & 0xFF) << 8)
                + ((((long) b[5]) & 0xFF) << 16)
                + ((((long) b[4]) & 0xFF) << 24)
                + ((((long) b[3]) & 0xFF) << 32)
                + ((((long) b[2]) & 0xFF) << 40)
                + ((((long) b[1]) & 0xFF) << 48) + ((((long) b[0]) & 0xFF) << 56));
    }

    /**
     * Build a 8-byte array from a long. No check is performed on the array
     * length.
     *
     * @param n The number to convert.
     * @param b The array to fill.
     * @return A byte[].
     */
    public static byte[] toBytes(long n, byte[] b)
    {
        b[7] = (byte) (n);
        n >>>= 8;
        b[6] = (byte) (n);
        n >>>= 8;
        b[5] = (byte) (n);
        n >>>= 8;
        b[4] = (byte) (n);
        n >>>= 8;
        b[3] = (byte) (n);
        n >>>= 8;
        b[2] = (byte) (n);
        n >>>= 8;
        b[1] = (byte) (n);
        n >>>= 8;
        b[0] = (byte) (n);
        return b;
    }

    /**
     * Build a short from first 2 bytes of the array.
     * @param b The byte[] to convert.
     * @return A short.
     */
    public static short toShort(byte[] b)
    {
        return (short) toUnsignedShort(b);
    }

    /**
     * Build a short from first 2 bytes of the array.
     *
     * @param b The byte[] to convert.
     * @return A short.
     */
    public static int toUnsignedShort(byte[] b)
    {
        return ((b[1] & 0xFF) + ((b[0] & 0xFF) << 8));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#write(int)
     */
    public void write(int b) throws IOException
    {
        buffer1[0] = (byte) b;
        this.write(buffer1);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#writeBoolean(boolean)
     */
    public void writeBoolean(boolean v) throws IOException
    {
        this.write(v ? 1 : 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#writeBytes(java.lang.String)
     */
    public void writeBytes(String s) throws IOException
    {
        write(s.getBytes());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#writeChar(int)
     */
    public void writeChar(int v) throws IOException
    {
        buffer2[0] = (byte) ((v >>> 8) & 0xFF);
        buffer2[1] = (byte) ((v >>> 0) & 0xFF);
        write(buffer2);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#writeChars(java.lang.String)
     */
    public void writeChars(String s) throws IOException
    {
        int len = s.length();
        for (int i = 0; i < len; i++)
        {
            writeChar(s.charAt(i));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#writeDouble(double)
     */
    public void writeDouble(double v) throws IOException
    {
        writeLong(Double.doubleToLongBits(v));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#writeFloat(float)
     */
    public void writeFloat(float v) throws IOException
    {
        writeInt(Float.floatToIntBits(v));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#writeInt(int)
     */
    public void writeInt(int v) throws IOException
    {
        buffer4[0] = (byte) ((v >>> 24) & 0xFF);
        buffer4[1] = (byte) ((v >>> 16) & 0xFF);
        buffer4[2] = (byte) ((v >>> 8) & 0xFF);
        buffer4[3] = (byte) (v & 0xFF);
        write(buffer4);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#writeLong(long)
     */
    public void writeLong(long v) throws IOException
    {
        write(toBytes(v, buffer8));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#writeShort(int)
     */
    public void writeShort(int v) throws IOException
    {
        buffer2[0] = (byte) ((v >>> 8) & 0xFF);
        buffer2[1] = (byte) (v & 0xFF);
        write(buffer2);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataOutput#writeUTF(java.lang.String)
     */
    public void writeUTF(String str) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream(str.length());
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeUTF(str);
        dataOut.flush();
        dataOut.close();
        byte[] b = out.toByteArray();
        write(b);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.DataInput#readLine()
     */
    public String readLine() throws IOException
    {
        throw new UnsupportedOperationException("deprecated");
    }

    public InputStream getInputStream() throws IOException
    {
        return rafis;
    }
}
