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
package org.apache.commons.vfs2.provider.sftp;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link SftpFileSystem}.
 */
public class SftpFileSystemGroupsTests {

    private SftpFileSystem fileSystem;
    private final FileSystemOptions options = new FileSystemOptions();
    private Session session;

    @Before
    public void setup() throws JSchException {
        session = new JSch().getSession("");
        fileSystem = new SftpFileSystem(null, session, options);
    }

    @Test
    public void shouldHandleEmptyGroupResult() {
        final StringBuilder builder = new StringBuilder("\n");
        final int[] groups = fileSystem.parseGroupIdOutput(builder);

        Assert.assertEquals("Group ids should be empty", 0, groups.length);
    }

    @Test
    public void shouldHandleListOfGroupIds() {
        final StringBuilder builder = new StringBuilder("1 22 333 4444\n");
        final int[] groups = fileSystem.parseGroupIdOutput(builder);

        Assert.assertEquals("Group ids should not be empty", 4, groups.length);
        Assert.assertArrayEquals(new int[]{1, 22, 333, 4444}, groups);
    }

    @Test
    public void shouldThrowOnUnexpectedOutput() {
        final StringBuilder builder = new StringBuilder("abc\n");
        assertThrows(NumberFormatException.class, () -> fileSystem.parseGroupIdOutput(builder));
    }

}
