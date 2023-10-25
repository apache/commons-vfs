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
package org.apache.commons.vfs2;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class IPv6LocalConnectionTests extends AbstractProviderTestCase {

    private static final Log log = LogFactory.getLog(IPv6LocalConnectionTests.class);

    @Override
    protected void runTest() throws Throwable {
        final List<String> localIPv6Addresses = getLocalIPv6Addresses();

        if (localIPv6Addresses.isEmpty()) {
            log.info("Local machine must have IPv6 address to run this test");
            return;
        }

        super.runTest();
    }

    @Override
    protected Capability[] getRequiredCapabilities() {
        return new Capability[] {Capability.URI, Capability.READ_CONTENT};
    }

    @Test
    public void testConnectIPv6UrlLocal() throws Exception {
        final List<String> localIPv6Addresses = getLocalIPv6Addresses();

        boolean connected = false;

        for (String ipv6Address: localIPv6Addresses) {
            final String ipv6Url = StringUtils.replace(
                    this.getReadFolder().getURL().toString(), "localhost", "[" + ipv6Address + "]");

            try {
                final FileSystem fileSystem = getFileSystem();

                final FileObject readFolderObject = getManager()
                        .resolveFile(ipv6Url, setupConnectionTimeoutHints(fileSystem));

                connected = connected || readFolderObject.resolveFile("file1.txt").getContent().getByteArray() != null;
            } catch (FileSystemException e) {
                // We don't care, if some of the discovered IPv6 addresses don't work.
                // We just need a single one to work for testing the functionality end-to-end.
                log.warn("Failed to connect to some of the local IPv6 network addresses", e);
            }
        }

        assertTrue("None of the discovered local IPv6 network addresses has responded for connection", connected);
    }

    private static List<String> getLocalIPv6Addresses() throws SocketException {
        final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        final List<String> result = new ArrayList<>();

        for (NetworkInterface networkInterface : Collections.list(networkInterfaces)) {
            if (!networkInterface.isUp() || networkInterface.isLoopback()
                    // utun refers to VPN network interface, we don't expect this connection to work
                    || networkInterface.getName().startsWith("utun")) {

                continue;
            }

            for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                if (inetAddress instanceof Inet6Address && !inetAddress.isLoopbackAddress() && !inetAddress.isMulticastAddress()) {
                    result.add(inetAddress.getHostAddress());
                }
            }
        }

        return result;
    }

    private FileSystemOptions setupConnectionTimeoutHints(FileSystem fileSystem) {
        // Unfortunately there is no common way to set up timeouts for every protocol
        // So, we use this hacky approach to make this class generic and formally independent of protocols implementations

        FileSystemOptions result = (FileSystemOptions) fileSystem.getFileSystemOptions().clone();

        Duration timeout = Duration.ofSeconds(5);

        result.setOption(fileSystem.getClass(),
                "org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder.CONNECT_TIMEOUT", timeout);
        result.setOption(fileSystem.getClass(),
                "org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder.TIMEOUT", timeout);

        result.setOption(fileSystem.getClass(), "http.connection.timeout", timeout);
        result.setOption(fileSystem.getClass(), "http.socket.timeout", timeout);

        // This actually doesn't affect FtpFileProvider now, but it looks like an issue
        // This would work, if FtpClientFactory call client.setConnectTimeout() with CONNECT_TIMEOUT value
        result.setOption(fileSystem.getClass(),
                "org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder.CONNECT_TIMEOUT", timeout);
        result.setOption(fileSystem.getClass(),
                "org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder.SO_TIMEOUT", timeout);

        return result;
    }

}
