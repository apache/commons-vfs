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
package org.apache.commons.vfs2.operations;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.operations.vcs.VcsLog;
import org.apache.commons.vfs2.provider.FileProvider;
import org.apache.commons.vfs2.provider.VfsComponent;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Basic Tests for the FileOperations and FileOperationsProvider API.
 */
public class BasicOperationsTestCase {
    /** This FileOperationsProvider is a VfsComponent and records invocations. */
    static class MyFileOperationProviderComp extends MyFileOprationProviderBase implements VfsComponent {
        @Override
        public void setLogger(final Log logger) {
            assertNotNull("setLogger", logger);
            ops |= 1;
        }

        @Override
        public void setContext(final VfsComponentContext context) {
            assertNotNull("setContext", context);
            ops |= 2;
        }

        @Override
        public void init() throws FileSystemException {
            ops |= 4;
        }

        @Override
        public void close() {
            ops |= 8;
        }
    }

    /** This FileOperationsProvider is no VfsComponent. */
    static class MyFileOperationProviderNoncomp extends MyFileOprationProviderBase {
    }

    /**
     * Base class for different Test Providers. This is also a compile test to ensure interface stability.
     */
    static class MyFileOprationProviderBase implements FileOperationProvider {
        int ops = 0; // bit array to record invocations (poor mans mock)

        @Override
        public void collectOperations(final Collection<Class<? extends FileOperation>> operationsList,
                final FileObject file) throws FileSystemException {
            assertNotNull("collect operationsList", operationsList);
            assertNotNull("collect file", file);
            ops |= 16;
        }

        @Override
        public FileOperation getOperation(final FileObject file, final Class<? extends FileOperation> operationClass)
                throws FileSystemException {
            assertNotNull("file object", file);
            assertNotNull("operationclass", operationClass);
            ops |= 32;
            return null;
        }
    }

    /** FSM to work with, maintained by JUnit Fixture. */
    private DefaultFileSystemManager manager;

    /**
     * JUnit Fixture: Prepare a simple FSM.
     *
     * @throws FileSystemException for runtime problems
     */
    @Before
    public void setUp() throws FileSystemException {
        manager = new DefaultFileSystemManager();
        final FileProvider fp = new DefaultLocalFileProvider();
        manager.addProvider("file", fp);
        manager.init();
    }

    /**
     * Ensure FileOperationProviders which are VfsComponents are set up and teared down.
     *
     * @throws FileSystemException for runtime problems
     */
    @Test
    public void testLifecycleComp() throws FileSystemException {
        final MyFileOprationProviderBase myop = new MyFileOperationProviderComp();
        assertEquals(0, myop.ops);
        manager.addOperationProvider("file", myop);
        assertEquals(7, myop.ops);
        manager.close();
        assertEquals("close() not called", 15, myop.ops); // VFS-577

        // fixture will close again
    }

    /**
     * Ensure you can use FileOperationProvider which is not a VfsComponnt.
     *
     * @throws FileSystemException for runtime problems
     */
    @Test
    public void testLifecycleNoncomp() throws FileSystemException {
        final MyFileOprationProviderBase myop = new MyFileOperationProviderNoncomp();
        manager.addOperationProvider("file", myop);
        final FileOperationProvider[] ops = manager.getOperationProviders("file");
        assertSame("exactly one provider registered", 1, ops.length);
        assertSame(myop, ops[0]);
        assertEquals(0, myop.ops); // collect not invoked
    }

    /**
     * Ensures getOperations calls collect and allows empty response.
     *
     * @throws FileSystemException for runtime problems
     */
    @Test
    public void testNotFoundAny() throws FileSystemException {
        final MyFileOprationProviderBase myop = new MyFileOperationProviderNoncomp();
        manager.addOperationProvider("file", myop);
        final FileObject fo = manager.toFileObject(new File("."));

        final FileOperations ops = fo.getFileOperations();
        assertNotNull(ops);

        final Class<? extends FileOperation>[] oparray = ops.getOperations();
        assertSame("no ops should be found", 0, oparray.length);
        assertSame(16, myop.ops); // collect
    }

    /**
     * Ensure proper response for not found FileOperation.
     *
     * @throws FileSystemException for runtime problems
     */
    @Test
    public void testNotFoundOperation() throws FileSystemException {
        final MyFileOprationProviderBase myop = new MyFileOperationProviderNoncomp();
        manager.addOperationProvider("file", myop);
        final FileObject fo = manager.toFileObject(new File("."));

        final FileOperations ops = fo.getFileOperations();
        assertNotNull(ops);

        try {
            final FileOperation logop = ops.getOperation(VcsLog.class);
            fail("Must throw but returned " + logop);
        } catch (final FileSystemException e) {
            assertEquals("vfs.operation/operation-not-supported.error", e.getCode());
        }
        assertSame(32, myop.ops); // getOperation was called
    }

    /**
     * JUnit Fixture: Tear Down the FSM.
     *
     * @throws FileSystemException for runtime problems
     */
    @After
    public void tearDown() throws FileSystemException {
        if (manager != null) {
            manager.close();
            manager = null;
        }
    }
}
