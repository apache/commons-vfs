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

import static org.junit.Assert.assertNotEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for getting and setting file last modified time.
 */
public class LastModifiedTests extends AbstractProviderTestCase {

    protected static final Duration ONE_DAY = Duration.ofDays(1);

    protected void assertDeltaMillis(final String message, final long expected, final long actual, final long delta) {
        if (expected == actual) {
            return;
        }
        // getLastModTimeAccuracy() is not accurate
        final long actualDelta = Math.abs(expected - actual);
        if (actualDelta > Math.max(delta, 1000)) {
            Assert.fail(String.format("%s expected=%,d (%s), actual=%,d (%s), expected delta=%,d, actual delta=%,d",
                message, Long.valueOf(expected), new Date(expected).toString(), Long.valueOf(actual),
                new Date(actual).toString(), Long.valueOf(delta), Long.valueOf(actualDelta)));
        }
    }

    protected void assertEqualMillis(final String message, final long expected, final long actual) {
        if (expected != actual) {
            final long delta = Math.abs(expected - actual);
            Assert
                .fail(String.format("%s expected=%,d (%s), actual=%,d (%s), delta=%,d", message, Long.valueOf(expected),
                    new Date(expected).toString(), Long.valueOf(actual), new Date(actual).toString(), delta));
        }
    }

    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCapabilities() {
        return new Capability[] {Capability.GET_LAST_MODIFIED};
    }

    /**
     * Tests FileSystem#getLastModTimeAccuracy for sane values.
     *
     * @throws FileSystemException if error occurred
     */
    @Test
    public void testGetAccurary() throws FileSystemException {
        final FileObject file = getReadFolder().resolveFile("file1.txt");
        final long lastModTimeAccuracyMillis = (long) file.getFileSystem().getLastModTimeAccuracy();
        // System.out.println("Accuracy on " + file.getFileSystem().getRootURI() + " is " + lastModTimeAccuracy + " as
        // told by " + file.getFileSystem().getClass().getCanonicalName());
        assertTrue("Accuracy must be positive", lastModTimeAccuracyMillis >= 0);
        // just any sane limit
        assertTrue("Accuracy must be < 2m", lastModTimeAccuracyMillis < Duration.ofMinutes(2).toMillis());
    }

    /**
     * Tests getting the last modified time of a file.
     *
     * @throws FileSystemException if error occurred
     */
    @Test
    public void testGetLastModifiedFile() throws FileSystemException {
        final FileObject file = getReadFolder().resolveFile("file1.txt");
        assertNotEquals(0L, file.getContent().getLastModifiedTime());
    }

    /**
     * Tests getting the last modified time of a folder.
     *
     * @throws FileSystemException if error occurred
     */
    @Test
    public void testGetLastModifiedFolder() throws FileSystemException {
        final FileObject file = getReadFolder().resolveFile("dir1");
        assertNotEquals(0L, file.getContent().getLastModifiedTime());
    }

    /**
     * Tests setting the last modified time of file.
     *
     * @throws FileSystemException if error occurred
     */
    @Test
    public void testSetLastModifiedFile() throws FileSystemException {
        final long yesterdayMillis = Instant.now().minus(ONE_DAY).toEpochMilli();

        if (getReadFolder().getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FILE)) {
            // Try a file
            final FileObject file = getReadFolder().resolveFile("file1.txt");
            file.getContent().setLastModifiedTime(yesterdayMillis);
            final long lastModTimeAccuracyMillis = (long) file.getFileSystem().getLastModTimeAccuracy();
            // folder.refresh(); TODO: does not work with SSH VFS-563
            final long lastModifiedTime = file.getContent().getLastModifiedTime();
            assertDeltaMillis("set/getLastModified on File", yesterdayMillis, lastModifiedTime,
                lastModTimeAccuracyMillis);
        }
    }

    /**
     * Tests setting the last modified time of a folder.
     *
     * @throws FileSystemException if error occurred
     */
    @Test
    public void testSetLastModifiedFolder() throws FileSystemException {
        final long yesterdayMillis = Instant.now().minus(ONE_DAY).toEpochMilli();

        if (getReadFolder().getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FOLDER)) {
            // Try a folder
            final FileObject folder = getReadFolder().resolveFile("dir1");
            folder.getContent().setLastModifiedTime(yesterdayMillis);
            final long lastModTimeAccuracyMillis = (long) folder.getFileSystem().getLastModTimeAccuracy();
            // folder.refresh(); TODO: does not work with SSH VFS-563
            final long lastModifiedTime = folder.getContent().getLastModifiedTime();
            assertDeltaMillis("set/getLastModified on Folder", yesterdayMillis, lastModifiedTime,
                lastModTimeAccuracyMillis);
        }
    }
}
