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
package org.apache.commons.vfs.provider.local.test;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.test.AbstractProviderTestCase;

import java.io.File;

/**
 * Additional naming tests for local file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class FileNameTests
    extends AbstractProviderTestCase
{
    /**
     * Tests resolution of an absolute file name.
     */
    public void testAbsoluteFileName() throws Exception
    {
        // Locate file by absolute file name
        String fileName = new File("testdir").getAbsolutePath();
        FileObject absFile = getManager().resolveFile(fileName);

        // Locate file by URI
        String uri = "file://" + fileName.replace(File.separatorChar, '/');
        FileObject uriFile = getManager().resolveFile(uri);

        assertSame("file object", absFile, uriFile);
    }
}
