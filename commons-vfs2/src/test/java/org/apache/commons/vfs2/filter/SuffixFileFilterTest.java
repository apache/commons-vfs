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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileSystemException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link SuffixFileFilter}.
 */
// CHECKSTYLE:OFF Test code
public class SuffixFileFilterTest extends BaseFilterTest {

    @Test
    public void testAcceptList() throws FileSystemException {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add(".txt");
        list.add(".bin");
        final SuffixFileFilter filter = new SuffixFileFilter(list);

        // TEST
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.bin"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test2.BIN"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));

    }

    @Test
    public void testAcceptListIOCaseInsensitive() throws FileSystemException {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add(".txt");
        list.add(".bin");
        final SuffixFileFilter filter = new SuffixFileFilter(IOCase.INSENSITIVE, list);

        // TEST
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("TEST1.txt"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.bin"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.TXT"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));

    }

    @Test
    public void testAcceptListIOCaseSensitive() throws FileSystemException {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add(".txt");
        list.add(".bin");
        final SuffixFileFilter filter = new SuffixFileFilter(IOCase.SENSITIVE, list);

        // TEST
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test1.Txt"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test2.BIN"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));

    }

    @Test
    public void testAcceptString() throws FileSystemException {

        // PREPARE
        final SuffixFileFilter filter = new SuffixFileFilter(".txt", ".xxx");

        // TEST
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test2.TXT"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.xxx"))));

    }

    @Test
    public void testAcceptStringIOCaseInsensitive() throws FileSystemException {

        // PREPARE
        final SuffixFileFilter filter = new SuffixFileFilter(IOCase.INSENSITIVE, ".txt", ".xxx");

        // TEST
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.TXT"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.xxx"))));

    }

    @Test
    public void testAcceptStringIOCaseSensitive() throws FileSystemException {

        // PREPARE
        final SuffixFileFilter filter = new SuffixFileFilter(IOCase.SENSITIVE, ".txt", ".xxx");

        // TEST
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test2.TXT"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.xxx"))));

    }

}
// CHECKSTYLE:ON
