/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
package org.apache.commons.vfs.provider.http;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractRandomAccessContent;
import org.apache.commons.vfs.util.MonitorInputStream;
import org.apache.commons.vfs.util.RandomAccessMode;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

class HttpRandomAccesContent extends AbstractRandomAccessContent
{
    private final HttpFileObject fileObject;
    private final HttpFileSystem fileSystem;

    protected long filePointer = 0;
    private DataInputStream dis = null;
    private MonitorInputStream mis = null;

    HttpRandomAccesContent(final HttpFileObject fileObject, RandomAccessMode mode)
    {
        super(mode);

        this.fileObject = fileObject;
        fileSystem = (HttpFileSystem) this.fileObject.getFileSystem();
    }

    public long getFilePointer() throws IOException
    {
        return filePointer;
    }

    public void seek(long pos) throws IOException
    {
        if (pos == 0)
        {
            // no change
            return;
        }

        if (pos < 0)
        {
            throw new FileSystemException("vfs.provider/random-access-invalid-position.error",
                new Object[]
                {
                    new Long(pos)
                });
        }
        if (dis != null)
        {
            close();
        }

        filePointer = pos;
    }

    private void createStream() throws IOException
    {
        if (dis != null)
        {
            return;
        }

        final GetMethod getMethod = new GetMethod();
        fileObject.setupMethod(getMethod);
        getMethod.setRequestHeader("Range", "bytes=" + filePointer + "-");
        final int status = fileSystem.getClient().executeMethod(getMethod);
        if (status != HttpURLConnection.HTTP_PARTIAL)
        {
            throw new FileSystemException("vfs.provider.http/get-range.error", new Object[]
            {
                fileObject.getName(),
                new Long(filePointer)
            });
        }

        mis = new HttpFileObject.HttpInputStream(getMethod);
        dis = new DataInputStream(mis);
    }


    public void close() throws IOException
    {
        if (dis != null)
        {
            dis.close();
            dis = null;
            mis = null;
        }
    }

    public long length() throws IOException
    {
        return fileObject.getContent().getSize();
    }

    public byte readByte() throws IOException
    {
        createStream();
        byte data = dis.readByte();
        filePointer += mis.getCount();
        return data;
    }

    public char readChar() throws IOException
    {
        createStream();
        char data = dis.readChar();
        filePointer += mis.getCount();
        return data;
    }

    public double readDouble() throws IOException
    {
        createStream();
        double data = dis.readDouble();
        filePointer += mis.getCount();
        return data;
    }

    public float readFloat() throws IOException
    {
        createStream();
        float data = dis.readFloat();
        filePointer += mis.getCount();
        return data;
    }

    public int readInt() throws IOException
    {
        createStream();
        int data = dis.readInt();
        filePointer += mis.getCount();
        return data;
    }

    public int readUnsignedByte() throws IOException
    {
        createStream();
        int data = dis.readUnsignedByte();
        filePointer += mis.getCount();
        return data;
    }

    public int readUnsignedShort() throws IOException
    {
        createStream();
        int data = dis.readUnsignedShort();
        filePointer += mis.getCount();
        return data;
    }

    public long readLong() throws IOException
    {
        createStream();
        long data = dis.readLong();
        filePointer += mis.getCount();
        return data;
    }

    public short readShort() throws IOException
    {
        createStream();
        short data = dis.readShort();
        filePointer += mis.getCount();
        return data;
    }

    public boolean readBoolean() throws IOException
    {
        createStream();
        boolean data = dis.readBoolean();
        filePointer += mis.getCount();
        return data;
    }

    public int skipBytes(int n) throws IOException
    {
        createStream();
        int data = dis.skipBytes(n);
        filePointer += mis.getCount();
        return data;
    }

    public void readFully(byte b[]) throws IOException
    {
        createStream();
        dis.readFully(b);
        filePointer += mis.getCount();
    }

    public void readFully(byte b[], int off, int len) throws IOException
    {
        createStream();
        dis.readFully(b, off, len);
        filePointer += mis.getCount();
    }

    public String readUTF() throws IOException
    {
        createStream();
        String data = dis.readUTF();
        filePointer += mis.getCount();
        return data;
    }
}