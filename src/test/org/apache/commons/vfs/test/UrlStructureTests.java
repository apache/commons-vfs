/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
import org.apache.commons.vfs.provider.http.HttpFileSystem;

import java.io.IOException;

/**
 * URL Test cases for providers that supply structural info.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class UrlStructureTests
    extends AbstractProviderTestCase
{
    /**
     * Returns the capabilities required by the tests of this test case.
     */
    protected Capability[] getRequiredCaps()
    {
        return new Capability[]
        {
            Capability.GET_TYPE,
            Capability.URI
        };
    }

    /**
     * Tests that folders have no content.
     */
    public void testFolderURL() throws Exception
    {
        final FileObject folder = getReadFolder().resolveFile("dir1");
        if (folder.getFileSystem() instanceof HttpFileSystem)
        {
            // bad hack, but this test might not fail on HttpFileSystem as there are no direcotries.
            // A Directory do have a content on http. e.g a generated directory listing or the index.html page.
            return;
        }

        assertTrue(folder.exists());

        // Try getting the content of a folder
        try
        {
            folder.getURL().openConnection().getInputStream();
            fail();
        }
        catch (final IOException e)
        {
            assertSameMessage("vfs.provider/read-not-file.error", folder, e);
        }
    }
}
