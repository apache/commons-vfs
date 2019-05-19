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
 * Test for {@link WildcardFileFilter}.
 */
// CHECKSTYLE:OFF Test code
public class WildcardFileFilterTest extends BaseFilterTest {

    @Test
    public void testAcceptList() throws FileSystemException {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add("*.txt");
        list.add("*.a??");
        final WildcardFileFilter filter = new WildcardFileFilter(list);

        // TEST
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.a"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.ab"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.abc"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.ABC"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.aaa"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.Aaa"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.aAA"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.abcd"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));

    }

    @Test
    public void testAcceptListIOCaseInsensitive() throws FileSystemException {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add("*.txt");
        list.add("*.a??");
        final WildcardFileFilter filter = new WildcardFileFilter(IOCase.INSENSITIVE, list);

        // TEST
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.a"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.ab"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.abc"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.ABC"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.aaa"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.Aaa"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.aAA"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.abcd"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));

    }

    @Test
    public void testAcceptListIOCaseSensitive() throws FileSystemException {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add("*.txt");
        list.add("*.a??");
        final WildcardFileFilter filter = new WildcardFileFilter(IOCase.SENSITIVE, list);

        // TEST
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.a"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.ab"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.abc"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.ABC"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.aaa"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.Aaa"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.aAA"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.abcd"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));

    }

    @Test
    public void testAcceptString() throws FileSystemException {

        // PREPARE
        final WildcardFileFilter filter = new WildcardFileFilter("*.txt", "*.a??");

        // TEST
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.a"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.ab"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.abc"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.ABC"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.aaa"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.Aaa"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.aAA"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.abcd"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));

    }

    @Test
    public void testAcceptStringIOCaseInsensitive() throws FileSystemException {

        // PREPARE
        final WildcardFileFilter filter = new WildcardFileFilter(IOCase.INSENSITIVE, "*.txt", "*.a??");

        // TEST
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.a"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.ab"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.abc"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.ABC"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.aaa"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.Aaa"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.aAA"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.abcd"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));

    }

    @Test
    public void testAcceptStringIOCaseSensitive() throws FileSystemException {

        // PREPARE
        final WildcardFileFilter filter = new WildcardFileFilter(IOCase.SENSITIVE, "*.txt", "*.a??");

        // TEST
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.a"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.ab"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.abc"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.ABC"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.aaa"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.Aaa"))));
        Assert.assertTrue(filter.accept(createFileSelectInfo(new File("test.aAA"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.abcd"))));
        Assert.assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));

    }

}
// CHECKSTYLE:ON
