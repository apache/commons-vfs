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
package org.apache.commons.vfs2.provider;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class UriParserTestCase {

	private static final String[] schemes = new String[2];

	@BeforeClass
	public static void setupSchemes() {
		schemes[0] = "ftp";
		schemes[1] = "file";
	}

	@Test
	public void testColonInFileNameAndNotSupportedScheme() {
        Assert.assertNull(UriParser.extractScheme(schemes, "some:file"));
	}

	@Test
	public void testColonInFileNameWithPath() {
        Assert.assertNull(UriParser.extractScheme(schemes, "some/path/some:file"));
	}

	@Test
	public void testNormalScheme() {
		Assert.assertEquals("ftp", UriParser.extractScheme(schemes, "ftp://user:pass@host/some/path/some:file"));
	}

	@Test
	public void testOneSlashScheme() {
		Assert.assertEquals("file", UriParser.extractScheme(schemes, "file:/user:pass@host/some/path/some:file"));
	}

	@Test
	public void testColonNotFollowedBySlash() {
		Assert.assertEquals("file", UriParser.extractScheme(schemes, "file:user/subdir/some/path/some:file"));
	}

	@Test
	public void testNormalSchemeWithBuffer() {
		final StringBuilder buffer = new StringBuilder();
		UriParser.extractScheme(schemes, "ftp://user:pass@host/some/path/some:file", buffer);
		Assert.assertEquals("//user:pass@host/some/path/some:file", buffer.toString());
	}

	@Test
	public void testOneSlashSchemeWithBuffer() {
		final StringBuilder buffer = new StringBuilder();
		UriParser.extractScheme(schemes, "file:/user:pass@host/some/path/some:file", buffer);
		Assert.assertEquals("/user:pass@host/some/path/some:file", buffer.toString());
	}

	@Test
	public void testColonInFileName() {
        Assert.assertNull(UriParser.extractScheme("some/path/some:file"));
	}

}
