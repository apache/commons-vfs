/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link NameFileFilter}.
 */
// CHECKSTYLE:OFF Test code
public class NameFileFilterTest extends BaseFilterTest {

    @Test
    public void testAcceptList() {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add("test1.txt");
        list.add("test2.txt");
        final NameFileFilter filter = new NameFileFilter(list);

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("Test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptListIOCaseInsensitive() {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add("test1.txt");
        list.add("test2.txt");
        final NameFileFilter filter = new NameFileFilter(IOCase.INSENSITIVE, list);

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("TEST1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("Test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptListIOCaseSensitive() {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add("test1.txt");
        list.add("test2.txt");
        final NameFileFilter filter = new NameFileFilter(IOCase.SENSITIVE, list);

        // TEST
        assertFalse(filter.accept(createFileSelectInfo(new File("TEST1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("Test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptString() {

        // PREPARE
        final NameFileFilter filter = new NameFileFilter("test1.txt");

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("Test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptStringIOCaseInsensitive() {

        // PREPARE
        final NameFileFilter filter = new NameFileFilter(IOCase.INSENSITIVE, "test2.txt");

        // TEST
        assertFalse(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("Test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptStringIOCaseSensitive() {

        // PREPARE
        final NameFileFilter filter = new NameFileFilter(IOCase.SENSITIVE, "test2.txt");

        // TEST
        assertFalse(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("Test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

}
// CHECKSTYLE:ON
