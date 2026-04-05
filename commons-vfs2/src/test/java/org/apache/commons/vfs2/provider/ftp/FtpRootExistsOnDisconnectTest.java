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
package org.apache.commons.vfs2.provider.ftp;

import java.time.Duration;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VfsTestUtils;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link FtpFileObject#exists()} for root-level FTP folders where {@code getParent()} returns {@code null}.
 * <p>
 * Regression test for the bug in {@link FtpFileObject} where {@code setFTPFile()} blindly assumed that root-level
 * directories exist ({@code setType(DIRECTORY_TYPE)}) without verifying on the server. This caused {@code exists()} to
 * return {@code true} even after the FTP connection was lost, while non-root folders correctly reported the connection
 * failure via {@link FileSystemException}.
 * </p>
 */
public class FtpRootExistsOnDisconnectTest {

    @BeforeEach
    public void setUp() throws Exception {
        FtpProviderTest.setUpClass(VfsTestUtils.getTestDirectory(), null, null);
    }

    @AfterEach
    public void tearDown() {
        FtpProviderTest.tearDownClass();
    }

    private static FileSystemOptions createOptions() {
        final FileSystemOptions options = new FileSystemOptions();
        final FtpFileSystemConfigBuilder builder = FtpFileSystemConfigBuilder.getInstance();
        builder.setUserDirIsRoot(options, true);
        builder.setPassiveMode(options, true);
        builder.setConnectTimeout(options, Duration.ofSeconds(10));
        return options;
    }

    /**
     * Tests that {@code exists()} returns {@code true} when the server is running, and does not silently return
     * {@code true} after the FTP connection is lost.
     * <p>
     * With {@code userDirIsRoot=true}, the root's {@code getParent()} returns {@code null}, which triggers the
     * {@code verifyRootDirectory()} code path in {@code setFTPFile()}.
     * </p>
     * <p>
     * Before the fix, {@code setFTPFile()} set {@code type=DIRECTORY} when {@code getParent()} returned {@code null},
     * without contacting the server. After the fix, {@code setFTPFile()} uses CWD to verify, which fails on a dead
     * connection.
     * </p>
     */
    @Test
    public void testRootExistsFailsWhenConnectionDropped() throws Exception {
        try (final DefaultFileSystemManager manager = new DefaultFileSystemManager()) {
            manager.addProvider("ftp", new FtpFileProvider());
            manager.init();
            final FileObject root = manager.resolveFile(FtpProviderTest.getConnectionUri(), createOptions());

            // Verify precondition: with userDirIsRoot=true, getParent() returns null,
            // which is the code path this test exercises.
            Assertions.assertNull(root.getParent(),
                "Root folder's getParent() should return null with userDirIsRoot=true");

            // Verify the root exists initially.
            Assertions.assertTrue(root.exists(), "Root should exist while server is running");

            // Stop the server to simulate a connection drop.
            FtpProviderTest.tearDownClass();

            // exists() must throw FileSystemException on the dead connection.
            // The first call may still succeed if the MINA server processes one last
            // CWD during graceful shutdown, but the second call must fail.
            boolean threwException = false;
            for (int i = 0; i < 2 && !threwException; i++) {
                root.refresh();
                try {
                    root.exists();
                } catch (final FileSystemException expected) {
                    threwException = true;
                }
            }
            Assertions.assertTrue(threwException,
                "exists() must throw FileSystemException after FTP connection is lost");
        }
    }
}
