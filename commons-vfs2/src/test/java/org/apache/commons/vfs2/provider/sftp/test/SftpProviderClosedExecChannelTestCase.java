package org.apache.commons.vfs2.provider.sftp.test;

import org.apache.commons.vfs2.test.PermissionsTests;

import junit.framework.Test;

public class SftpProviderClosedExecChannelTestCase extends AbstractSftpProviderTestCase {
    @Override
    protected boolean isExecChannelClosed() {
        return true;
    }

    /**
     * Creates the test suite for the sftp file system.
     */
    public static Test suite() throws Exception {
        final SftpProviderTestSuite suite = new SftpProviderTestSuite(new SftpProviderClosedExecChannelTestCase());
        // VFS-405: set/get permissions
        suite.addTests(PermissionsTests.class);
        return suite;
    }
}
