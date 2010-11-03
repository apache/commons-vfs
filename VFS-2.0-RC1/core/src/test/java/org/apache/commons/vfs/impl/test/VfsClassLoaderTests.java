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
package org.apache.commons.vfs.impl.test;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.impl.VFSClassLoader;
import org.apache.commons.vfs.test.AbstractProviderTestCase;

import java.net.URL;
import java.net.URLConnection;

/**
 * VfsClassLoader test cases.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class VfsClassLoaderTests
    extends AbstractProviderTestCase
{
    /**
     * Returns the capabilities required by the tests of this test case.
     */
    protected Capability[] getRequiredCaps()
    {
        return new Capability[]
        {
            Capability.READ_CONTENT,
            Capability.URI
        };
    }

    /**
     * Creates the classloader to use when testing.
     */
    private VFSClassLoader createClassLoader() throws FileSystemException
    {
        FileObject file = getBaseFolder();
        final VFSClassLoader loader =
            new VFSClassLoader(file , getManager());
        return loader;
    }

    /**
     * Tests loading a class.
     */
    public void testLoadClass() throws Exception
    {
        final VFSClassLoader loader = createClassLoader();

        final Class testClass = loader.loadClass("code.ClassToLoad");
        final Package pack = testClass.getPackage();
        assertEquals("code", pack.getName());
        verifyPackage(pack, false);

        final Object testObject = testClass.newInstance();
        assertEquals("**PRIVATE**", testObject.toString());
    }

    /**
     * Tests loading a resource.
     */
    public void testLoadResource() throws Exception
    {
        final VFSClassLoader loader = createClassLoader();

        final URL resource = loader.getResource("read-tests/file1.txt");

        assertNotNull(resource);
        final URLConnection urlCon = resource.openConnection();
        assertSameURLContent(FILE1_CONTENT, urlCon);
    }

    /**
     * Tests package sealing.
     */
    public void testSealing() throws Exception
    {
        final VFSClassLoader loader = createClassLoader();
        final Class testClass = loader.loadClass("code.sealed.AnotherClass");
        final Package pack = testClass.getPackage();
        assertEquals("code.sealed", pack.getName());
        verifyPackage(pack, true);
    }

    /**
     * Verify the package loaded with class loader.
     */
    private void verifyPackage(final Package pack,
                               final boolean sealed)
        throws FileSystemException
    {
        if (getBaseFolder().getFileSystem().hasCapability(Capability.MANIFEST_ATTRIBUTES))
        {
            assertEquals("ImplTitle", pack.getImplementationTitle());
            assertEquals("ImplVendor", pack.getImplementationVendor());
            assertEquals("1.1", pack.getImplementationVersion());
            assertEquals("SpecTitle", pack.getSpecificationTitle());
            assertEquals("SpecVendor", pack.getSpecificationVendor());
            assertEquals("1.0", pack.getSpecificationVersion());
            assertEquals(sealed, pack.isSealed());
        }
        else
        {
            assertNull(pack.getImplementationTitle());
            assertNull(pack.getImplementationVendor());
            assertNull(pack.getImplementationVersion());
            assertNull(pack.getSpecificationTitle());
            assertNull(pack.getSpecificationVendor());
            assertNull(pack.getSpecificationVersion());
            assertFalse(pack.isSealed());
        }
    }

}
