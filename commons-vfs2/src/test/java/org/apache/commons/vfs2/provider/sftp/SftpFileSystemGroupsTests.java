package org.apache.commons.vfs2.provider.sftp;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SftpFileSystemGroupsTests {

    FileSystemOptions options = new FileSystemOptions();
    Session session;
    SftpFileSystem fileSystem;

    @Before
    public void setup() throws JSchException {
        session = new JSch().getSession("");
        fileSystem = new SftpFileSystem(null, session, options);
    }

    @Test
    public void shouldHandleEmptyGroupResult() {
        StringBuilder builder = new StringBuilder("\n");
        int[] groups = fileSystem.parseGroupIdOutput(builder);

        Assert.assertEquals("Group ids should be empty", 0, groups.length);
    }

    @Test
    public void shouldHandleListOfGroupIds() {
        StringBuilder builder = new StringBuilder("1 22 333 4444\n");
        int[] groups = fileSystem.parseGroupIdOutput(builder);

        Assert.assertEquals("Group ids should not be empty", 4, groups.length);
        Assert.assertArrayEquals(new int[]{1, 22, 333, 4444}, groups);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldThrowOnUnexpectedOutput() {
        StringBuilder builder = new StringBuilder("abc\n");
        fileSystem.parseGroupIdOutput(builder);
    }
}
