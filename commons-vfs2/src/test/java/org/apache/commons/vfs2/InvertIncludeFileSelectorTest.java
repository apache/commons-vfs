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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class InvertIncludeFileSelectorTest {

    @BeforeAll
    public static void setUpClass() throws Exception {
        PatternFileSelectorTest.setUpClass();
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        PatternFileSelectorTest.tearDownClass();
    }

    @Test
    public void testInvertMatchAll() throws Exception {
        final FileObject[] list = PatternFileSelectorTest.getBaseFolder()
                .findFiles(new InvertIncludeFileSelector(new PatternFileSelector(".*")));
        assertEquals(0, list.length);
    }

    @Test
    public void testInvertMatchSome() throws Exception {
        final FileObject[] list = PatternFileSelectorTest.getBaseFolder()
                .findFiles(new InvertIncludeFileSelector(new PatternFileSelector(".*\\.html")));
        assertEquals(7, list.length);
    }

}
