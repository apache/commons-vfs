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
 * Test for {@link WildcardFileFilter}.
 */
// CHECKSTYLE:OFF Test code
public class WildcardFileFilterTest extends BaseFilterTest {

    @Test
    public void testAcceptList() {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add("*.txt");
        list.add("*.a??");
        final WildcardFileFilter filter = new WildcardFileFilter(list);

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.a"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.ab"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.abc"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.ABC"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.aaa"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.Aaa"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.aAA"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.abcd"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptListIOCaseInsensitive() {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add("*.txt");
        list.add("*.a??");
        final WildcardFileFilter filter = new WildcardFileFilter(IOCase.INSENSITIVE, list);

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.a"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.ab"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.abc"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.ABC"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.aaa"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.Aaa"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.aAA"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.abcd"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptListIOCaseSensitive() {

        // PREPARE
        final List<String> list = new ArrayList<>();
        list.add("*.txt");
        list.add("*.a??");
        final WildcardFileFilter filter = new WildcardFileFilter(IOCase.SENSITIVE, list);

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.a"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.ab"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.abc"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.ABC"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.aaa"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.Aaa"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.aAA"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.abcd"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptString() {

        // PREPARE
        final WildcardFileFilter filter = new WildcardFileFilter("*.txt", "*.a??");

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.a"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.ab"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.abc"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.ABC"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.aaa"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.Aaa"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.aAA"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.abcd"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptStringIOCaseInsensitive() {

        // PREPARE
        final WildcardFileFilter filter = new WildcardFileFilter(IOCase.INSENSITIVE, "*.txt", "*.a??");

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.a"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.ab"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.abc"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.ABC"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.aaa"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.Aaa"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.aAA"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.abcd"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

    @Test
    public void testAcceptStringIOCaseSensitive() {

        // PREPARE
        final WildcardFileFilter filter = new WildcardFileFilter(IOCase.SENSITIVE, "*.txt", "*.a??");

        // TEST
        assertTrue(filter.accept(createFileSelectInfo(new File("test1.txt"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test2.txt"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.a"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.ab"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.abc"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.ABC"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.aaa"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.Aaa"))));
        assertTrue(filter.accept(createFileSelectInfo(new File("test.aAA"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.abcd"))));
        assertFalse(filter.accept(createFileSelectInfo(new File("test.xxx"))));
    }

}
// CHECKSTYLE:ON
