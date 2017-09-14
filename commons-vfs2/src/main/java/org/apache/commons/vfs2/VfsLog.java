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
package org.apache.commons.vfs2;

import org.apache.commons.logging.Log;

/**
 * This class is to keep the old logging behaviour (for ant-task) and to be able to correctly use commons-logging.<br>
 * I hope i could remove it sometimes.
 */
public final class VfsLog {
    // static utility class
    private VfsLog() {
    }

    /**
     * warning.
     *
     * @param vfslog The base component Logger to use.
     * @param commonslog The class specific Logger
     * @param message The message to log.
     * @param t The exception, if any.
     */
    public static void warn(final Log vfslog, final Log commonslog, final String message, final Throwable t) {
        if (vfslog != null) {
            vfslog.warn(message, t);
        } else if (commonslog != null) {
            commonslog.warn(message, t);
        }
    }

    /**
     * warning.
     *
     * @param vfslog The base component Logger to use.
     * @param commonslog The class specific Logger
     * @param message The message to log.
     */
    public static void warn(final Log vfslog, final Log commonslog, final String message) {
        if (vfslog != null) {
            vfslog.warn(message);
        } else if (commonslog != null) {
            commonslog.warn(message);
        }
    }

    /**
     * debug.
     *
     * @param vfslog The base component Logger to use.
     * @param commonslog The class specific Logger
     * @param message The message to log.
     */
    public static void debug(final Log vfslog, final Log commonslog, final String message) {
        if (vfslog != null) {
            vfslog.debug(message);
        } else if (commonslog != null) {
            commonslog.debug(message);
        }
    }

    /**
     * debug.
     *
     * @param vfslog The base component Logger to use.
     * @param commonslog The class specific Logger
     * @param message The message to log.
     * @param t The exception, if any.
     */
    public static void debug(final Log vfslog, final Log commonslog, final String message, final Throwable t) {
        if (vfslog != null) {
            vfslog.debug(message, t);
        } else if (commonslog != null) {
            commonslog.debug(message, t);
        }
    }

    /**
     * info.
     *
     * @param vfslog The base component Logger to use.
     * @param commonslog The class specific Logger
     * @param message The message to log.
     * @param t The exception, if any.
     */
    public static void info(final Log vfslog, final Log commonslog, final String message, final Throwable t) {
        if (vfslog != null) {
            vfslog.info(message, t);
        } else if (commonslog != null) {
            commonslog.info(message, t);
        }
    }

    /**
     * info.
     *
     * @param vfslog The base component Logger to use.
     * @param commonslog The class specific Logger
     * @param message The message to log.
     */
    public static void info(final Log vfslog, final Log commonslog, final String message) {
        if (vfslog != null) {
            vfslog.info(message);
        } else if (commonslog != null) {
            commonslog.info(message);
        }
    }

    /**
     * error.
     *
     * @param vfslog The base component Logger to use.
     * @param commonslog The class specific Logger
     * @param message The message to log.
     * @param t The exception, if any.
     */
    public static void error(final Log vfslog, final Log commonslog, final String message, final Throwable t) {
        if (vfslog != null) {
            vfslog.error(message, t);
        } else if (commonslog != null) {
            commonslog.error(message, t);
        }
    }

    /**
     * error.
     *
     * @param vfslog The base component Logger to use.
     * @param commonslog The class specific Logger
     * @param message The message to log.
     */
    public static void error(final Log vfslog, final Log commonslog, final String message) {
        if (vfslog != null) {
            vfslog.error(message);
        } else if (commonslog != null) {
            commonslog.error(message);
        }
    }

    /**
     * fatal.
     *
     * @param vfslog The base component Logger to use.
     * @param commonslog The class specific Logger
     * @param message The message to log.
     * @param t The exception, if any.
     */
    public static void fatal(final Log vfslog, final Log commonslog, final String message, final Throwable t) {
        if (vfslog != null) {
            vfslog.fatal(message, t);
        } else if (commonslog != null) {
            commonslog.fatal(message, t);
        }
    }

    /**
     * fatal.
     *
     * @param vfslog The base component Logger to use.
     * @param commonslog The class specific Logger
     * @param message The message to log.
     */
    public static void fatal(final Log vfslog, final Log commonslog, final String message) {
        if (vfslog != null) {
            vfslog.fatal(message);
        } else if (commonslog != null) {
            commonslog.fatal(message);
        }
    }
}
