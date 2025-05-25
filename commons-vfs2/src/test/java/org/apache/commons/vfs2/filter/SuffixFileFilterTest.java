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
 * Test for {@link SuffixFileFilter}.
 */
// CHECKSTYLE:OFF Test code
public class SuffixFileFilterTest extends BaseFilterTest {

    @Test
    public void testAcceptList() {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add(".txt");
        list.add(".bin");
        final SuffixFileFilter filter = new SuffixFileFilter(list);

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.bin"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test2.BIN"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptListIOCaseInsensitive() {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add(".txt");
        list.add(".bin");
        final SuffixFileFilter filter = new SuffixFileFilter(IOCase.INSENSITIVE, list);

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("TEST1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.bin"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.TXT"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptListIOCaseSensitive() {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add(".txt");
        list.add(".bin");
        final SuffixFileFilter filter = new SuffixFileFilter(IOCase.SENSITIVE, list);

        // TEST
        assertFalse(filter.accept(createFileSelectInfo(new File("test1.Txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test2.BIN"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptString() {

        // PREPARE
        final SuffixFileFilter filter = new SuffixFileFilter(".txt", ".xxx");

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test2.TXT"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptStringIOCaseInsensitive() {

        // PREPARE
        final SuffixFileFilter filter = new SuffixFileFilter(IOCase.INSENSITIVE, ".txt", ".xxx");

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.TXT"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptStringIOCaseSensitive() {

        // PREPARE
        final SuffixFileFilter filter = new SuffixFileFilter(IOCase.SENSITIVE, ".txt", ".xxx");

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test2.TXT"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

}
// CHECKSTYLE:ON
