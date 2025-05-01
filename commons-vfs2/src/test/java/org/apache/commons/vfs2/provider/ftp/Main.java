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

package org.apache.commons.vfs2.provider.ftp;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.io.Util;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;

public class Main {

    public static void main(final String[] args) {
        final String host = "localhost";
        final String remoteDir = "/unicode";
        try {
            // System.setProperty("file.encoding", "UTF-8");
            final FileSystemManager manager = VFS.getManager();
            final FileSystemOptions opts = new FileSystemOptions();
            final FtpFileSystemConfigBuilder builder = FtpFileSystemConfigBuilder.getInstance();
            builder.setAutodetectUtf8(opts, true);
            builder.setControlEncoding(opts, StandardCharsets.UTF_8);
            builder.setUserDirIsRoot(opts, false);
            builder.setPassiveMode(opts, true);
            builder.setFileType(opts, FtpFileType.BINARY);
            final String ftpUrl = "ftp://" + host + remoteDir;
            try (FileObject remoteFolder = manager.resolveFile(ftpUrl, opts)) {
                final FtpFileObject ftpFileObject = (FtpFileObject) remoteFolder;
                final FtpFileSystem ftpFileSystem = (FtpFileSystem) ftpFileObject.getFileSystem();
                final FTPClientWrapper clientW = (FTPClientWrapper) ftpFileSystem.getClient();
                clientW.getFtpClient().addProtocolCommandListener(new PrintCommandListener(Util.newPrintWriter(System.out), true));
                clientW.getFtpClient().syst();
                clientW.getFtpClient().setFileType(0);
                clientW.sendOptions("UTF8", "ON");
                System.out.printf("%s - Files in %s:%n", Instant.now(), remoteDir);
                final FileObject[] files = remoteFolder.getChildren();
                for (final FileObject file : files) {
                    final String fileName = file.getName().getBaseName();
                    System.out.println(fileName);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
