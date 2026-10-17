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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VfsTestUtils;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that a file name holding a carriage return or line feed cannot smuggle a second command onto the FTP control
 * connection.
 * <p>
 * {@code %0D%0A} in a name decodes to a real CRLF in the path that {@link FtpFileObject} sends with every command, and
 * RFC 959 ends a command at the first CRLF, so {@code folder%0D%0ADELE%20secret.txt} used to make the server run
 * {@code MKD folder} followed by the caller's own {@code DELE secret.txt}.
 * </p>
 */
public class FtpCommandInjectionTest {

    private static FileSystemOptions createOptions() {
        final FileSystemOptions options = new FileSystemOptions();
        final FtpFileSystemConfigBuilder builder = FtpFileSystemConfigBuilder.getInstance();
        builder.setUserDirIsRoot(options, true);
        builder.setPassiveMode(options, true);
        builder.setConnectTimeout(options, Duration.ofSeconds(10));
        return options;
    }

    @BeforeEach
    public void setUp() throws Exception {
        FtpProviderTest.setUpClass(VfsTestUtils.getTestDirectory(), null, null);
    }

    @AfterEach
    public void tearDown() {
        FtpProviderTest.tearDownClass();
    }

    @Test
    public void testLineBreakInNameIsRejected() throws Exception {
        try (final DefaultFileSystemManager manager = new DefaultFileSystemManager()) {
            manager.addProvider("ftp", new FtpFileProvider());
            manager.init();
            final FileObject root = manager.resolveFile(FtpProviderTest.getConnectionUri(), createOptions());
            assertThrows(FileSystemException.class, () -> root.resolveFile("folder%0D%0ADELE%20secret.txt"));
            assertThrows(FileSystemException.class, () -> root.resolveFile("folder%0Aquiet.txt"));
            assertThrows(FileSystemException.class, () -> root.resolveFile("folder\r\nDELE secret.txt"));
            // A name that holds neither still resolves.
            assertEquals("/folder/file.txt", root.resolveFile("folder/file.txt").getName().getPath());
        }
    }
}
