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
package org.apache.commons.vfs2.test;

import static org.junit.Assert.assertNotEquals;

import java.util.Date;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Assert;

/**
 * Test cases for getting and setting file last modified time.
 */
public class LastModifiedTests extends AbstractProviderTestCase {
    private void assertDelta(final String message, final long expected, final long actual, final long delta) {
        if (expected == actual) {
            return;
        }
        if (Math.abs(expected - actual) > Math.max(delta, 1000)) // getLastModTimeAccuracy() is not accurate
        {
            Assert.fail(String.format("%s expected=%d (%s), actual=%d (%s), delta=%d", message, Long.valueOf(expected),
                    new Date(expected).toString(), Long.valueOf(actual), new Date(actual).toString(),
                    Long.valueOf(delta)));
        }
    }

    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCaps() {
        return new Capability[] { Capability.GET_LAST_MODIFIED };
    }

    /**
     * Tests FileSystem#getLastModTimeAccuracy for sane values.
     *
     * @throws FileSystemException if error occurred
     */
    public void testGetAccurary() throws FileSystemException {
        final FileObject file = getReadFolder().resolveFile("file1.txt");
        final long lastModTimeAccuracy = (long) file.getFileSystem().getLastModTimeAccuracy();
        // System.out.println("Accuracy on " + file.getFileSystem().getRootURI() + " is " + lastModTimeAccuracy + " as
        // told by " + file.getFileSystem().getClass().getCanonicalName());
        assertTrue("Accuracy must be positive", lastModTimeAccuracy >= 0);
        assertTrue("Accuracy must be < 2m", lastModTimeAccuracy < 2 * 60 * 1000); // just any sane limit
    }

    /**
     * Tests getting the last modified time of a folder.
     *
     * @throws FileSystemException if error occurred
     */
    public void testGetLastModifiedFolder() throws FileSystemException {
        final FileObject file = getReadFolder().resolveFile("dir1");
        assertNotEquals(0L, file.getContent().getLastModifiedTime());
    }

    /**
     * Tests getting the last modified time of a file.
     *
     * @throws FileSystemException if error occurred
     */
    public void testGetLastModifiedFile() throws FileSystemException {
        final FileObject file = getReadFolder().resolveFile("file1.txt");
        assertNotEquals(0L, file.getContent().getLastModifiedTime());
    }

    /**
     * Tests setting the last modified time of a folder.
     *
     * @throws FileSystemException if error occurred
     */
    public void testSetLastModifiedFolder() throws FileSystemException {
        final long yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000;

        if (getReadFolder().getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FOLDER)) {
            // Try a folder
            final FileObject folder = getReadFolder().resolveFile("dir1");
            folder.getContent().setLastModifiedTime(yesterday);
            final long lastModTimeAccuracy = (long) folder.getFileSystem().getLastModTimeAccuracy();
            // folder.refresh(); TODO: does not work with SSH VFS-563
            final long lastModifiedTime = folder.getContent().getLastModifiedTime();
            assertDelta("set/getLastModified on Folder", yesterday, lastModifiedTime, lastModTimeAccuracy);
        }
    }

    /**
     * Tests setting the last modified time of file.
     *
     * @throws FileSystemException if error occurred
     */
    public void testSetLastModifiedFile() throws FileSystemException {
        final long yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000;

        if (getReadFolder().getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FILE)) {
            // Try a file
            final FileObject file = getReadFolder().resolveFile("file1.txt");
            file.getContent().setLastModifiedTime(yesterday);
            final long lastModTimeAccuracy = (long) file.getFileSystem().getLastModTimeAccuracy();
            // folder.refresh(); TODO: does not work with SSH VFS-563
            final long lastModifiedTime = file.getContent().getLastModifiedTime();
            assertDelta("set/getLastModified on File", yesterday, lastModifiedTime, lastModTimeAccuracy);
        }
    }
}
