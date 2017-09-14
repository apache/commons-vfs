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
package org.apache.commons.vfs2.provider.ftp.test;

import junit.framework.Test;
import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Tests for FTP file systems (with homeDirIsRoot=true).
 */
public class FtpProviderUserDirTestCase extends FtpProviderTestCase {
    /**
     * Creates the test suite for the ftp file system.
     */
    public static Test suite() throws Exception {
        return suite(new FtpProviderUserDirTestCase());
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    protected boolean getUserDirIsRoot() {
        return true;
    }

    /**
     * Gets option file system factory for local FTP server.
     */
    @Override
    protected FileSystemFactory getFtpFileSystem() throws IOException {
        // simulate a non-root home directory by copying test directory to it
        final File testDir = new File(getTestDirectory());
        final File rootDir = new File(testDir, "homeDirIsRoot");
        final File homesDir = new File(rootDir, "home");
        final File initialDir = new File(homesDir, "test");
        FileUtils.deleteDirectory(rootDir);
        // noinspection ResultOfMethodCallIgnored
        rootDir.mkdir();
        FileUtils.copyDirectory(testDir, initialDir, new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return !pathname.getPath().contains(rootDir.getName());
            }
        });

        return new NativeFileSystemFactory() {
            @Override
            public FileSystemView createFileSystemView(final User user) throws FtpException {
                final FileSystemView fsView = super.createFileSystemView(user);
                fsView.changeWorkingDirectory("home/test");
                return fsView;
            }
        };
    }

    /**
     * Gets the root of the local FTP Server file system.
     */
    @Override
    protected String getFtpRootDir() {
        return new File(getTestDirectory(), "homeDirIsRoot").getPath();
    }

}
