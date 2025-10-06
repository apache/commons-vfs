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
package org.apache.commons.vfs2.provider.tar;

import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.junit.jupiter.api.TestInstance;

/**
 * Tests for nested TBZ2 file systems (JUnit 5).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NestedTbz2Test extends ProviderTestSuiteJunit5 {

    public NestedTbz2Test() throws Exception {
        super(new NestedTbz2TestCase(), "", true);
    }
}

