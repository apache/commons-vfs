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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SftpFileSystemGroupsTests {

    private final FileSystemOptions options = new FileSystemOptions();
    private Session session;
    private SftpFileSystem fileSystem;

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
