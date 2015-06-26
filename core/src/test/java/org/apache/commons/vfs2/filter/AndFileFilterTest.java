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
import org.junit.Assert;
import org.junit.Test;

// CHECKSTYLE:OFF Test code
public class AndFileFilterTest extends BaseFilterTest {

    @Test
    public void testAndFileFilterFileFilter() {

        // PREPARE
        FileFilter filter1 = new DummyFilter();
        FileFilter filter2 = new DummyFilter();
        FileFilter filter3 = new DummyFilter();

        // TEST
        AndFileFilter testee = new AndFileFilter(filter1, filter2, filter3);

        // VERIFY
        assertContainsOnly(testee.getFileFilters(), filter1, filter2, filter3);

    }

    @Test
    public void testAndFileFilterList() {

        // PREPARE
        FileFilter filter1 = new DummyFilter();
        FileFilter filter2 = new DummyFilter();
        FileFilter filter3 = new DummyFilter();
        List<FileFilter> list = new ArrayList<FileFilter>();
        list.add(filter1);
        list.add(filter2);
        list.add(filter3);

        // TEST
        AndFileFilter testee = new AndFileFilter(list);

        // VERIFY
        assertContainsOnly(testee.getFileFilters(), filter1, filter2, filter3);

    }

    @Test
    public void testAddFileFilter() {

        // PREPARE
        FileFilter filter1 = new DummyFilter();
        FileFilter filter2 = new DummyFilter();
        FileFilter filter3 = new DummyFilter();

        // TEST
        AndFileFilter testee = new AndFileFilter();
        testee.addFileFilter(filter1);
        testee.addFileFilter(filter2);
        testee.addFileFilter(filter3);

        // VERIFY
        assertContainsOnly(testee.getFileFilters(), filter1, filter2, filter3);

    }

    @Test
    public void testRemoveFileFilter() {

        // PREPARE
        FileFilter filter1 = new DummyFilter();
        FileFilter filter2 = new DummyFilter();
        FileFilter filter3 = new DummyFilter();
        AndFileFilter testee = new AndFileFilter(filter1, filter2, filter3);

        // TEST
        testee.removeFileFilter(filter2);

        // VERIFY
        assertContainsOnly(testee.getFileFilters(), filter1, filter3);

    }

    @Test
    public void testSetFileFilters() {

        // PREPARE
        FileFilter filter1 = new DummyFilter();
        FileFilter filter2 = new DummyFilter();
        FileFilter filter3 = new DummyFilter();
        List<FileFilter> list = new ArrayList<FileFilter>();
        list.add(filter1);
        list.add(filter2);
        list.add(filter3);
        AndFileFilter testee = new AndFileFilter();

        // TEST
        testee.setFileFilters(list);

        // VERIFY
        assertContainsOnly(testee.getFileFilters(), filter1, filter2, filter3);

    }

    @Test
    public void testAccept() {

        FileSelectInfo any = createFSI(new File("anyfile"));

        // Empty
        Assert.assertFalse(new AndFileFilter().accept(any));

        // True
        Assert.assertTrue(new AndFileFilter(new True()).accept(any));
        Assert.assertTrue(new AndFileFilter(new True(), new True()).accept(any));

        // False
        Assert.assertFalse(new AndFileFilter(new False()).accept(any));
        Assert.assertFalse(new AndFileFilter(new False(), new False())
                .accept(any));
        Assert.assertFalse(new AndFileFilter(new False(), new True())
                .accept(any));
        Assert.assertFalse(new AndFileFilter(new True(), new False())
                .accept(any));

    }

    /**
     * Just a filter class.
     */
    private static class DummyFilter implements FileFilter {

        @Override
        public boolean accept(FileSelectInfo fileInfo) {
            return false;
        }

    }

    /**
     * Always TRUE.
     */
    private static class True implements FileFilter {

        @Override
        public boolean accept(FileSelectInfo fileInfo) {
            return true;
        }

    }

    /**
     * Always FALSE.
     */
    private static class False implements FileFilter {

        @Override
        public boolean accept(FileSelectInfo fileInfo) {
            return false;
        }

    }

}
// CHECKSTYLE:ON
