/*
 * Copyright 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.test;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;

/**
 * Test cases for getting and setting file last modified time.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2004/02/28 03:35:53 $
 */
public class LastModifiedTests
    extends AbstractProviderTestCase
{
    /**
     * Returns the capabilities required by the tests of this test case.
     */
    protected Capability[] getRequiredCaps()
    {
        return new Capability[] {
            Capability.GET_LAST_MODIFIED
        };
    }

    /**
     * Tests getting the last modified time of a file.
     */
    public void testGetLastModified() throws Exception {
        // Try a file.
        final FileObject file = getReadFolder().resolveFile( "file1.txt" );
        file.getContent().getLastModifiedTime();

        // TODO - switch this on
        // Try a folder
        //final FileObject folder = getReadFolder().resolveFile( "dir1" );
        //folder.getContent().getLastModifiedTime();
    }

    /**
     * Tests setting the last modified time of file.
     */
    public void testSetLastModified() throws Exception {
        if ( !getReadFolder().getFileSystem().hasCapability( Capability.SET_LAST_MODIFIED ) )
        {
            // Can't set last modified
            return;
        }
        final long now = System.currentTimeMillis();

        // Try a file
        final FileObject file = getReadFolder().resolveFile( "file1.txt" );
        file.getContent().setLastModifiedTime( now);
        assertEquals( now, file.getContent().getLastModifiedTime() );

        // Try a folder
        final FileObject folder = getReadFolder().resolveFile( "dir1" );
        folder.getContent().setLastModifiedTime( now );
        assertEquals( now, folder.getContent().getLastModifiedTime() );
    }
}
