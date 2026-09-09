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
package org.apache.commons.vfs2.provider.http4;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.net.InetAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Http4FileProvider#createSSLContext(Http4FileSystemConfigBuilder, FileSystemOptions)} against a server
 * holding the self-signed {@code CN=localhost} certificate of the FTPS test server, which chains to no certificate
 * authority.
 */
public class Http4SslContextTest {

    private static final String SERVER_JKS_RES = "org.apache.ftpsserver/ftpserver.jks";

    private static final char[] PASSWORD = "password".toCharArray();

    private SSLContext createServerSslContext() throws Exception {
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(SERVER_JKS_RES)) {
            keyStore.load(inputStream, PASSWORD);
        }
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, PASSWORD);
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        return sslContext;
    }

    private void handshake(final FileSystemOptions options, final boolean expectTrusted) throws Exception {
        try (SSLServerSocket serverSocket = (SSLServerSocket) createServerSslContext().getServerSocketFactory()
                .createServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            final Thread serverThread = new Thread(() -> {
                try (SSLSocket socket = (SSLSocket) serverSocket.accept()) {
                    socket.startHandshake();
                } catch (final Exception expected) {
                    // the client aborts the handshake when it does not trust the certificate
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();
            final SSLContext sslContext = new Http4FileProvider().createSSLContext(Http4FileSystemConfigBuilder.getInstance(), options);
            try (SSLSocket socket = (SSLSocket) sslContext.getSocketFactory().createSocket(serverSocket.getInetAddress(),
                    serverSocket.getLocalPort())) {
                if (expectTrusted) {
                    socket.startHandshake();
                } else {
                    assertThrows(SSLException.class, socket::startHandshake);
                }
            }
        }
    }

    /**
     * Tests VFS-786 keystore type: the configured keystore provides the trust material.
     */
    @Test
    public void testConfiguredKeyStoreIsTrusted() throws Exception {
        final Http4FileSystemConfigBuilder builder = Http4FileSystemConfigBuilder.getInstance();
        final FileSystemOptions options = new FileSystemOptions();
        builder.setKeyStoreFile(options, ClassLoader.getSystemResource(SERVER_JKS_RES).getFile());
        builder.setKeyStorePass(options, new String(PASSWORD));
        builder.setKeyStoreType(options, "JKS");
        handshake(options, true);
    }

    @Test
    public void testDefaultKeyStoreRejectsUntrustedCertificate() throws Exception {
        handshake(new FileSystemOptions(), false);
    }
}
