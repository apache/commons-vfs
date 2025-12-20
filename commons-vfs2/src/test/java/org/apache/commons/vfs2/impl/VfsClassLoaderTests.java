/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.impl;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectoryFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.vfs2.AbstractProviderTestCase;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.junit.jupiter.api.Test;

/**
 * VfsClassLoader test cases.
 */
public class VfsClassLoaderTests extends AbstractProviderTestCase {

    private class LoadClass implements Runnable {
        private final VFSClassLoader loader;
        public LoadClass(final VFSClassLoader loader) {
            this.loader = loader;
        }

        @Override
        public void run() {
            try {
                final Class<?> testClass = loader.findClass("code.ClassToLoad");
                final Package pack = testClass.getPackage();
                assertEquals("code", pack.getName());
                verifyPackage(pack, false);

                final Object testObject = testClass.getConstructor().newInstance();
                assertEquals("**PRIVATE**", testObject.toString());
            } catch (final ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Non-Delegating Class Loader.
     */
    public static class MockClassloader extends ClassLoader {
        MockClassloader() {
            super(null);
        }

        @Override
        protected Class<?> findClass(final String name) throws ClassNotFoundException {
            fail("Not intended to be used for class loading.");
            return null;
        }

        /**
         * This method will not return any hit to VFSClassLoader#testGetResourcesJARs.
         */
        @Override
        public Enumeration<URL> getResources(final String name) {
            return Collections.enumeration(Collections.emptyList());
        }
    }

    /**
     * Creates the classloader to use when testing.
     */
    private VFSClassLoader createClassLoader() throws FileSystemException {
        return new VFSClassLoader(getBaseFolder(), getManager());
    }

    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCapabilities() {
        return new Capability[] { Capability.READ_CONTENT, Capability.URI };
    }

    /**
     * Tests retrieving resources (from JAR searchpath).
     * <p>
     * This is run for all providers, but only when a local provider is present and jar extension is registered it will
     * actually carry out all tests.
     * </p>
     */
    @Test
    public void testGetResourcesJARs() throws Exception {
        final FileSystemManager manager = getManager();
        try {
            // hasProvider("file") cannot be used as it triggers default provider URL
            manager.toFileObject(new File("."));
        } catch (final FileSystemException e) {
            System.out.println("VfsClassLoaderTests no local file provider, skipping.");
            return;
        }

        // build search path without using #getBaseFolder()
        // because NestedJarTestCase redefines it
        final File baseDir = getTestDirectoryFile();
        final FileObject nestedJar = manager.resolveFile(baseDir, "nested.jar");
        final FileObject testJar = manager.resolveFile(baseDir, "test.jar");

        // test setup needs to know about .jar extension - i.e. NestedJarTestCase
        if (!manager.canCreateFileSystem(nestedJar)) {
            System.out.println("VfsClassLoaderTests no layered .jar provider, skipping.");
            return;
        }

        // verify test setup
        assertSame(FileType.FILE, nestedJar.getType(), "nested.jar is required for testing");
        assertSame(FileType.FILE, testJar.getType(), "test.jar is required for testing");

        // System class loader (null) might be unpredictable in regards
        // to returning resources for META-INF/MANIFEST.MF (see VFS-500)
        // so we use our own which is guaranteed to not return any hit
        final ClassLoader mockClassloader = new MockClassloader();
        final FileObject[] search = { nestedJar, testJar };
        final VFSClassLoader loader = new VFSClassLoader(search, getManager(), mockClassloader);

        final Enumeration<URL> urls = loader.getResources("META-INF/MANIFEST.MF");
        final URL url1 = urls.nextElement();
        final URL url2 = urls.nextElement();

        assertTrue(url1.toString().endsWith("nested.jar!/META-INF/MANIFEST.MF"),
                "First resource must refer to nested.jar but was " + url1);
        assertTrue(url2.toString().endsWith("test.jar!/META-INF/MANIFEST.MF"),
                "Second resource must refer to test.jar but was " + url2);
    }

    /**
     * Tests retrieving resources (from local directory with .jar extension).
     * <p>
     * This test is repeated with various provider configurations but works on local files, only.
     * </p>
     */
    @Test
    public void testGetResourcesNoLayerLocal() throws Exception {
        final FileSystemManager manager = getManager();
        try {
            // hasProvider("file") cannot be used as it triggers default provider URL
            manager.toFileObject(new File("."));
        } catch (final FileSystemException e) {
            System.out.println("VfsClassLoaderTests no local file provider, skipping.");
            return;
        }
        final File baseDir = getTestDirectoryFile();

        // setup test folder
        final FileObject dir = manager.resolveFile(baseDir, "read-tests/dir1/subdir4.jar");
        assertSame(FileType.FOLDER, dir.getType(), "subdir4.jar/ is required for testing " + dir);
        assertFalse(manager.canCreateFileSystem(dir));

        // prepare classloader
        final FileObject[] search = { dir };
        final ClassLoader mockClassloader = new MockClassloader();
        final VFSClassLoader loader = new VFSClassLoader(search, getManager(), mockClassloader);

        // verify resource loading
        final Enumeration<URL> urls = loader.getResources("file1.txt");
        final URL url1 = urls.nextElement();
        assertFalse(urls.hasMoreElements(), "Only one hit expected");
        assertTrue(url1.toString().endsWith("subdir4.jar/file1.txt"), "not pointing to resource " + url1);
    }

    /**
     * Tests loading a class.
     */
    @Test
    public void testLoadClass() throws Exception {
        final VFSClassLoader loader = createClassLoader();

        final Class<?> testClass = loader.loadClass("code.ClassToLoad");
        final Package pack = testClass.getPackage();
        assertEquals("code", pack.getName());
        verifyPackage(pack, false);

        final Object testObject = testClass.getConstructor().newInstance();
        assertEquals("**PRIVATE**", testObject.toString());
    }

    /**
     * Tests loading a resource.
     */
    @Test
    public void testLoadResource() throws Exception {
        final VFSClassLoader loader = createClassLoader();

        final URL resource = loader.getResource("read-tests/file1.txt");

        assertNotNull(resource);
        final URLConnection urlCon = resource.openConnection();
        assertSameURLContent(FILE1_CONTENT, urlCon);
    }

    /**
     * Tests package sealing.
     */
    @Test
    public void testSealing() throws Exception {
        final VFSClassLoader loader = createClassLoader();
        final Class<?> testClass = loader.loadClass("code.sealed.AnotherClass");
        final Package pack = testClass.getPackage();
        assertEquals("code.sealed", pack.getName());
        verifyPackage(pack, true);
    }

    @Test
    public void testThreadSafety() throws Exception {
        final int THREADS = 40;
        final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(THREADS * 2);
        final List<Throwable> exceptions = new ArrayList<>();
        final Thread.UncaughtExceptionHandler handler = (t, e) -> {
            synchronized (exceptions) {
                exceptions.add(e);
            }
        };
        final ThreadFactory factory = r -> {
            final Thread thread = new Thread(r, "VfsClassLoaderTests.testThreadSafety");
            thread.setUncaughtExceptionHandler(handler);
            return thread;
        };
        final Queue<Runnable> rejections = new LinkedList<>();
        final RejectedExecutionHandler rejectionHandler = (r, executor) -> {
            synchronized (rejections) {
                rejections.add(r);
            }
        };
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(THREADS, THREADS, 0, TimeUnit.SECONDS, workQueue, factory, rejectionHandler);
        executor.prestartAllCoreThreads();
        for (int i = 0; i < THREADS; i++) {
            final VFSClassLoader loader = createClassLoader();
            workQueue.put(new VfsClassLoaderTests.LoadClass(loader));
        }
        while (!workQueue.isEmpty()) {
            Thread.sleep(10);
        }
        while (!rejections.isEmpty() && executor.getActiveCount() > 0) {
            final List<Runnable> rejected = new ArrayList<>();
            synchronized(rejections) {
                rejected.addAll(rejections);
                rejections.clear();
            }
            workQueue.addAll(rejected);
        }
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        assertEquals(THREADS, executor.getCompletedTaskCount());
        if (!exceptions.isEmpty()) {
            final StringBuilder exceptionMsg = new StringBuilder();
            final StringBuilderWriter writer = new StringBuilderWriter(exceptionMsg);
            try (PrintWriter printWriter = new PrintWriter(writer)) {
                for (final Throwable t : exceptions) {
                    printWriter.write(t.getMessage());
                    printWriter.write('\n');
                    t.printStackTrace(printWriter);
                    printWriter.write('\n');
                }
                printWriter.flush();
            }
            assertTrue(exceptions.isEmpty(), exceptions.size() + " threads failed: " + exceptionMsg);
        }
    }

    /**
     * Verify the package loaded with class loader.
     */
    private void verifyPackage(final Package pack, final boolean sealed) {
        if (getBaseFolder().getFileSystem().hasCapability(Capability.MANIFEST_ATTRIBUTES)) {
            assertEquals("ImplTitle", pack.getImplementationTitle());
            assertEquals("ImplVendor", pack.getImplementationVendor());
            assertEquals("1.1", pack.getImplementationVersion());
            assertEquals("SpecTitle", pack.getSpecificationTitle());
            assertEquals("SpecVendor", pack.getSpecificationVendor());
            assertEquals("1.0", pack.getSpecificationVersion());
            assertEquals(sealed, pack.isSealed());
        } else {
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
