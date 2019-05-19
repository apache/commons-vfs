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

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Assert;
import org.junit.Test;

// CHECKSTYLE:OFF Test code
public class OrFileFilterTest extends BaseFilterTest {

    @Test
    public void testOrFileFilterFileFilter() {

        // PREPARE
        final FileFilter filter1 = new DummyFilter();
        final FileFilter filter2 = new DummyFilter();
        final FileFilter filter3 = new DummyFilter();

        // TEST
        final OrFileFilter testee = new OrFileFilter(filter1, filter2, filter3);

        // VERIFY
        assertContainsOnly(testee.getFileFilters(), filter1, filter2, filter3);

    }

    @Test
    public void testOrFileFilterList() {

        // PREPARE
        final FileFilter filter1 = new DummyFilter();
        final FileFilter filter2 = new DummyFilter();
        final FileFilter filter3 = new DummyFilter();
        final List<FileFilter> list = new ArrayList<>();
        list.add(filter1);
        list.add(filter2);
        list.add(filter3);

        // TEST
        final OrFileFilter testee = new OrFileFilter(list);

        // VERIFY
        assertContainsOnly(testee.getFileFilters(), filter1, filter2, filter3);

    }

    @Test
    public void testAddFileFilter() {

        // PREPARE
        final FileFilter filter1 = new DummyFilter();
        final FileFilter filter2 = new DummyFilter();
        final FileFilter filter3 = new DummyFilter();

        // TEST
        final OrFileFilter testee = new OrFileFilter();
        testee.addFileFilter(filter1);
        testee.addFileFilter(filter2);
        testee.addFileFilter(filter3);

        // VERIFY
        assertContainsOnly(testee.getFileFilters(), filter1, filter2, filter3);

    }

    @Test
    public void testRemoveFileFilter() {

        // PREPARE
        final FileFilter filter1 = new DummyFilter();
        final FileFilter filter2 = new DummyFilter();
        final FileFilter filter3 = new DummyFilter();
        final OrFileFilter testee = new OrFileFilter(filter1, filter2, filter3);

        // TEST
        testee.removeFileFilter(filter2);

        // VERIFY
        assertContainsOnly(testee.getFileFilters(), filter1, filter3);

    }

    @Test
    public void testSetFileFilters() {

        // PREPARE
        final FileFilter filter1 = new DummyFilter();
        final FileFilter filter2 = new DummyFilter();
        final FileFilter filter3 = new DummyFilter();
        final List<FileFilter> list = new ArrayList<>();
        list.add(filter1);
        list.add(filter2);
        list.add(filter3);
        final OrFileFilter testee = new OrFileFilter();

        // TEST
        testee.setFileFilters(list);

        // VERIFY
        assertContainsOnly(testee.getFileFilters(), filter1, filter2, filter3);

    }

    @Test
    public void testAccept() throws FileSystemException {

        final FileSelectInfo any = createFileSelectInfo(new File("anyfile"));

        // Empty
        Assert.assertFalse(new OrFileFilter().accept(any));

        // True
        Assert.assertTrue(new OrFileFilter(new True()).accept(any));
        Assert.assertTrue(new OrFileFilter(new True(), new True()).accept(any));
        Assert.assertTrue(new OrFileFilter(new False(), new True()).accept(any));
        Assert.assertTrue(new OrFileFilter(new True(), new False()).accept(any));

        // False
        Assert.assertFalse(new OrFileFilter(new False()).accept(any));
        Assert.assertFalse(new OrFileFilter(new False(), new False()).accept(any));

    }

    /**
     * Just a filter class.
     */
    private static class DummyFilter implements FileFilter {

        @Override
        public boolean accept(final FileSelectInfo fileInfo) {
            return false;
        }

    }

    /**
     * Always TRUE.
     */
    private static class True implements FileFilter {

        @Override
        public boolean accept(final FileSelectInfo fileInfo) {
            return true;
        }

    }

    /**
     * Always FALSE.
     */
    private static class False implements FileFilter {

        @Override
        public boolean accept(final FileSelectInfo fileInfo) {
            return false;
        }

    }

}
// CHECKSTYLE:ON
