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
package org.apache.commons.vfs2;

import org.apache.commons.vfs2.impl.VfsClassLoaderTests;
import org.apache.commons.vfs2.impl.VfsThreadedClassLoaderTests;

import java.util.Collections;
import java.util.Set;

/**
 * The suite of tests for a file system.
 */
public class ProviderTestSuite extends AbstractTestSuite {

    /**
     * The list of base tests to be added to the test suite
     */
    private final static Class<?>[] BASE_TESTS = new Class<?>[] {
            UrlTests.class,
            ProviderCacheStrategyTests.class,
            UriTests.class,
            NamingTests.class,
            ContentTests.class,
            ProviderReadTests.class,
            ProviderWriteTests.class,
            ProviderWriteAppendTests.class,
            ProviderRandomReadTests.class,
            ProviderRandomReadWriteTests.class,
            ProviderRandomSetLengthTests.class,
            ProviderRenameTests.class,
            ProviderDeleteTests.class,
            LastModifiedTests.class,
            UrlStructureTests.class,
            VfsClassLoaderTests.class,
            VfsThreadedClassLoaderTests.class};

    /**
     * Adds the tests for a file system to this suite.
     */
    public ProviderTestSuite(final ProviderTestConfig providerConfig) throws Exception {
        this(providerConfig, "", false, false);
    }

    /**
     * Adds the tests for a file system to this suite except for specified exclusions
     */
    public ProviderTestSuite(final ProviderTestConfig providerConfig, Set<Class<?>> exclusions) throws Exception {
        this(providerConfig, "", false, false, exclusions);
    }

    /**
     * Adds the tests for a file system to this suite. Provider has an empty directory.
     */
    public ProviderTestSuite(final ProviderTestConfig providerConfig, final boolean addEmptyDir) throws Exception {
        this(providerConfig, "", false, addEmptyDir);
    }

    protected ProviderTestSuite(final ProviderTestConfig providerConfig, final String prefix, final boolean nested,
            final boolean addEmptyDir) throws Exception {
        this(providerConfig, prefix, nested, addEmptyDir, Collections.emptySet());
    }

    protected ProviderTestSuite(final ProviderTestConfig providerConfig, final String prefix, final boolean nested,
                                final boolean addEmptyDir, final Set<Class<?>> exclusions) throws Exception {
        super(providerConfig, prefix, nested, addEmptyDir, exclusions);
    }

    /**
     * Adds base tests - excludes the nested test cases and specified exclusions.
     */
    @Override
    protected void addBaseTests(Set<Class<?>> exclusions) throws Exception {
        for (Class<?> testClass : BASE_TESTS) {
            if (!exclusions.contains(testClass)) {
                addTests(testClass);
            }
        }
    }

}
