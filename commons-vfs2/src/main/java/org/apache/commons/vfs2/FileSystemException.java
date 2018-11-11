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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.util.Messages;

/**
 * Thrown for file system errors.
 */
public class FileSystemException extends IOException {
    /**
     * serialVersionUID format is YYYYMMDD for the date of the last binary change.
     */
    private static final long serialVersionUID = 20101208L;

    /** URL pattern */
    private static final Pattern URL_PATTERN = Pattern.compile("[a-z]+://.*");

    /** Password pattern */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(":(?:[^/]+)@");

    /**
     * Array of complementary info (context).
     */
    private final String[] info;

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code the error code of the message.
     */
    public FileSystemException(final String code) {
        this(code, null, (Object[]) null);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code the error code of the message.
     * @param info0 one context information.
     */
    public FileSystemException(final String code, final Object info0) {
        this(code, null, new Object[] { info0 });
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code the error code of the message.
     * @param info0 one context information.
     * @param throwable the cause.
     */
    public FileSystemException(final String code, final Object info0, final Throwable throwable) {
        this(code, throwable, new Object[] { info0 });
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code the error code of the message.
     * @param info array of complementary info (context).
     */
    public FileSystemException(final String code, final Object... info) {
        this(code, null, info);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code the error code of the message.
     * @param throwable the original cause
     */
    public FileSystemException(final String code, final Throwable throwable) {
        this(code, throwable, (Object[]) null);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code the error code of the message.
     * @param info array of complementary info (context).
     * @param throwable the cause.
     * @deprecated Use instead {@link #FileSystemException(String, Throwable, Object[])}. Will be removed in 3.0.
     */
    @Deprecated
    public FileSystemException(final String code, final Object[] info, final Throwable throwable) {
        this(code, throwable, info);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code the error code of the message.
     * @param info array of complementary info (context).
     * @param throwable the cause.
     */
    public FileSystemException(final String code, final Throwable throwable, final Object... info) {
        super(code, throwable);

        if (info == null) {
            this.info = new String[0];
        } else {
            this.info = new String[info.length];
            for (int i = 0; i < info.length; i++) {
                String value = String.valueOf(info[i]);
                // mask passwords (VFS-169)
                final Matcher urlMatcher = URL_PATTERN.matcher(value);
                if (urlMatcher.find()) {
                    final Matcher pwdMatcher = PASSWORD_PATTERN.matcher(value);
                    value = pwdMatcher.replaceFirst(":***@");
                }
                this.info[i] = value;
            }
        }
    }

    /**
     * Constructs wrapper exception.
     *
     * @param throwable the root cause to wrap.
     */
    public FileSystemException(final Throwable throwable) {
        this(throwable.getMessage(), throwable, (Object[]) null);
    }

    /**
     * Retrieves message from bundle.
     *
     * @return The exception message.
     */
    @Override
    public String getMessage() {
        return Messages.getString(super.getMessage(), (Object[]) getInfo());
    }

    /**
     * Retrieves error code of the exception. Could be used as key for internationalization.
     *
     * @return the code.
     */
    public String getCode() {
        return super.getMessage();
    }

    /**
     * Retrieves array of complementary info (context). Could be used as parameter for internationalization.
     *
     * @return the context info.
     */
    public String[] getInfo() {
        return info;
    }
}
