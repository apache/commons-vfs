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

import org.apache.commons.vfs2.RandomAccessContent;

import java.io.IOException;
import java.io.InputStream;

/**
 * An RandomAccessContent that provides end-of-stream monitoring.
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class MonitorRandomAccessContent implements RandomAccessContent
{
    private final RandomAccessContent content;
    private boolean finished;

    public MonitorRandomAccessContent(final RandomAccessContent content)
    {
        this.content = content;
    }

    /**
     * Called after this stream is closed.  This implementation does nothing.
     */
    @SuppressWarnings("unused") // IOException is needed because subclasses may need to throw it
    protected void onClose() throws IOException
    {
    }

    /**
     * Closes this content.
     * @throws IOException if an error occurs.
     */
    public void close() throws IOException
    {
        if (finished)
        {
            return;
        }

        // Close the output stream
        IOException exc = null;
        try
        {
            content.close();
        }
        catch (final IOException ioe)
        {
            exc = ioe;
        }

        // Notify of end of output
        exc = null;
        try
        {
            onClose();
        }
        catch (final IOException ioe)
        {
            exc = ioe;
        }

        finished = true;

        if (exc != null)
        {
            throw exc;
        }
    }

    public long getFilePointer() throws IOException
    {
        return content.getFilePointer();
    }

    public void seek(long pos) throws IOException
    {
        content.seek(pos);
    }

    public long length() throws IOException
    {
        return content.length();
    }

    public void write(int b) throws IOException
    {
        content.write(b);
    }

    public void write(byte[] b) throws IOException
    {
        content.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        content.write(b, off, len);
    }

    public void writeBoolean(boolean v) throws IOException
    {
        content.writeBoolean(v);
    }

    public void writeByte(int v) throws IOException
    {
        content.writeByte(v);
    }

    public void writeShort(int v) throws IOException
    {
        content.writeShort(v);
    }

    public void writeChar(int v) throws IOException
    {
        content.writeChar(v);
    }

    public void writeInt(int v) throws IOException
    {
        content.writeInt(v);
    }

    public void writeLong(long v) throws IOException
    {
        content.writeLong(v);
    }

    public void writeFloat(float v) throws IOException
    {
        content.writeFloat(v);
    }

    public void writeDouble(double v) throws IOException
    {
        content.writeDouble(v);
    }

    public void writeBytes(String s) throws IOException
    {
        content.writeBytes(s);
    }

    public void writeChars(String s) throws IOException
    {
        content.writeChars(s);
    }

    public void writeUTF(String str) throws IOException
    {
        content.writeUTF(str);
    }

    public void readFully(byte[] b) throws IOException
    {
        content.readFully(b);
    }

    public void readFully(byte[] b, int off, int len) throws IOException
    {
        content.readFully(b, off, len);
    }

    public int skipBytes(int n) throws IOException
    {
        return content.skipBytes(n);
    }

    public boolean readBoolean() throws IOException
    {
        return content.readBoolean();
    }

    public byte readByte() throws IOException
    {
        return content.readByte();
    }

    public int readUnsignedByte() throws IOException
    {
        return content.readUnsignedByte();
    }

    public short readShort() throws IOException
    {
        return content.readShort();
    }

    public int readUnsignedShort() throws IOException
    {
        return content.readUnsignedShort();
    }

    public char readChar() throws IOException
    {
        return content.readChar();
    }

    public int readInt() throws IOException
    {
        return content.readInt();
    }

    public long readLong() throws IOException
    {
        return content.readLong();
    }

    public float readFloat() throws IOException
    {
        return content.readFloat();
    }

    public double readDouble() throws IOException
    {
        return content.readDouble();
    }

    public String readLine() throws IOException
    {
        return content.readLine();
    }

    public String readUTF() throws IOException
    {
        return content.readUTF();
    }

    public InputStream getInputStream() throws IOException
    {
        return content.getInputStream();
    }
}
