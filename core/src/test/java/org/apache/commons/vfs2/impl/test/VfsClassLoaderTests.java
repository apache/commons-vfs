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
package org.apache.commons.vfs2.impl.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.VFSClassLoader;
import org.apache.commons.vfs2.test.AbstractProviderTestCase;

/**
 * VfsClassLoader test cases.
 */
public class VfsClassLoaderTests
    extends AbstractProviderTestCase
{
    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
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
        final FileObject file = getBaseFolder();
        return new VFSClassLoader(file, getManager());
    }

    /**
     * Tests loading a class.
     */
    public void testLoadClass() throws Exception
    {
        final VFSClassLoader loader = createClassLoader();

        final Class<?> testClass = loader.loadClass("code.ClassToLoad");
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
        final Class<?> testClass = loader.loadClass("code.sealed.AnotherClass");
        final Package pack = testClass.getPackage();
        assertEquals("code.sealed", pack.getName());
        verifyPackage(pack, true);
    }

    /**
     * Tests retrieving resources (from JAR searchpath)
     */
    public void testGetResourcesJARs() throws Exception
    {
        final FileSystemManager manager = getManager();
        final File baseDir = AbstractVfsTestCase.getTestDirectory();

        // make sure the provider config is useable
        if (baseDir == null || manager == null || !baseDir.isDirectory()) 
        {
            return;
        }

        // build search path without using #getBaseFolder()
        // because NestedJarTestCase redefines it
        final FileObject nestedJar;
        final FileObject testJar;
        try
        {
            nestedJar = manager.resolveFile(baseDir, "nested.jar");
            testJar = manager.resolveFile(baseDir, "test.jar");
        }
        catch (FileSystemException ignored)
        {
            return; // this suite cannot handle localFiles
        }

        final FileObject[] search = new FileObject[] { nestedJar, testJar };

        // test setup needs to know about .jar extension - i.e. NestedJarTestCase
        if (!manager.canCreateFileSystem(nestedJar)) 
        {
            return;
        }

        // verify test setup
        assertTrue("nested.jar is required for testing", nestedJar.getType() == FileType.FILE);
        assertTrue("test.jar is required for testing", testJar.getType() == FileType.FILE);

        // System class loader (null) might be unpredictable in regards
        // to returning resources for META-INF/MANIFEST.MF (see VFS-500)
        // so we use our own which is guaranteed to not return any hit
        final ClassLoader mockClassloader = new MockClassloader();

        final VFSClassLoader loader = new VFSClassLoader(search, getManager(), mockClassloader);

        final Enumeration<URL> urls = loader.getResources("META-INF/MANIFEST.MF");
        final URL url1 = urls.nextElement();
        final URL url2 = urls.nextElement();

        assertTrue("First resource must refer to nested.jar but was " + url1, url1.toString().endsWith("nested.jar!/META-INF/MANIFEST.MF"));
        assertTrue("Second resource must refer to test.jar but was " + url2, url2.toString().endsWith("test.jar!/META-INF/MANIFEST.MF"));
    }


    /**
     * Verify the package loaded with class loader.
     */
    private void verifyPackage(final Package pack,
                               final boolean sealed)
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


    /**
     * Non-Delegating Class Loader.
     */
    public static class MockClassloader extends ClassLoader
    {
        MockClassloader()
        {
            super(null);
        }

        /**
         * This method will not return any hit to
         * VFSClassLoader#testGetResourcesJARs.
         */
        @Override
        public Enumeration<URL> getResources(String name)
            throws IOException
        {
            return Collections.enumeration(Collections.<URL> emptyList());
        }

        @Override
        protected Class<?> findClass(String name)
            throws ClassNotFoundException
        {
            fail("Not intended to be used for class loading.");
            return null;
        }
    }
}
