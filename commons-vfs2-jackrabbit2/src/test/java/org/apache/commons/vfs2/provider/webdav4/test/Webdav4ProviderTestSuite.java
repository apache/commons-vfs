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

import org.apache.commons.vfs2.ContentTests;
import org.apache.commons.vfs2.LastModifiedTests;
import org.apache.commons.vfs2.NamingTests;
import org.apache.commons.vfs2.ProviderCacheStrategyTests;
import org.apache.commons.vfs2.ProviderDeleteTests;
import org.apache.commons.vfs2.ProviderRandomReadTests;
import org.apache.commons.vfs2.ProviderRandomReadWriteTests;
import org.apache.commons.vfs2.ProviderReadTests;
import org.apache.commons.vfs2.ProviderRenameTests;
import org.apache.commons.vfs2.ProviderTestConfig;
import org.apache.commons.vfs2.ProviderTestSuite;
import org.apache.commons.vfs2.ProviderWriteAppendTests;
import org.apache.commons.vfs2.ProviderWriteTests;
import org.apache.commons.vfs2.UriTests;
import org.apache.commons.vfs2.UrlStructureTests;
import org.apache.commons.vfs2.UrlTests;

/**
 * The suite of tests for a file system.
 *
 * @since 2.5.0
 */
public class Webdav4ProviderTestSuite extends ProviderTestSuite {
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
        super(providerConfig, prefix, nested, addEmptyDir);
    }

    /**
     * Adds base tests - excludes the nested test cases.
     */
    @Override
    protected void addBaseTests() throws Exception {
        addTests(ProviderCacheStrategyTests.class);
        addTests(UriTests.class);
        addTests(NamingTests.class);
        addTests(ContentTests.class);
        addTests(ProviderReadTests.class);
        addTests(ProviderRandomReadTests.class);
        addTests(ProviderWriteTests.class);
        addTests(ProviderWriteAppendTests.class);
        addTests(ProviderRandomReadWriteTests.class);
        addTests(ProviderRenameTests.class);
        addTests(ProviderDeleteTests.class);
        addTests(LastModifiedTests.class);
        addTests(UrlTests.class);
        addTests(UrlStructureTests.class);
        // The class loader test requires the classes be uploaded to the webdav repo.
        // addTests(VfsClassLoaderTests.class);
    }
}
