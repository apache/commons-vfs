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
package org.apache.commons.vfs2.provider.http4;

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractRandomAccessStreamContent;
import org.apache.commons.vfs2.util.MonitorInputStream;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

/**
 * RandomAccess content using {@code Http4FileObject}.
 */
final class Http4RandomAccessContent<FS extends Http4FileSystem> extends AbstractRandomAccessStreamContent {

    protected long filePointer;

    private final Http4FileObject<FS> fileObject;

    private DataInputStream dataInputStream;
    private MonitorInputStream monitorInputStream;

    Http4RandomAccessContent(final Http4FileObject<FS> fileObject, final RandomAccessMode mode) {
        super(mode);
        this.fileObject = fileObject;
    }

    @Override
    public void close() throws IOException {
        if (dataInputStream != null) {
            dataInputStream.close();
            dataInputStream = null;
            monitorInputStream = null;
        }
    }

    @Override
    protected DataInputStream getDataInputStream() throws IOException {
        if (dataInputStream != null) {
            return dataInputStream;
        }

        final HttpGet httpGet = new HttpGet(fileObject.getInternalURI());
        httpGet.setHeader("Range", "bytes=" + filePointer + "-");
        final HttpResponse httpResponse = fileObject.executeHttpUriRequest(httpGet);
        final int status = httpResponse.getStatusLine().getStatusCode();

        if (status != HttpURLConnection.HTTP_PARTIAL && status != HttpURLConnection.HTTP_OK) {
            throw new FileSystemException("vfs.provider.http/get-range.error", fileObject.getName(),
                    Long.valueOf(filePointer), Integer.valueOf(status));
        }

        monitorInputStream = new MonitoredHttpResponseContentInputStream(httpResponse);

        // If the range request was ignored
        if (status == HttpURLConnection.HTTP_OK) {
            final long skipped = monitorInputStream.skip(filePointer);
            if (skipped != filePointer) {
                throw new FileSystemException("vfs.provider.http/get-range.error", fileObject.getName(),
                        Long.valueOf(filePointer), Integer.valueOf(status));
            }
        }

        dataInputStream = new DataInputStream(new FilterInputStream(monitorInputStream) {
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

        return dataInputStream;
    }

    @Override
    public long getFilePointer() throws IOException {
        return filePointer;
    }

    @Override
    public long length() throws IOException {
        return fileObject.getContent().getSize();
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

        if (dataInputStream != null) {
            close();
        }

        filePointer = pos;
    }
}
