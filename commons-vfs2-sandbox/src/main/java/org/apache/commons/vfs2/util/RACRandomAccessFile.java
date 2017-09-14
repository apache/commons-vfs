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
package org.apache.commons.vfs2.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import org.apache.commons.vfs2.RandomAccessContent;

/**
 * (Sandbox) Encapsulates a {@link RandomAccessContent} instance, allowing it to be used as a {@link RandomAccessFile}
 * instance.
 */
public class RACRandomAccessFile extends RandomAccessFile implements RandomAccessContent {
    private final byte[] singleByteBuf = new byte[1];

    private RandomAccessContent rac;

    public RACRandomAccessFile(final RandomAccessContent rac) throws IOException {
        this(createTempFile());
        this.rac = rac;
    }

    private RACRandomAccessFile(final File tempFile) throws IOException {
        super(tempFile, "r");
        deleteTempFile(tempFile);
    }

    private static File createTempFile() throws IOException {
        return File.createTempFile("fraf", "");
    }

    private void deleteTempFile(final File tempFile) {
        try {
            super.close();
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            /* ignored = */ tempFile.delete();
        }
    }

    @Override
    public long getFilePointer() throws IOException {
        return this.rac.getFilePointer();
    }

    @Override
    public void seek(final long pos) throws IOException {
        this.rac.seek(pos);
    }

    @Override
    public int skipBytes(final int n) throws IOException {
        return this.rac.skipBytes(n);
    }

    @Override
    public long length() throws IOException {
        return this.rac.length();
    }

    @Override
    public void setLength(final long newLength) throws IOException {
        throw new IOException("Underlying RandomAccessContent instance length cannot be modified.");
    }

    public InputStream getInputStream() throws IOException {
        return this.rac.getInputStream();
    }

    @Override
    public void close() throws IOException {
        this.rac.close();
    }

    @Override
    public final int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public final int read() throws IOException {
        final byte[] buf = this.singleByteBuf;
        final int count = read(buf, 0, 1);
        return count < 0 ? -1 : buf[0] & 0xFF;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        this.rac.readFully(b, off, len);
        return len;
    }

    @Override
    public final void write(final int b) throws IOException {
        final byte[] buf = this.singleByteBuf;
        buf[0] = (byte) b;
        write(buf, 0, 1);
    }

    @Override
    public final void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        this.rac.write(b, off, len);
    }

}
