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
package org.apache.commons.vfs2.provider.ftp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link FtpFileSystemConfigBuilder}.
 */
public class FtpFileSystemConfigBuilderTest {

    @Test
    public void testControlKeepAliveReplyTimeout() {
        final FtpFileSystemConfigBuilder instance = FtpFileSystemConfigBuilder.getInstance();
        final FileSystemOptions options = new FileSystemOptions();
        instance.setControlKeepAliveReplyTimeout(options, Duration.ofSeconds(10));
        assertEquals(Duration.ofSeconds(10), instance.getControlKeepAliveReplyTimeout(options));
    }

    @Test
    public void testControlKeepAliveTimeout() {
        final FtpFileSystemConfigBuilder instance = FtpFileSystemConfigBuilder.getInstance();
        final FileSystemOptions options = new FileSystemOptions();
        instance.setControlKeepAliveTimeout(options, Duration.ofSeconds(10));
        assertEquals(Duration.ofSeconds(10), instance.getControlKeepAliveTimeout(options));
    }

    @Test
    public void testActivePortRange() {
        final FtpFileSystemConfigBuilder instance = FtpFileSystemConfigBuilder.getInstance();
        final FileSystemOptions options = new FileSystemOptions();
        instance.setActivePortRange(options, FtpActivePortRange.of(2121, 2125));
        assertEquals(FtpActivePortRange.of(2121, 2125), instance.getActivePortRange(options));
    }
}
