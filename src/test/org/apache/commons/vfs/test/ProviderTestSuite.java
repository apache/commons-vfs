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

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.cache.SoftRefFilesCache;
import org.apache.commons.vfs.impl.DefaultFileReplicator;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.PrivilegedFileReplicator;
import org.apache.commons.vfs.impl.test.VfsClassLoaderTests;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;

/**
 * The suite of tests for a file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @author Gary D. Gregory
 * @version $Id: ProviderTestSuite.java,v 1.18 2004/05/08 19:48:30 imario Exp $
 */
public class ProviderTestSuite
    extends TestSetup
{
    private final ProviderTestConfig providerConfig;
    private final String prefix;
    private final TestSuite testSuite;

    private FileObject baseFolder;
    private FileObject readFolder;
    private FileObject writeFolder;
    private DefaultFileSystemManager manager;
    private File tempDir;

    /**
     * Adds the tests for a file system to this suite.
     */
    public ProviderTestSuite(final ProviderTestConfig providerConfig) throws Exception
    {
        this(providerConfig, "", false);
    }

    private ProviderTestSuite(final ProviderTestConfig providerConfig,
                              final String prefix,
                              final boolean nested)
        throws Exception
    {
        super(new TestSuite());
        testSuite = (TestSuite) fTest;
        this.providerConfig = providerConfig;
        this.prefix = prefix;
        addBaseTests();
        if (!nested)
        {
            // Add nested tests
            // TODO - move nested jar and zip tests here
            // TODO - enable this again
            //testSuite.addTest( new ProviderTestSuite( new JunctionProviderConfig( providerConfig ), "junction.", true ));
        }
    }

    /**
     * Adds base tests - excludes the nested test cases.
     */
    private void addBaseTests() throws Exception
    {
        addTests(UriTests.class);
        addTests(NamingTests.class);
        addTests(ContentTests.class);
        addTests(ProviderReadTests.class);
        addTests(ProviderWriteTests.class);
        addTests(ProviderWriteAppendTests.class);
        addTests(ProviderRenameTests.class);
        addTests(LastModifiedTests.class);
        addTests(UrlTests.class);
        addTests(UrlStructureTests.class);
        addTests(VfsClassLoaderTests.class);
    }

    /**
     * Adds the tests from a class to this suite.  The supplied class must be
     * a subclass of {@link AbstractProviderTestCase} and have a public a
     * no-args constructor.  This method creates an instance of the supplied
     * class for each public 'testNnnn' method provided by the class.
     */
    public void addTests(final Class testClass) throws Exception
    {
        // Verify the class
        if (!AbstractProviderTestCase.class.isAssignableFrom(testClass))
        {
            throw new Exception("Test class " + testClass.getName() + " is not assignable to " + AbstractProviderTestCase.class.getName());
        }

        // Locate the test methods
        final Method[] methods = testClass.getMethods();
        for (int i = 0; i < methods.length; i++)
        {
            final Method method = methods[i];
            if (!method.getName().startsWith("test")
                || Modifier.isStatic(method.getModifiers())
                || method.getReturnType() != Void.TYPE
                || method.getParameterTypes().length != 0)
            {
                continue;
            }

            // Create instance
            final AbstractProviderTestCase testCase = (AbstractProviderTestCase) testClass.newInstance();
            testCase.setMethod(method);
            testCase.setName(prefix + method.getName());
            testSuite.addTest(testCase);
        }
    }

    protected void setUp() throws Exception
    {
        // Locate the temp directory, and clean it up
        tempDir = AbstractVfsTestCase.getTestDirectory("temp");
        checkTempDir("Temp dir not empty before test");

        // Create the file system manager
        manager = new DefaultFileSystemManager();

        manager.setFilesCache(new SoftRefFilesCache());

        final DefaultFileReplicator replicator = new DefaultFileReplicator(tempDir);
        manager.setReplicator(new PrivilegedFileReplicator(replicator));
        manager.setTemporaryFileStore(replicator);

        providerConfig.prepare(manager);

        if (!manager.hasProvider("file"))
        {
            manager.addProvider("file", new DefaultLocalFileProvider(manager));
        }

        manager.init();

        // Locate the base folders
        baseFolder = providerConfig.getBaseTestFolder(manager);
        readFolder = baseFolder.resolveFile("read-tests");
        writeFolder = baseFolder.resolveFile("write-tests");

        // Make some assumptions about the read folder
        assertTrue("Folder does not exist: " + readFolder, readFolder.exists());
        assertFalse(readFolder.getName().getPath().equals(FileName.ROOT_PATH));

        // Configure the tests
        final Enumeration tests = testSuite.tests();
        while (tests.hasMoreElements())
        {
            final Test test = (Test) tests.nextElement();
            if (test instanceof AbstractProviderTestCase)
            {
                final AbstractProviderTestCase providerTestCase = (AbstractProviderTestCase) test;
                providerTestCase.setConfig(manager, baseFolder, readFolder, writeFolder);
            }
        }
    }

    protected void tearDown() throws Exception
    {
        manager.close();

        // Make sure temp directory is empty or gone
        checkTempDir("Temp dir not empty after test");
    }

    /**
     * Asserts that the temp dir is empty or gone.
     */
    private void checkTempDir(final String assertMsg)
    {
        if (tempDir.exists())
        {
            assertTrue(assertMsg, tempDir.isDirectory() && tempDir.list().length == 0);
        }
    }

}
