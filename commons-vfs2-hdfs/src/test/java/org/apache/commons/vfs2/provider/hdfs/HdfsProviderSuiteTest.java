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
package org.apache.commons.vfs2.provider.hdfs;

import org.apache.commons.vfs2.provider.hdfs.HdfsFileProviderTestCase.HdfsProviderTestSuite;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;

/**
 * JUnit 5 test suite for HDFS provider (JUnit 5).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisabledForJreRange(min = JRE.JAVA_23)
@DisabledOnOs(OS.WINDOWS)
public class HdfsProviderSuiteTest extends HdfsProviderTestSuite {

    public HdfsProviderSuiteTest() throws Exception {
        super(new HdfsFileProviderTestCase(), "", false);
    }
}

