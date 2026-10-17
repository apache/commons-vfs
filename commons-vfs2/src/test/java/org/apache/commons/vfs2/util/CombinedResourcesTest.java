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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link CombinedResources}.
 */
class CombinedResourcesTest {

    private static final String RESOURCE_NAME = "org.apache.commons.vfs2.Resources";

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Test
    void testContainsKey() {
        final CombinedResources resources = new CombinedResources(RESOURCE_NAME);
        assertTimeoutPreemptively(TIMEOUT, () -> {
            assertTrue(resources.containsKey("vfs.provider/closed.error"));
            assertFalse(resources.containsKey("does.not.exist"));
        });
    }

    @Test
    void testGetKeysEnumeratesEachKeyOnce() {
        final CombinedResources resources = new CombinedResources(RESOURCE_NAME);
        assertTimeoutPreemptively(TIMEOUT, () -> {
            final Set<String> keys = new HashSet<>();
            final Enumeration<String> enumeration = resources.getKeys();
            int count = 0;
            while (enumeration.hasMoreElements()) {
                assertTrue(keys.add(enumeration.nextElement()), "duplicate key");
                count++;
            }
            assertTrue(count > 1);
            assertTrue(keys.contains("vfs.provider/closed.error"));
            assertEquals(resources.keySet(), keys);
        });
    }

}
