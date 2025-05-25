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
package org.apache.commons.vfs2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import org.apache.commons.vfs2.provider.sftp.IdentityInfo;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.junit.jupiter.api.Test;

/**
 * Check FileSystemOptions.
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

    private static void assertSftpOptionsEquals(final File privKey, final File pubKey, final byte[] passphrase) {
        final SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();

        final FileSystemOptions expected = new FileSystemOptions();
        final IdentityInfo info1 = new IdentityInfo(privKey, pubKey, passphrase);
        builder.setIdentityProvider(expected, info1);

        final FileSystemOptions actual = new FileSystemOptions();
        final IdentityInfo info2 = new IdentityInfo(privKey, pubKey, passphrase);
        builder.setIdentityProvider(actual, info2);

        assertEquals(0, expected.compareTo(actual));
        assertEquals(expected.hashCode(), actual.hashCode());
    }

    private static void assertSftpOptionsNotEquals(final File privKey1, final File pubKey1, final byte[] passphrase1,
        final File privKey2, final File pubKey2, final byte[] passphrase2) {
        final SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();

        final FileSystemOptions expected = new FileSystemOptions();
        final IdentityInfo info1 = new IdentityInfo(privKey1, pubKey1, passphrase1);
        builder.setIdentityProvider(expected, info1);

        final FileSystemOptions actual = new FileSystemOptions();
        final IdentityInfo info2 = new IdentityInfo(privKey2, pubKey2, passphrase2);
        builder.setIdentityProvider(actual, info2);

        assertNotEquals(0, expected.compareTo(actual));
        assertNotEquals(expected.hashCode(), actual.hashCode());
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

    @Test
    public void testEqualsHashCodeAndCompareToWithSftpIdentityProviderMatch() {
        for (int mask = 0; mask < 8; mask++) {
            assertSftpOptionsEquals(
                (mask & 1) == 1 ? new File("/tmp/test.priv") : null,
                (mask & 2) == 2 ? new File("/tmp/test.pub") : null,
                (mask & 4) == 4 ? new byte[] {1, 2, 3} : null
            );
        }
    }

    @Test
    public void testEqualsHashCodeAndCompareToWithSftpIdentityProviderMismatch() {
        final String pubKey1 = "/tmp/test.pub";
        final String pubKey2 = "/tmp/test1.pub";

        final String privKey1 = "/tmp/test.priv";
        final String privKey2 = "/tmp/test1.priv";

        assertSftpOptionsNotEquals(
            new File(privKey1), new File(pubKey1), new byte[] {1, 2, 3},
            new File(privKey2), new File(pubKey1), new byte[] {1, 2, 3}
        );

        assertSftpOptionsNotEquals(
            new File(privKey1), new File(pubKey1), new byte[] {1, 2, 3},
            new File(privKey1), new File(pubKey2), new byte[] {1, 2, 3}
        );

        assertSftpOptionsNotEquals(
            new File(privKey1), new File(pubKey1), new byte[] {1, 2, 3},
            new File(privKey1), new File(pubKey1), new byte[] {1, 2, 4}
        );
    }
}
