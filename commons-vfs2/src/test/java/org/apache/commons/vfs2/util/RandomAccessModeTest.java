package org.apache.commons.vfs2.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests {@link RandomAccessMode}.
 */
public class RandomAccessModeTest {

    @Test
    public void test_getModeStringRead() {
        assertEquals("r", RandomAccessMode.READ.getModeString());
    }

    @Test
    public void test_getModeStringReadWrite() {
        assertEquals("rw", RandomAccessMode.READWRITE.getModeString());
    }

    @Test
    public void test_testRead() {
        assertTrue(RandomAccessMode.READ.requestRead());
        assertFalse(RandomAccessMode.READ.requestWrite());
    }

    @Test
    public void test_testReadWrite() {
        assertTrue(RandomAccessMode.READWRITE.requestRead());
        assertTrue(RandomAccessMode.READWRITE.requestWrite());
    }
}
