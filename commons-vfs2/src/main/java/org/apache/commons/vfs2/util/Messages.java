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
package org.apache.commons.vfs2.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Formats messages.
 */
public final class Messages {

    /**
     * Map from message code to MessageFormat object for the message.
     */
    private static final ConcurrentMap<String, MessageFormat> messageMap = new ConcurrentHashMap<>();
    private static final ResourceBundle RESOURCES = new CombinedResources("org.apache.commons.vfs2.Resources");

    /**
     * Locates a message by its code.
     */
    private static MessageFormat findMessage(final String code) throws MissingResourceException {
        // Check if the message is cached
        return messageMap.computeIfAbsent(code, k -> new MessageFormat(RESOURCES.getString(k)));
    }

    /**
     * Formats a message.
     *
     * @param code The message code.
     * @return The formatted message.
     */
    public static String getString(final String code) {
        return getString(code, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    /**
     * Formats a message.
     *
     * @param code The message code.
     * @param param The message parameter.
     * @return The formatted message.
     * @deprecated Will be removed in 3.0 in favor of {@link #getString(String, Object[])} When removed, calls to this
     *             method will automatically recompile to {@link #getString(String, Object[])}
     */
    @Deprecated
    public static String getString(final String code, final Object param) {
        return getString(code, new Object[] { param });
    }

    /**
     * Formats a message.
     *
     * @param code The message code.
     * @param params The message parameters.
     * @return The formatted message.
     */
    public static String getString(final String code, final Object... params) {
        try {
            if (code == null) {
                return null;
            }
            return findMessage(code).format(params);
        } catch (final MissingResourceException mre) {
            return "Unknown message with code \"" + code + "\".";
        }
    }

    private Messages() {
        // no instances.
    }
}
