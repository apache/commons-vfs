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

import org.apache.commons.vfs.impl.test.VfsClassLoaderTests;

/**
 * The suite of tests for a file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @author Gary D. Gregory
 * @version $Id$
 */
public class ProviderTestSuite
    extends AbstractTestSuite
{
    /**
     * Adds the tests for a file system to this suite.
     */
    public ProviderTestSuite(final ProviderTestConfig providerConfig) throws Exception
    {
        this(providerConfig, "", false);
    }

    protected ProviderTestSuite(final ProviderTestConfig providerConfig,
                                final String prefix,
                                final boolean nested)
        throws Exception
    {
        super(providerConfig, prefix, nested);
    }

    /**
     * Adds base tests - excludes the nested test cases.
     */
    protected void addBaseTests() throws Exception
    {
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
        addTests(VfsClassLoaderTests.class);
    }
}
