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

package org.apache.commons.vfs2.provider.sftp;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectoryFile;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.FileSystemFactory;
import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.SshFile;
import org.apache.sshd.server.filesystem.NativeSshFile;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.jcraft.jsch.TestIdentityRepositoryFactory;

/**
 * Tests SFTP password authentication using {@link StaticUserAuthenticator}.
 * <p>
 * Verifies that credentials supplied via {@link DefaultFileSystemConfigBuilder#setUserAuthenticator}
 * are correctly propagated to the SFTP server.
 * </p>
 * <p>
 * Uses SSHD 0.8.0 with an explicit RSA {@link KeyPairProvider} for Java 17 compatibility
 * (the default DSA key generation is disabled on modern JDKs).
 * </p>
 */
public class SftpPasswordAuthTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "testpass";

    private static SshServer sshServer;
    private static int serverPort;
    private static DefaultFileSystemManager manager;

    @BeforeAll
    static void setUp() throws Exception {
        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(0);

        final KeyPairGenerator hostKeyGen = KeyPairGenerator.getInstance("RSA");
        hostKeyGen.initialize(2048);
        final KeyPair hostKey = hostKeyGen.generateKeyPair();
        sshServer.setKeyPairProvider(new KeyPairProvider() {
            @Override
            public KeyPair loadKey(final String type) {
                return KeyPairProvider.SSH_RSA.equals(type) ? hostKey : null;
            }
            @Override
            public String getKeyTypes() {
                return KeyPairProvider.SSH_RSA;
            }
        });

        sshServer.setPasswordAuthenticator(
                (user, pass, session) -> TEST_USERNAME.equals(user) && TEST_PASSWORD.equals(pass));

        final List<NamedFactory<Command>> subsystems = new ArrayList<>();
        subsystems.add(new NamedFactory<Command>() {
            @Override
            public Command create() { return new SftpSubsystem(); }
            @Override
            public String getName() { return "sftp"; }
        });
        sshServer.setSubsystemFactories(subsystems);

        sshServer.setFileSystemFactory(new TestFileSystemFactory());
        sshServer.start();

        serverPort = sshServer.getPort();

        manager = new DefaultFileSystemManager();
        manager.addProvider("sftp", new SftpFileProvider());
        manager.addProvider("file", new DefaultLocalFileProvider());
        manager.init();
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (manager != null) {
            try {
                manager.close();
            } catch (final Exception e) {
                // ignore
            }
        }
        if (sshServer != null) {
            stopServerWithTimeout(5000);
        }
    }

    private static void stopServerWithTimeout(final long timeoutMs) {
        final Thread stopThread = new Thread(() -> {
            try {
                sshServer.stop(true);
            } catch (final Exception e) {
                // ignore
            }
        }, "sshd-stop");
        stopThread.setDaemon(true);
        stopThread.start();
        try {
            stopThread.join(timeoutMs);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String baseUri() {
        return String.format("sftp://%s@localhost:%d", TEST_USERNAME, serverPort);
    }

    private FileSystemOptions authOptions() throws FileSystemException {
        final FileSystemOptions opts = new FileSystemOptions();
        final StaticUserAuthenticator auth = new StaticUserAuthenticator(null, TEST_USERNAME, TEST_PASSWORD);
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
        configureSftpOptions(opts);
        return opts;
    }

    private static void configureSftpOptions(final FileSystemOptions opts) throws FileSystemException {
        final SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
        builder.setStrictHostKeyChecking(opts, "no");
        builder.setUserInfo(opts, new TrustEveryoneUserInfo());
        builder.setIdentityRepositoryFactory(opts, new TestIdentityRepositoryFactory());
        builder.setConnectTimeout(opts, Duration.ofSeconds(60));
        builder.setSessionTimeout(opts, Duration.ofSeconds(60));
    }

    @Test
    void testResolveFile() throws FileSystemException {
        final FileSystemOptions opts = authOptions();
        try (FileObject file = manager.resolveFile(baseUri() + "/read-tests/file1.txt", opts)) {
            assertNotNull(file);
            assertTrue(file.exists(), "file1.txt should exist");
            assertEquals(FileType.FILE, file.getType());
            assertNotNull(file.getContent(), "Content should be readable when credentials are correct");
        }
    }

    @Test
    void testResolveFolder() throws FileSystemException {
        final FileSystemOptions opts = authOptions();
        try (FileObject folder = manager.resolveFile(baseUri() + "/read-tests", opts)) {
            assertNotNull(folder);
            assertTrue(folder.exists(), "read-tests folder should exist");
        }
    }

    @Test
    void testResolveFolderWithTrailingSlash() throws FileSystemException {
        final FileSystemOptions opts = authOptions();
        try (FileObject folder = manager.resolveFile(baseUri() + "/read-tests/", opts)) {
            assertNotNull(folder);
            assertTrue(folder.exists(), "read-tests/ folder should exist");
        }
    }

    @Test
    void testWrongCredentialsThrowsException() throws FileSystemException {
        final FileSystemOptions opts = new FileSystemOptions();
        final StaticUserAuthenticator auth = new StaticUserAuthenticator(null, "wronguser", "wrongpassword");
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
        configureSftpOptions(opts);

        final String wrongUserUri = String.format("sftp://wronguser@localhost:%d/read-tests/file1.txt", serverPort);
        assertThrows(FileSystemException.class, () -> {
            try (FileObject file = manager.resolveFile(wrongUserUri, opts)) {
                file.exists();
            }
        }, "Expected FileSystemException when accessing a resource with wrong credentials");
    }
    
    private static class TestFileSystemFactory implements FileSystemFactory {
        @Override
        public FileSystemView createFileSystemView(final Session session) {
            final String home = getTestDirectoryFile().getAbsolutePath();
            final String user = session.getUsername();
            return new FileSystemView() {
                @Override
                public SshFile getFile(final SshFile baseDir, final String file) {
                    return getFile(baseDir.getAbsolutePath(), file);
                }
                @Override
                public SshFile getFile(final String file) {
                    return getFile(home, file);
                }
                private SshFile getFile(final String dir, final String file) {
                    final String normalized = NativeSshFile.normalizeSeparateChar(file);
                    final String homeNorm = NativeSshFile.normalizeSeparateChar(home);
                    final String prefix = removePrefix(homeNorm);
                    String userFile = removePrefix(normalized);
                    final File f = userFile.startsWith(prefix)
                            ? new File(userFile)
                            : new File(prefix, userFile);
                    userFile = removePrefix(NativeSshFile.normalizeSeparateChar(f.getAbsolutePath()));
                    return new AccessibleNativeSshFile(userFile, f, user);
                }
                private String removePrefix(final String s) {
                    final int idx = s.indexOf('/');
                    return idx < 1 ? s : s.substring(idx);
                }
            };
        }
    }

    private static class AccessibleNativeSshFile extends NativeSshFile {
        AccessibleNativeSshFile(final String fileName, final File file, final String userName) {
            super(fileName, file, userName);
        }
    }
}
