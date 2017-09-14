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
package org.apache.commons.vfs2.provider.http;

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractRandomAccessStreamContent;
import org.apache.commons.vfs2.util.MonitorInputStream;
import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * RandomAccess content using HTTP.
 */
class HttpRandomAccessContent extends AbstractRandomAccessStreamContent {
    protected long filePointer = 0;

    private final HttpFileObject fileObject;
    private final HttpFileSystem fileSystem;

    private DataInputStream dis = null;
    private MonitorInputStream mis = null;

    HttpRandomAccessContent(final HttpFileObject fileObject, final RandomAccessMode mode) {
        super(mode);

        this.fileObject = fileObject;
        fileSystem = (HttpFileSystem) this.fileObject.getFileSystem();
    }

    @Override
    public long getFilePointer() throws IOException {
        return filePointer;
    }

    @Override
    public void seek(final long pos) throws IOException {
        if (pos == filePointer) {
            // no change
            return;
        }

        if (pos < 0) {
            throw new FileSystemException("vfs.provider/random-access-invalid-position.error", Long.valueOf(pos));
        }
        if (dis != null) {
            close();
        }

        filePointer = pos;
    }

    @Override
    protected DataInputStream getDataInputStream() throws IOException {
        if (dis != null) {
            return dis;
        }

        final GetMethod getMethod = new GetMethod();
        fileObject.setupMethod(getMethod);
        getMethod.setRequestHeader("Range", "bytes=" + filePointer + "-");
        final int status = fileSystem.getClient().executeMethod(getMethod);
        if (status != HttpURLConnection.HTTP_PARTIAL && status != HttpURLConnection.HTTP_OK) {
            throw new FileSystemException("vfs.provider.http/get-range.error", fileObject.getName(),
                    Long.valueOf(filePointer), Integer.valueOf(status));
        }

        mis = new HttpFileObject.HttpInputStream(getMethod);
        // If the range request was ignored
        if (status == HttpURLConnection.HTTP_OK) {
            final long skipped = mis.skip(filePointer);
            if (skipped != filePointer) {
                throw new FileSystemException("vfs.provider.http/get-range.error", fileObject.getName(),
                        Long.valueOf(filePointer), Integer.valueOf(status));
            }
        }
        dis = new DataInputStream(new FilterInputStream(mis) {
            @Override
            public int read() throws IOException {
                final int ret = super.read();
                if (ret > -1) {
                    filePointer++;
                }
                return ret;
            }

            @Override
            public int read(final byte[] b) throws IOException {
                final int ret = super.read(b);
                if (ret > -1) {
                    filePointer += ret;
                }
                return ret;
            }

            @Override
            public int read(final byte[] b, final int off, final int len) throws IOException {
                final int ret = super.read(b, off, len);
                if (ret > -1) {
                    filePointer += ret;
                }
                return ret;
            }
        });

        return dis;
    }

    @Override
    public void close() throws IOException {
        if (dis != null) {
            dis.close();
            dis = null;
            mis = null;
        }
    }

    @Override
    public long length() throws IOException {
        return fileObject.getContent().getSize();
    }
}
