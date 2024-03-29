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
package org.apache.commons.vfs2.provider.tftp;

import org.apache.commons.net.tftp.TFTP;
import org.apache.commons.net.tftp.TFTPClient;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.util.RandomAccessMode;

import java.io.*;

/**
 * A file in an TFTP file system.
 */
public class TftpFileObject extends AbstractFileObject<TftpFileSystem> {
    private TFTPClient client;
    private final String host;
    private final int port;

    protected TftpFileObject(final AbstractFileName name, final TftpFileSystem fileSystem) throws IOException {
        super(name, fileSystem);
        this.host = ((TftpFileName) name).getHostName();
        this.port = ((TftpFileName) name).getPort();
    }

    public class TFTPFileOutputStream extends OutputStream {
        private final TFTPClient tftpClient;
        private final String remoteFilePath;
        private final OutputStream outputStream;

        public TFTPFileOutputStream(TFTPClient tftpClient, String remoteFilePath) throws IOException {
            this.tftpClient = tftpClient;
            this.remoteFilePath = remoteFilePath;
            this.outputStream = new ByteArrayOutputStream(); // 创建一个内存输出流，用于暂存数据
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(b); // 将数据写入内存输出流
        }

        @Override
        public void close() throws IOException {
            super.close();
            byte[] data = ((ByteArrayOutputStream) outputStream).toByteArray(); // 将内存输出流中的数据转换为字节数组
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data); // 创建一个基于内存的输入流
            tftpClient.sendFile(remoteFilePath, TFTPClient.BINARY_MODE, byteArrayInputStream, host, port); // 发送数据到TFTP服务器
            outputStream.close(); // 关闭内存输出流
        }
    }

    /**
     * Attaches this file object to its file resource.
     */
    @Override
    protected void doAttach() throws Exception {
        // Defer creation of the TftpClient to here
        if (client == null) {
            client = new TFTPClient();
            client.open();
        }
    }

    @Override
    protected void doDetach() throws Exception {
        // file closed through content-streams
        client = null;
    }

    /**
     * Determines the type of the file, returns null if the file does not exist.
     */
    @Override
    protected FileType doGetType() throws Exception {
        return FileType.FILE;
    }

    /**
     * Lists the children of the file. Is only called if {@link #doGetType} returns {@link FileType#FOLDER}.
     */
    @Override
    protected String[] doListChildren() throws Exception {
        return new String[0];
    }

    /**
     * Determines if this file is hidden.
     */
    @Override
    protected boolean doIsHidden() throws Exception {
        return false;
    }

    /**
     * Deletes the file.
     */
    @Override
    protected void doDelete() throws Exception {
        throw new TftpException("Delete not supported");
    }

    @Override
    protected void doRename(final FileObject newfile) throws Exception {
        throw new TftpException("Rename not supported");
    }

    /**
     * Creates this file as a folder.
     */
    @Override
    protected void doCreateFolder() throws Exception {
        throw new TftpException("CreateFolder not supported");
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    @Override
    protected long doGetContentSize() throws Exception {
        return client.getTotalBytesReceived();
    }

    private static final long DEFAULT_TIMESTAMP = 0L;

    /**
     * Returns the last modified time of this file.
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        return DEFAULT_TIMESTAMP;
    }

    /**
     * Creates an input stream to read the file content from.
     */
    @Override
    protected InputStream doGetInputStream(final int bufferSize) throws Exception {
        if (!getType().hasContent()) {
            throw new FileSystemException("vfs.provider/read-not-file.error", getName());
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        client.receiveFile(getName().getPath().substring(1), TFTP.BINARY_MODE, outputStream, this.host, this.port);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * Creates an output stream to write the file content to.
     */
    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception {
        return new TFTPFileOutputStream(client, getName().getPath().substring(1));
    }

    /**
     * random access
     */
    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
        throw new TftpException("GetRandomAccessContent not supported");
    }

    @Override
    protected boolean doSetLastModifiedTime(final long modtime) throws Exception {
        throw new TftpException("SetLastModifiedTime not supported");
    }

}
