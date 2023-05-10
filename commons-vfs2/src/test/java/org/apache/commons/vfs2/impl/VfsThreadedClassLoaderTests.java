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
package org.apache.commons.vfs2.impl;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.vfs2.AbstractProviderTestCase;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * VfsClassLoader test cases.
 */
public class VfsThreadedClassLoaderTests extends AbstractProviderTestCase {

    final static Map<String, Long> TEST_FILES = Arrays.asList(new Object[][] {
            {"read-tests/empty.txt",0L},
            {"read-tests/file1.txt",20L},
            {"read-tests/dir1/file1.txt",12L},
            {"read-tests/dir1/file2.txt",12L},
            {"read-tests/dir1/subdir2/file1.txt",12L},
            {"read-tests/dir1/subdir2/file2.txt",12L},
            {"read-tests/dir1/subdir2/file3.txt",12L},
            {"read-tests/dir1/subdir3/file1.txt",12L},
            {"read-tests/dir1/subdir3/file2.txt",12L},
            {"read-tests/dir1/subdir3/file3.txt",12L},
            {"read-tests/dir1/file3.txt",12L},
            {"read-tests/dir1/subdir4.jar/file1.txt",12L},
            {"read-tests/dir1/subdir4.jar/file2.txt",12L},
            {"read-tests/dir1/subdir4.jar/file3.txt",12L},
            {"read-tests/dir1/subdir1/file1.txt",12L},
            {"read-tests/dir1/subdir1/file2.txt",12L},
            {"read-tests/dir1/subdir1/file3.txt",12L},
            //{"read-tests/largefile.txt", 3221225472L},
            {"read-tests/file space.txt",20L}
    }).stream().collect(Collectors.toMap(o -> (String)o[0], o -> (Long)o[1]));

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

    @Test
    public void testThreadSafety() throws Exception {
        // note THREADS must be an even number
        final int THREADS = 20;
        final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(THREADS * 2);
        final List<Throwable> exceptions = new ArrayList<>();
        final Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                synchronized (exceptions) {
                    exceptions.add(e);
                }
            }
        };
        final ThreadFactory factory = new ThreadFactory() {
            private int count = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "VfsClassLoaderTests.testThreadSafety #" + count++);
                thread.setUncaughtExceptionHandler(handler);
                return thread;
            }
        };
        final Queue<Runnable> rejections = new LinkedList<>();
        final RejectedExecutionHandler rejectionHandler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                synchronized (rejections) {
                    rejections.add(r);
                }
            }
        };
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(THREADS, THREADS, 0, TimeUnit.SECONDS, workQueue, factory, rejectionHandler);
        executor.prestartAllCoreThreads();
        VFSClassLoader resourceLoader = createClassLoader();
        final CyclicBarrier barrier = new CyclicBarrier(THREADS);
        for (int i = 0; i < THREADS/2; i++) {
            final VFSClassLoader loader = createClassLoader();
            workQueue.put(new VfsThreadedClassLoaderTests.LoadClass(barrier, loader));
            workQueue.put(new VfsThreadedClassLoaderTests.ReadResource(barrier, resourceLoader));
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
        executor.awaitTermination(60, TimeUnit.SECONDS);
        assertEquals(THREADS, executor.getCompletedTaskCount());
        if (!exceptions.isEmpty()) {
            StringBuilder exceptionMsg = new StringBuilder();
            StringBuilderWriter writer = new StringBuilderWriter(exceptionMsg);
            PrintWriter pWriter = new PrintWriter(writer);
            for (Throwable t : exceptions) {
                pWriter.write(String.valueOf(t.getMessage()));
                pWriter.write('\n');
                t.printStackTrace(pWriter);
                pWriter.write('\n');
            }
            pWriter.flush();
            assertTrue(exceptions.size() + " threads failed: " + exceptionMsg, exceptions.isEmpty());
        }
    }

    private class LoadClass implements Runnable {
        private final VFSClassLoader loader;
        private final CyclicBarrier barrier;

        public LoadClass(CyclicBarrier barrier, VFSClassLoader loader) {
            this.barrier = barrier;
            this.loader = loader;
        }

        @Override
        public void run() {
            try {
                barrier.await();
                final Class<?> testClass = loader.findClass("code.ClassToLoad");
                final Package pack = testClass.getPackage();
                assertEquals("code", pack.getName());
                verifyPackage(pack, false);

                final Object testObject = testClass.newInstance();
                assertEquals("**PRIVATE**", testObject.toString());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class ReadResource implements Runnable {
        private final VFSClassLoader loader;
        private final CyclicBarrier barrier;

        public ReadResource(CyclicBarrier barrier, VFSClassLoader loader) {
            this.barrier = barrier;
            this.loader = loader;
        }

        @Override
        public void run() {
            try {
                barrier.await();
                List<Map.Entry<String, Long>> files = new ArrayList<>(TEST_FILES.entrySet());
                Collections.shuffle(files);
                for (int i = 0; i < 10; i++) {
                    for (Map.Entry<String, Long> file : files) {
                        testFindResource(file.getKey(), file.getValue());
                        testGetResource(file.getKey(), file.getValue());
                        testResourceAsStream(file.getKey(), file.getValue());
                    }
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }

        private void testResourceAsStream(String file, long size) {
            try {
                try (InputStream stream = loader.getResourceAsStream(file)) {
                    if (stream == null) {
                        loader.getResourceAsStream(file);
                    }
                    readStream(file, stream, size);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to read " + file + " on thread " + Thread.currentThread(), e);
            }
        }
        private void testGetResource(String file, long size) {
            try {
                URL url = loader.getResource(file);
                try (InputStream stream = url.openStream()) {
                    readStream(file, stream, size);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to read " + file + " on thread " + Thread.currentThread(), e);
            }
        }
        private void testFindResource(String file, long size) {
            try {
                URL url = loader.findResource(file);
                try (InputStream stream = url.openStream()) {
                    readStream(file, stream, size);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to read " + file + " on thread " + Thread.currentThread(), e);
            }
        }
        private void readStream(String file, InputStream stream, long size) throws IOException {
            long length = 0;
            byte[] bytes = new byte[1024];
            int readBytes = stream.read(bytes);
            while (readBytes >= 0) {
                length += readBytes;
                readBytes = stream.read(bytes);
            }
            assertEquals("Incorrect length for " + file + " on thread " + Thread.currentThread(), length, size);
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
