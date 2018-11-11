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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.apache.commons.io.FileUtils;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileReplicator;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.impl.PrivilegedFileReplicator;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;

/**
 * The suite of tests for a file system.
 */
public abstract class AbstractTestSuite extends TestSetup {
    private final ProviderTestConfig providerConfig;
    private final String prefix;
    private TestSuite testSuite;

    private FileObject baseFolder;
    private FileObject readFolder;
    private FileObject writeFolder;
    private DefaultFileSystemManager manager;
    private File tempDir;

    private Thread[] startThreadSnapshot;
    private Thread[] endThreadSnapshot;
    private final boolean addEmptyDir;

    /**
     * Adds the tests for a file system to this suite.
     */
    public AbstractTestSuite(final ProviderTestConfig providerConfig) throws Exception {
        this(providerConfig, "", false, false);
    }

    protected AbstractTestSuite(final ProviderTestConfig providerConfig, final String prefix, final boolean nested)
            throws Exception {
        this(providerConfig, prefix, nested, false);
    }

    protected AbstractTestSuite(final ProviderTestConfig providerConfig, final String prefix, final boolean nested,
            final boolean addEmptyDir) throws Exception {
        super(new TestSuite());
        testSuite = (TestSuite) fTest;
        this.providerConfig = providerConfig;
        this.prefix = prefix;
        this.addEmptyDir = addEmptyDir;
        addBaseTests();
        if (!nested) {
            // Add nested tests
            // TODO - move nested jar and zip tests here
            // TODO - enable this again
            // testSuite.addTest( new ProviderTestSuite( new JunctionProviderConfig( providerConfig ), "junction.", true
            // ));
        }
    }

    /**
     * Adds base tests - excludes the nested test cases.
     */
    protected void addBaseTests() throws Exception {
    }

    /**
     * Adds the tests from a class to this suite. The supplied class must be a subclass of
     * {@link AbstractProviderTestCase} and have a public a no-args constructor. This method creates an instance of the
     * supplied class for each public 'testNnnn' method provided by the class.
     */
    public void addTests(final Class<?> testClass) throws Exception {
        // Verify the class
        if (!AbstractProviderTestCase.class.isAssignableFrom(testClass)) {
            throw new Exception("Test class " + testClass.getName() + " is not assignable to "
                    + AbstractProviderTestCase.class.getName());
        }

        // Locate the test methods
        final Method[] methods = testClass.getMethods();
        for (final Method method2 : methods) {
            final Method method = method2;
            if (!method.getName().startsWith("test") || Modifier.isStatic(method.getModifiers())
                    || method.getReturnType() != Void.TYPE || method.getParameterTypes().length != 0) {
                continue;
            }

            // Create instance
            final AbstractProviderTestCase testCase = (AbstractProviderTestCase) testClass.newInstance();
            testCase.setMethod(method);
            testCase.setName(prefix + method.getName());
            testCase.addEmptyDir(this.addEmptyDir);
            testSuite.addTest(testCase);
        }
    }

    @Override
    public void run(final TestResult result) {
        final Protectable p = new Protectable() {
            @Override
            public void protect() throws Exception {
                setUp();
                basicRun(result);
                tearDown();
                validateThreadSnapshot();
            }
        };
        result.runProtected(this, p);
    }

