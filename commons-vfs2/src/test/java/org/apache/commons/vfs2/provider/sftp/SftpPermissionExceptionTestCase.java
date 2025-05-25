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

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectory;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.apache.sshd.server.channel.ChannelSession;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Test SftpFileObject.doGetOutputStream return the channel to pool, even throw a sftp write permission exception.
 */
public class SftpPermissionExceptionTestCase extends AbstractSftpProviderTestCase {

    /**
     * Creates the test suite for the sftp file system.
     */
    public static junit.framework.Test suite() throws Exception {
        return new SftpProviderTestSuite(new SftpPermissionExceptionTestCase()){
            @Override
            protected void addBaseTests() throws Exception {
                // Just tries to read
                addTests(SftpPermissionExceptionTestCase.class);
            }
        };
    }

    /**
     * Sets up a scratch folder for the test to use.
     */
    protected FileObject createScratchFolder() throws Exception {
        final FileObject scratchFolder = getWriteFolder();

        // Make sure the test folder is empty
        scratchFolder.delete(Selectors.EXCLUDE_SELF);
        scratchFolder.createFolder();
        scratchFolder.setWritable(false, false);
        return scratchFolder;
    }

    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCapabilities() {
        return new Capability[] {Capability.CREATE, Capability.DELETE, Capability.GET_TYPE, Capability.LIST_CHILDREN,
            Capability.READ_CONTENT, Capability.WRITE_CONTENT};
    }

    @Override
    protected boolean isExecChannelClosed() {
        return false;
    }

    /**
     * Test SftpFileObject.doGetOutputStream return the channel to pool, when there is an exception in channel.put.
     */
    @Test
    public void testGetOutputStreamException() throws Exception {
        final File localFile = new File("src/test/resources/test-data/test.zip");

        final FileObject localFileObject = VFS.getManager().toFileObject(localFile);

        final FileObject scratchFolder = createScratchFolder();

        // try to create local file
        final String fileName = "filecopy.txt";
        FileObject fileObjectCopy = scratchFolder.resolveFile(fileName);
        fileObjectCopy.setWritable(false, false);
        fileObjectCopy.copyFrom(localFileObject, Selectors.SELECT_SELF);

        // try to set the local file to readonly
        Paths.get(getTestDirectory(), scratchFolder.getName().getBaseName(), fileName).toFile().setWritable(false);
        for (int i = 0; i < 30; i++) {
            try{
                fileObjectCopy = scratchFolder.resolveFile(fileName);
                Assertions.assertFalse(fileObjectCopy.isWriteable());
                fileObjectCopy.copyFrom(localFileObject, Selectors.SELECT_SELF);
                Assertions.fail("permission fail");
            } catch (final Exception ex) {
                // ignore no permission
            }
        }

        // try to get created channel number.
        final int channelId = server.getActiveSessions().get(0).registerChannel(new ChannelSession());
        Assertions.assertTrue(channelId < 30, "create too many sftp channel more");

        // try to set the local file to writable
        Paths.get(getTestDirectory(), scratchFolder.getName().getBaseName(), fileName).toFile().setWritable(true);

        fileObjectCopy = scratchFolder.resolveFile(fileName);
        Assertions.assertTrue(fileObjectCopy.isWriteable());
        fileObjectCopy.copyFrom(localFileObject, Selectors.SELECT_SELF);
    }

}
