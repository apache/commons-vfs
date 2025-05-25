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

import java.io.InputStream;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.mina.core.session.IoSession;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.session.AbstractSession;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.session.SessionFactory;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Test SftpFileObject.doGetInputStream return the channel to pool when throw an exception.
 */
public class SftpPutChannelTestCase extends AbstractSftpProviderTestCase {

    /**
     * Exposes the channels size.
     */
    private static class CustomServerSession extends ServerSession {
        public CustomServerSession(final FactoryManager server, final IoSession ioSession) throws Exception {
            super(server, ioSession);
        }

        public int getChannelsCount() {
            return channels.size();
        }
    }

    private static class CustomSessionFactory extends SessionFactory {
        @Override
        protected AbstractSession doCreateSession(final IoSession ioSession) throws Exception {
            return new CustomServerSession(server, ioSession);
        }
    }

    private static final Integer MAX_CHANNELS = 10;

    /**
     * Creates the test suite for the sftp file system.
     */
    public static junit.framework.Test suite() throws Exception {
        return new SftpProviderTestSuite(new SftpPutChannelTestCase()) {
            @Override
            protected void addBaseTests() throws Exception {
                // Just tries to read
                addTests(SftpPutChannelTestCase.class);
            }
        };
    }

    /**
     * Gets the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCapabilities() {
        return new Capability[] { Capability.CREATE, Capability.DELETE, Capability.GET_TYPE, Capability.LIST_CHILDREN, Capability.READ_CONTENT,
                Capability.WRITE_CONTENT };
    }

    @Override
    protected boolean isExecChannelClosed() {
        return false;
    }

    @Override
    protected SessionFactory sessionFactory() {
        return new CustomSessionFactory();
    }

    /**
     * Tests SftpFileObject.doGetInputStream return the channel to pool, when there is an exception.
     */
    @Test
    public void testDoGetInputStream() throws Exception {
        final FileObject readFolder = getReadFolder();
        // try MAX_CHANNELS * 2 times, then check channels count less than MAX_CHANNELS
        // ( actually must <= 2, but less than MAX_CHANNELS is enough
        for (int i = 0; i < MAX_CHANNELS * 2; i++) {
            try {
                try (InputStream ignored = readFolder.resolveFile("not-exists.txt").getContent().getInputStream()) {
                    Assertions.fail("file should not be exists");
                }
            } catch (final FileSystemException e) {
                final int channelsCount = ((CustomServerSession) server.getActiveSessions().get(0)).getChannelsCount();
                Assertions.assertTrue(channelsCount < MAX_CHANNELS, "channels count expected less than " + MAX_CHANNELS);
            }
        }
    }

}
