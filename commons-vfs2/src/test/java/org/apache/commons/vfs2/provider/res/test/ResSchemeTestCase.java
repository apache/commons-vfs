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

package org.apache.commons.vfs2.provider.res.test;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

public class ResSchemeTestCase {

    @Test
    public void test_resolveFile_String() throws FileSystemException {
        Assert.assertTrue(VFS.getManager().resolveFile("res:test.properties").exists());
    }

    @Test
    public void test_resolveFile_String_S() throws FileSystemException {
        Assert.assertTrue(VFS.getManager().resolveFile("res:/test.properties").exists());
    }

    @Test
    public void test_resolveFile_String_SS() throws FileSystemException {
        Assert.assertTrue(VFS.getManager().resolveFile("res://test.properties").exists());
    }

    @Test
    public void test_resolveFile_String_SSS() throws FileSystemException {
        Assert.assertTrue(VFS.getManager().resolveFile("res://test.properties").exists());
    }

    @Test
    public void test_resolveFile_URI() throws FileSystemException, URISyntaxException {
        Assert.assertTrue(VFS.getManager().resolveFile(new URI("res:test.properties")).exists());
    }

    @Test
    public void test_resolveFile_URI_S() throws FileSystemException, URISyntaxException {
        Assert.assertTrue(VFS.getManager().resolveFile(new URI("res:/test.properties")).exists());
    }

    @Test
    public void test_resolveFile_URI_SS() throws FileSystemException, URISyntaxException {
        Assert.assertTrue(VFS.getManager().resolveFile(new URI("res://test.properties")).exists());
    }

    @Test
    public void test_resolveFile_URI_SSS() throws FileSystemException, URISyntaxException {
        Assert.assertTrue(VFS.getManager().resolveFile(new URI("res://test.properties")).exists());
    }

    @Test
    public void test_resolveURI_String() throws FileSystemException {
        Assert.assertTrue(VFS.getManager().resolveURI("res:test.properties").isFile());
    }

    @Test
    public void test_resolveURI_String_S() throws FileSystemException {
        Assert.assertTrue(VFS.getManager().resolveURI("res:/test.properties").isFile());
    }

    @Test
    public void test_resolveURI_String_SS() throws FileSystemException {
        Assert.assertTrue(VFS.getManager().resolveURI("res://test.properties").isFile());
    }

    @Test
    public void test_resolveURI_String_SSS() throws FileSystemException {
        Assert.assertTrue(VFS.getManager().resolveURI("res:///test.properties").isFile());
    }

    @Test(expected = FileSystemException.class)
    public void test_resolveURI_String_SSSnull() throws FileSystemException {
        // Resulting path is empty
        Assert.assertTrue(VFS.getManager().resolveURI("res:///").isFile());
    }

    @Test(expected = FileSystemException.class)
    public void test_resolveFile_String_SSSnull() throws FileSystemException {
        // Resulting path is empty
        Assert.assertTrue(VFS.getManager().resolveFile("res:///").exists());
    }
}
