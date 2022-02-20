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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Check FileSystemOptions.
 *
 * @since 2.1
 */
public class FileSystemOptionsTest {

    public static class JUnitConfigBuilder extends FileSystemConfigBuilder {
        private abstract static class JUnitFS implements FileSystem {
        }

        private static final JUnitConfigBuilder BUILDER = new JUnitConfigBuilder();

        public static JUnitConfigBuilder getInstance() {
            return BUILDER;
        }

        @Override
        protected Class<? extends FileSystem> getConfigClass() {
            return JUnitFS.class;
        }

        public void setId(final FileSystemOptions opts, final String id) {
            setParam(opts, "id", id);
        }

        public void setNames(final FileSystemOptions opts, final String[] names) {
            setParam(opts, "names", names);
        }
    }

    @Test
    public void testClone() {
        final FileSystemOptions fileSystemOptions = new FileSystemOptions();
        assertEquals(fileSystemOptions.getClass(), fileSystemOptions.clone().getClass());
        assertEquals(0, ((FileSystemOptions) fileSystemOptions.clone()).size());
        fileSystemOptions.setOption(FileSystem.class, "key1", "value1");
        assertEquals(1, ((FileSystemOptions) fileSystemOptions.clone()).size());
        final FileSystemOptions clone = (FileSystemOptions) fileSystemOptions.clone();
        assertEquals("value1", clone.getOption(FileSystem.class, "key1"));
        fileSystemOptions.setOption(FileSystem.class, "key2", "value2");
        assertNull(clone.getOption(FileSystem.class, "key2"));
    }

    @Test
    public void testEqualsHashCodeAndCompareTo() {
        final JUnitConfigBuilder builder = JUnitConfigBuilder.getInstance();
        final FileSystemOptions expected = new FileSystemOptions();
        builder.setId(expected, "Test");

        final FileSystemOptions actual = new FileSystemOptions();
        builder.setId(actual, "Test");

        assertEquals(expected, actual);
        assertEquals(0, actual.compareTo(expected));
        assertEquals(expected.hashCode(), actual.hashCode());

        builder.setNames(expected, new String[] {"A", "B", "C"});

        assertNotEquals(expected, actual);
        assertEquals(-1, actual.compareTo(expected));
        assertNotEquals(expected.hashCode(), actual.hashCode());

        builder.setNames(actual, new String[] {"A", "B", "C"});

        assertEquals(expected, actual);
        assertEquals(0, actual.compareTo(expected));
        assertEquals(expected.hashCode(), actual.hashCode());
    }

}
