/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider.hdfs;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.provider.AbstractRandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * Provides random access to content in an HdfsFileObject. Currently this only supports read operations. All write
 * operations throw an {@link UnsupportedOperationException}.
 *
 * @since 2.1
 */
public class HdfsRandomAccessContent extends AbstractRandomAccessContent {

    private final FSDataInputStream fis;
    private final FileSystem fs;
    private final Path path;

    /**
     * Create random access content.
     *
     * @param path A Hadoop Path
     * @param fs A Hadoop FileSystem
     * @throws IOException when the path cannot be processed.
     */
    public HdfsRandomAccessContent(final Path path, final FileSystem fs) throws IOException {
        super(RandomAccessMode.READ);
        this.fs = fs;
        this.path = path;
        fis = this.fs.open(this.path);
    }

    /**
     * @see org.apache.commons.vfs2.RandomAccessContent#close()
     */
    @Override
    public void close() throws IOException {
        fis.close();
    }

    /**
     * @see org.apache.commons.vfs2.RandomAccessContent#getFilePointer()
     */
    @Override
    public long getFilePointer() throws IOException {
        return fis.getPos();
    }

    /**
     * @see org.apache.commons.vfs2.RandomAccessContent#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return fis;
    }

    /**
     * @see org.apache.commons.vfs2.RandomAccessContent#length()
     */
    @Override
    public long length() throws IOException {
        return fs.getFileStatus(path).getLen();
    }

    /**
     * @see java.io.DataInput#readBoolean()
     */
    @Override
    public boolean readBoolean() throws IOException {
        return fis.readBoolean();
    }

    /**
     * @see java.io.DataInput#readByte()
     */
    @Override
    public byte readByte() throws IOException {
        return fis.readByte();
    }

    /**
     * @see java.io.DataInput#readChar()
     */
    @Override
    public char readChar() throws IOException {
        return fis.readChar();
    }

    /**
     * @see java.io.DataInput#readDouble()
     */
    @Override
    public double readDouble() throws IOException {
        return fis.readDouble();
    }

    /**
     * @see java.io.DataInput#readFloat()
     */
    @Override
    public float readFloat() throws IOException {
        return fis.readFloat();
    }

    /**
     * @see java.io.DataInput#readFully(byte[])
     */
    @Override
    public void readFully(final byte[] b) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.io.DataInput#readFully(byte[], int, int)
     */
    @Override
    public void readFully(final byte[] b, final int off, final int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.io.DataInput#readInt()
     */
    @Override
    public int readInt() throws IOException {
        return fis.readInt();
    }

    /**
     * @see java.io.DataInput#readLine()
     */
    @Override
    @SuppressWarnings("deprecation")
    public String readLine() throws IOException {
        return fis.readLine();
    }

    /**
     * @see java.io.DataInput#readLong()
     */
    @Override
    public long readLong() throws IOException {
        return fis.readLong();
    }

    /**
     * @see java.io.DataInput#readShort()
     */
    @Override
    public short readShort() throws IOException {
        return fis.readShort();
    }

    /**
     * @see java.io.DataInput#readUnsignedByte()
     */
    @Override
    public int readUnsignedByte() throws IOException {
        return fis.readUnsignedByte();
    }

    /**
     * @see java.io.DataInput#readUnsignedShort()
     */
    @Override
    public int readUnsignedShort() throws IOException {
        return fis.readUnsignedShort();
    }

    /**
     * @see java.io.DataInput#readUTF()
     */
    @Override
    public String readUTF() throws IOException {
        return fis.readUTF();
    }

    /**
     * @see org.apache.commons.vfs2.RandomAccessContent#seek(long)
     */
    @Override
    public void seek(final long pos) throws IOException {
        fis.seek(pos);
    }

    /**
     * @see org.apache.commons.vfs2.RandomAccessContent#setLength(long)
     */
    @Override
    public void setLength(final long newLength) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.io.DataInput#skipBytes(int)
     */
    @Override
    public int skipBytes(final int n) throws IOException {
        throw new UnsupportedOperationException();
    }

}
