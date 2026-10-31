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
package org.apache.commons.vfs2.provider.sftp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.impl.SimpleLog;
import org.junit.jupiter.api.Test;

import com.jcraft.jsch.Logger;

/**
 * Tests {@link SftpClientFactory}.
 */
public class SftpClientFactoryTest {

    private static Logger newJSchLogger(final int simpleLogLevel) {
        final SimpleLog log = new SimpleLog(SftpClientFactoryTest.class.getName());
        log.setLevel(simpleLogLevel);
        return new SftpClientFactory.JSchLogger(log);
    }

    @Test
    public void testIsEnabledAtDebugLevel() {
        final Logger jschLogger = newJSchLogger(SimpleLog.LOG_LEVEL_DEBUG);
        assertTrue(jschLogger.isEnabled(Logger.WARN));
        assertTrue(jschLogger.isEnabled(Logger.INFO));
        assertTrue(jschLogger.isEnabled(Logger.DEBUG));
    }

    @Test
    public void testIsEnabledAtErrorLevel() {
        final Logger jschLogger = newJSchLogger(SimpleLog.LOG_LEVEL_ERROR);
        assertTrue(jschLogger.isEnabled(Logger.FATAL));
        assertTrue(jschLogger.isEnabled(Logger.ERROR));
        assertFalse(jschLogger.isEnabled(Logger.WARN));
        assertFalse(jschLogger.isEnabled(Logger.INFO));
        assertFalse(jschLogger.isEnabled(Logger.DEBUG));
    }

    /**
     * Tests VFS-864: JSch asks {@link Logger#isEnabled(int)} before it calls {@link Logger#log(int, String)}, so a JSch level must be enabled exactly when
     * the Commons Logging level that {@code log} writes to is enabled, otherwise the message is silently dropped.
     */
    @Test
    public void testIsEnabledAtWarnLevel() {
        final Logger jschLogger = newJSchLogger(SimpleLog.LOG_LEVEL_WARN);
        assertTrue(jschLogger.isEnabled(Logger.FATAL));
        assertTrue(jschLogger.isEnabled(Logger.ERROR));
        assertTrue(jschLogger.isEnabled(Logger.WARN));
        assertFalse(jschLogger.isEnabled(Logger.INFO));
        assertFalse(jschLogger.isEnabled(Logger.DEBUG));
    }
}
