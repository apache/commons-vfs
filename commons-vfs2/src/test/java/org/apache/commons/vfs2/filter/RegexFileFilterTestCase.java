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
package org.apache.commons.vfs2.filter;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileFilter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link RegexFileFilter}.
 */
// CHECKSTYLE:OFF Test code
public class RegexFileFilterTestCase extends BaseFilterTest {

    @Test
    public void testRegex() throws Exception {

        FileFilter filter;

        filter = new RegexFileFilter("^.*[tT]est(-\\d+)?\\.java$");
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("Test.java"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test-10.java"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test-.java"))));

        filter = new RegexFileFilter("^[Tt]est.java$");
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("Test.java"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.java"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("tEST.java"))));

        filter = new RegexFileFilter(Pattern.compile("^test.java$", Pattern.CASE_INSENSITIVE));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("Test.java"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.java"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("tEST.java"))));

        filter = new RegexFileFilter("^test.java$", Pattern.CASE_INSENSITIVE);
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("Test.java"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.java"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("tEST.java"))));

        filter = new RegexFileFilter("^test.java$", IOCase.INSENSITIVE);
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("Test.java"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.java"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("tEST.java"))));

    }

    @Test
    public void testStringNullArgConstruction() {
        try {
            new RegexFileFilter((String) null);
            fail();
        } catch (final IllegalArgumentException ex) {
            Assert.assertEquals(RegexFileFilter.PATTERN_IS_MISSING, ex.getMessage());
        }
    }

    @Test
    public void testPatternNullArgConstruction() {
        try {
            new RegexFileFilter((Pattern) null);
            fail();
        } catch (final IllegalArgumentException ex) {
            Assert.assertEquals(RegexFileFilter.PATTERN_IS_MISSING, ex.getMessage());
        }
    }

    @Test
    public void testStringPatternNullArgConstruction() {
        try {
            new RegexFileFilter((String) null, Pattern.CASE_INSENSITIVE);
            fail();
        } catch (final IllegalArgumentException ex) {
            Assert.assertEquals(RegexFileFilter.PATTERN_IS_MISSING, ex.getMessage());
        }
    }

    @Test
    public void testStringIOCaseNullArgConstruction() {
        try {
            new RegexFileFilter((String) null, IOCase.INSENSITIVE);
            fail();
        } catch (final IllegalArgumentException ex) {
            Assert.assertEquals(RegexFileFilter.PATTERN_IS_MISSING, ex.getMessage());
        }
    }

}
// CHECKSTYLE:ON
