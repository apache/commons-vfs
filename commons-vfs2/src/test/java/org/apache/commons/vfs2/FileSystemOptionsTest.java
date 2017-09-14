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
package org.apache.commons.vfs2;

import org.junit.Assert;
import org.junit.Test;

/**
 * Check FileSystemOptions.
 *
 * @since 2.1
 */
public class FileSystemOptionsTest {

    @Test
    public void testEqualsHashCodeAndCompareTo() {
        final JUnitConfigBuilder builder = JUnitConfigBuilder.getInstance();
        final FileSystemOptions expected = new FileSystemOptions();
        builder.setId(expected, "Test");

        final FileSystemOptions actual = new FileSystemOptions();
        builder.setId(actual, "Test");

        Assert.assertEquals(expected, actual);
        Assert.assertEquals(0, actual.compareTo(expected));
        Assert.assertEquals(expected.hashCode(), actual.hashCode());

        builder.setNames(expected, new String[] { "A", "B", "C" });

        Assert.assertNotEquals(expected, actual);
        Assert.assertEquals(-1, actual.compareTo(expected));
        Assert.assertNotEquals(expected.hashCode(), actual.hashCode());

        builder.setNames(actual, new String[] { "A", "B", "C" });

        Assert.assertEquals(expected, actual);
        Assert.assertEquals(0, actual.compareTo(expected));
        Assert.assertEquals(expected.hashCode(), actual.hashCode());
    }

    public static class JUnitConfigBuilder extends FileSystemConfigBuilder {
        private static final JUnitConfigBuilder BUILDER = new JUnitConfigBuilder();

        public static JUnitConfigBuilder getInstance() {
            return BUILDER;
        }

        public void setId(final FileSystemOptions opts, final String id) {
            setParam(opts, "id", id);
        }

        public void setNames(final FileSystemOptions opts, final String[] names) {
            setParam(opts, "names", names);
        }

        @Override
        protected Class<? extends FileSystem> getConfigClass() {
            return JUnitFS.class;
        }

        private abstract static class JUnitFS implements FileSystem {
        }
    }

}
