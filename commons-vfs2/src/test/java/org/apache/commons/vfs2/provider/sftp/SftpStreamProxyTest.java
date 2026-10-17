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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.jcraft.jsch.TestIdentityRepositoryFactory;

/**
 * Tests the command that {@link SftpStreamProxy} asks the proxy host to run.
 * <p>
 * The embedded server plays the proxy host. It records each command it receives and ends it without running anything, so no connection ever reaches a
 * target host.
 * </p>
 */
public class SftpStreamProxyTest {

    /**
     * A command that exits without forwarding anything.
     */
    private static final class ExitingCommand implements Command {

        private ExitCallback callback;

        private InputStream in;

        @Override
        public void destroy(final ChannelSession channel) {
            // nothing to release
        }

        @Override
        public void setErrorStream(final OutputStream err) {
            // not used
        }

        @Override
        public void setExitCallback(final ExitCallback callback) {
            this.callback = callback;
        }

        @Override
        public void setInputStream(final InputStream in) {
            this.in = in;
        }

        @Override
        public void setOutputStream(final OutputStream out) {
            // not used
        }

        @Override
        public void start(final ChannelSession channel, final Environment env) {
            // JSch only reads from the proxy after it has sent its identification string; exiting before that would leave it waiting.
            final Thread thread = new Thread(() -> {
                try {
                    in.read();
                } catch (final IOException e) {
                    // exit anyway
                } finally {
                    callback.onExit(1);
                }
            }, "proxy-command");
            thread.setDaemon(true);
            thread.start();
        }
    }

    private static final String TEST_USERNAME = "testuser";

    private static final String TEST_PASSWORD = "testpass";

    private static final List<String> COMMANDS = new CopyOnWriteArrayList<>();

    private static SshServer proxyServer;

    private static DefaultFileSystemManager manager;

    private static FileSystemOptions options() throws FileSystemException {
        final SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
        final FileSystemOptions proxyOptions = new FileSystemOptions();
        builder.setStrictHostKeyChecking(proxyOptions, "no");
        builder.setUserInfo(proxyOptions, new TrustEveryoneUserInfo());
        builder.setIdentityRepositoryFactory(proxyOptions, new TestIdentityRepositoryFactory());
        final FileSystemOptions options = (FileSystemOptions) proxyOptions.clone();
        builder.setProxyType(options, SftpFileSystemConfigBuilder.PROXY_STREAM);
        builder.setProxyHost(options, "localhost");
        builder.setProxyPort(options, proxyServer.getPort());
        builder.setProxyUser(options, TEST_USERNAME);
        builder.setProxyPassword(options, TEST_PASSWORD);
        builder.setProxyCommand(options, SftpStreamProxy.NETCAT_COMMAND);
        builder.setProxyOptions(options, proxyOptions);
        return options;
    }

    @BeforeAll
    static void setUp() throws Exception {
        proxyServer = SshServer.setUpDefaultServer();
        proxyServer.setPort(0);
        final Path tmpKeyFile = Files.createTempFile("sshd-test-key", ".ser");
        tmpKeyFile.toFile().deleteOnExit();
        final SimpleGeneratorHostKeyProvider keyProvider = new SimpleGeneratorHostKeyProvider(tmpKeyFile);
        keyProvider.setAlgorithm("RSA");
        proxyServer.setKeyPairProvider(keyProvider);
        proxyServer.setPasswordAuthenticator((user, pass, session) -> TEST_USERNAME.equals(user) && TEST_PASSWORD.equals(pass));
        proxyServer.setCommandFactory((channel, command) -> {
            COMMANDS.add(command);
            return new ExitingCommand();
        });
        proxyServer.start();
        manager = new DefaultFileSystemManager();
        manager.addProvider("sftp", new SftpFileProvider());
        manager.init();
    }

    @AfterAll
    static void tearDown() {
        if (manager != null) {
            manager.close();
        }
        IOUtils.closeQuietly(proxyServer);
    }

    @BeforeEach
    void clearCommands() {
        COMMANDS.clear();
    }

    @ParameterizedTest
    @ValueSource(strings = { "target.example.com", "10.0.0.1", "target_host-1", "[fe80::1c42:dae:8370:aea6%en1]" })
    void testHostNameIsPassedToCommand(final String host) {
        assertThrows(FileSystemException.class, () -> manager.resolveFile("sftp://user:pass@" + host + ":2222/", options()));
        assertEquals(Collections.singletonList(String.format(SftpStreamProxy.NETCAT_COMMAND, host, 2222)), COMMANDS);
    }

    @ParameterizedTest
    @ValueSource(strings = { "target|id", "target`id`", "target id", "target\nid", "target(id)", "target>out", "target<in", "target'id'", "target\"id\"",
            "target{id}", "target*", "target!id", "-v" })
    void testShellSyntaxInHostNameIsRejected(final String host) {
        assertThrows(FileSystemException.class, () -> manager.resolveFile("sftp://user:pass@" + host + ":2222/", options()));
        assertEquals(Collections.emptyList(), COMMANDS);
    }
}
