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
package org.apache.commons.vfs2.provider.webdav4.test;

import org.apache.commons.vfs2.ProviderTestConfig;
import org.apache.commons.vfs2.ProviderTestSuite;
import org.apache.commons.vfs2.impl.VfsClassLoaderTests;
import org.apache.commons.vfs2.impl.VfsThreadedClassLoaderTests;

import java.util.Arrays;
import java.util.HashSet;

/**
 * The suite of tests for a file system.
 *
 * @since 2.5.0
 */
public class Webdav4ProviderTestSuite extends ProviderTestSuite {

    // The class loader test requires the classes be uploaded to the webdav repo.
    private static final Class<?>[] EXCLUSIONS = new Class<?>[] {
            VfsClassLoaderTests.class, VfsThreadedClassLoaderTests.class
    };

    /**
     * Adds the tests for a file system to this suite.
     */
    public Webdav4ProviderTestSuite(final ProviderTestConfig providerConfig) throws Exception {
        this(providerConfig, "", false, false);
    }

    /**
     * Adds the tests for a file system to this suite. Provider has an empty directory.
     */
    public Webdav4ProviderTestSuite(final ProviderTestConfig providerConfig, final boolean addEmptyDir)
            throws Exception {
        this(providerConfig, "", false, addEmptyDir);
    }

    protected Webdav4ProviderTestSuite(final ProviderTestConfig providerConfig, final String prefix,
            final boolean nested, final boolean addEmptyDir) throws Exception {
        super(providerConfig, prefix, nested, addEmptyDir, new HashSet<>(Arrays.asList(EXCLUSIONS)));
    }

}
