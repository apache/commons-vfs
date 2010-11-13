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

import junit.framework.AssertionFailedError;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;

/**
 * Test cases for getting and setting file last modified time.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class LastModifiedTests
    extends AbstractProviderTestCase
{
    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCaps()
    {
        return new Capability[]{
            Capability.GET_LAST_MODIFIED
        };
    }

    /**
     * Tests getting the last modified time of a file.
     */
    public void testGetLastModified() throws Exception
    {
        // Try a file.
        final FileObject file = getReadFolder().resolveFile("file1.txt");
        file.getContent().getLastModifiedTime();

        // TODO - switch this on
        // Try a folder
        //final FileObject folder = getReadFolder().resolveFile( "dir1" );
        //folder.getContent().getLastModifiedTime();
    }

    /**
     * Tests setting the last modified time of file.
     */
    public void testSetLastModified() throws Exception
    {
        final long now = System.currentTimeMillis();

        if (getReadFolder().getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FILE))
        {
            // Try a file
            final FileObject file = getReadFolder().resolveFile("file1.txt");
            file.getContent().setLastModifiedTime(now);
            try
            {
                assertEquals(now, file.getContent().getLastModifiedTime(), file.getFileSystem().getLastModTimeAccuracy());
            }
            catch (AssertionFailedError e)
            {
                // on linux ext3 the above check is not necessarily true
                if (file.getFileSystem().getLastModTimeAccuracy() < 1000L)
                {
                    assertEquals(now, file.getContent().getLastModifiedTime(), 1000L);
                }
                else
                {
                    throw e;
                }
            }
        }

        if (getReadFolder().getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FOLDER))
        {
            // Try a folder
            final FileObject folder = getReadFolder().resolveFile("dir1");
            folder.getContent().setLastModifiedTime(now);
            try
            {
                assertEquals(now, folder.getContent().getLastModifiedTime(), folder.getFileSystem().getLastModTimeAccuracy());
            }
            catch (AssertionFailedError e)
            {
                // on linux ext3 the above check is not necessarily true
                if (folder.getFileSystem().getLastModTimeAccuracy() < 1000L)
                {
                    assertEquals(now, folder.getContent().getLastModifiedTime(), 1000L);
                }
                else
                {
                    throw e;
                }
            }
        }
    }
}