    @Override
    protected void setUp() throws Exception {
        startThreadSnapshot = createThreadSnapshot();

        // Locate the temp directory, and clean it up
        tempDir = AbstractVfsTestCase.getTestDirectory("temp");
        FileUtils.cleanDirectory(tempDir);
        checkTempDir("Temp dir not empty before test");

        // Create the file system manager
        manager = providerConfig.getDefaultFileSystemManager();
        manager.setFilesCache(providerConfig.getFilesCache());

        final DefaultFileReplicator replicator = new DefaultFileReplicator(tempDir);
        manager.setReplicator(new PrivilegedFileReplicator(replicator));
        manager.setTemporaryFileStore(replicator);

        providerConfig.prepare(manager);

        if (!manager.hasProvider("file")) {
            manager.addProvider("file", new DefaultLocalFileProvider());
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
        final Enumeration<Test> tests = testSuite.tests();
        while (tests.hasMoreElements()) {
            final Test test = tests.nextElement();
            if (test instanceof AbstractProviderTestCase) {
                final AbstractProviderTestCase providerTestCase = (AbstractProviderTestCase) test;
                providerTestCase.setConfig(manager, providerConfig, baseFolder, readFolder, writeFolder);
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        readFolder.close();
        writeFolder.close();
        baseFolder.close();

        readFolder = null;
        writeFolder = null;
        baseFolder = null;
        testSuite = null;

        // force the SoftRefFilesChache to free all files
        System.gc();
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);

        manager.freeUnusedResources();
        manager.close();

        // Make sure temp directory is empty or gone
        checkTempDir("Temp dir not empty after test");
    }

    private void validateThreadSnapshot() {
        endThreadSnapshot = createThreadSnapshot();

        final Thread[] diffThreadSnapshot = diffThreadSnapshot(startThreadSnapshot, endThreadSnapshot);
        if (diffThreadSnapshot.length > 0) {
            final String message = dumpThreadSnapshot(diffThreadSnapshot);
            /*
             * if (providerConfig.checkCleanThreadState()) { // close the manager to do a "not thread safe" release of
             * all resources // and allow the vm to shutdown manager.close(); fail(message); } else {
             */
            System.out.println(message);
            // }
        }
        // System.in.read();
    }

    /**
     * Asserts that the temp dir is empty or gone.
     */
    private void checkTempDir(final String assertMsg) {
        if (tempDir.exists()) {
            assertTrue(assertMsg + " (" + tempDir.getAbsolutePath() + ")",
                    tempDir.isDirectory() && tempDir.list().length == 0);
        }
    }

    private String dumpThreadSnapshot(final Thread[] threadSnapshot) {
        final StringBuffer sb = new StringBuffer(256);
        sb.append("created threads still running:\n");

        Field threadTargetField = null;
        try {
            threadTargetField = Thread.class.getDeclaredField("target");
            threadTargetField.setAccessible(true);
        } catch (final NoSuchFieldException e) {
            // ignored
        }

        for (int iter = 0; iter < threadSnapshot.length; iter++) {
            final Thread thread = threadSnapshot[iter];
            if (thread == null || !thread.isAlive()) {
                continue;
            }

            sb.append("#");
            sb.append(iter + 1);
            sb.append(": ");
            sb.append(thread.getThreadGroup() != null ? thread.getThreadGroup().getName() : "(null)");
            sb.append("\t");
            sb.append(thread.getName());
            sb.append("\t");
            sb.append(thread.getState());
            sb.append("\t");
            if (thread.isDaemon()) {
                sb.append("daemon");
            } else {
                sb.append("not_a_daemon");
            }

            if (threadTargetField != null) {
                sb.append("\t");
                try {
                    final Object threadTarget = threadTargetField.get(thread);
                    if (threadTarget != null) {
                        sb.append(threadTarget.getClass());
                    } else {
                        sb.append("null");
                    }
                } catch (final IllegalAccessException e) {
                    sb.append("unknown class");
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private Thread[] diffThreadSnapshot(final Thread[] startThreadSnapshot, final Thread[] endThreadSnapshot) {
        final List<Thread> diff = new ArrayList<>(10);

        nextEnd: for (int iterEnd = 0; iterEnd < endThreadSnapshot.length; iterEnd++) {
            for (int iterStart = 0; iterStart < startThreadSnapshot.length; iterStart++) {
                if (startThreadSnapshot[iterStart] == endThreadSnapshot[iterEnd]) {
                    continue nextEnd;
                }
            }

            diff.add(endThreadSnapshot[iterEnd]);
        }

        final Thread ret[] = new Thread[diff.size()];
        diff.toArray(ret);
        return ret;
    }

    private Thread[] createThreadSnapshot() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        while (tg.getParent() != null) {
            tg = tg.getParent();
        }

        final Thread snapshot[] = new Thread[200];
        tg.enumerate(snapshot, true);

        return snapshot;
    }
}
