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
package org.apache.commons.vfs2.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.AccessMode;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link RandomAccessMode}.
 */
public class RandomAccessModeTest {

    @Test
    public void testFromAccessMode() {
        assertEquals(RandomAccessMode.READ, RandomAccessMode.from(AccessMode.READ));
        assertEquals(RandomAccessMode.READ, RandomAccessMode.from(AccessMode.READ, AccessMode.READ));
        assertEquals(RandomAccessMode.READ, RandomAccessMode.from(AccessMode.READ, AccessMode.READ, AccessMode.EXECUTE));
        assertEquals(RandomAccessMode.READWRITE, RandomAccessMode.from(AccessMode.WRITE));
        assertEquals(RandomAccessMode.READWRITE, RandomAccessMode.from(AccessMode.WRITE, AccessMode.WRITE));
        assertEquals(RandomAccessMode.READWRITE, RandomAccessMode.from(AccessMode.WRITE, AccessMode.READ));
        assertEquals(RandomAccessMode.READWRITE, RandomAccessMode.from(AccessMode.READ, AccessMode.WRITE));
        assertEquals(RandomAccessMode.READWRITE, RandomAccessMode.from(AccessMode.WRITE, AccessMode.WRITE, AccessMode.EXECUTE));
        assertThrows(IllegalArgumentException.class, () -> RandomAccessMode.from(AccessMode.EXECUTE));
    }

    @Test
    public void testGetModeStringRead() {
        assertEquals("r", RandomAccessMode.READ.getModeString());
    }

    @Test
    public void testGetModeStringReadWrite() {
        assertEquals("rw", RandomAccessMode.READWRITE.getModeString());
    }

    @Test
    public void testRead() {
        assertTrue(RandomAccessMode.READ.requestRead());
        assertFalse(RandomAccessMode.READ.requestWrite());
    }

    @Test
    public void testReadWrite() {
        assertTrue(RandomAccessMode.READWRITE.requestRead());
        assertTrue(RandomAccessMode.READWRITE.requestWrite());
    }

    @Test
    public void testToAccessModes() {
        assertArrayEquals(new AccessMode[] {AccessMode.READ}, RandomAccessMode.READ.toAccessModes());
        assertArrayEquals(new AccessMode[] {AccessMode.READ, AccessMode.WRITE}, RandomAccessMode.READWRITE.toAccessModes());
    }

}
