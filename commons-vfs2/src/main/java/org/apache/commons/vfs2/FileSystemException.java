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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
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
     * Throws a FileSystemException when the given object is null.
     *
     * @param obj
     *            the object reference to check for null.
     * @param code
     *            message used when {@code
     *                FileSystemException} is thrown
     * @param <T>
     *            the type of the reference
     * @return {@code obj} if not {@code null}
     * @throws FileSystemException
     *             if {@code obj} is {@code null}
     * @since 2.3
     */
    public static <T> T requireNonNull(final T obj, final String code) throws FileSystemException {
        if (obj == null) {
            throw new FileSystemException(code);
        }
        return obj;
    }

    /**
     * Throws a FileSystemException when the given object is null.
     *
     * @param obj
     *            the object reference to check for null.
     * @param code
     *            message used when {@code
     *                FileSystemException} is thrown
     * @param info
     *            one context information.
     * @param <T>
     *            the type of the reference
     * @return {@code obj} if not {@code null}
     * @throws FileSystemException
     *             if {@code obj} is {@code null}
     * @since 2.3
     */
    public static <T> T requireNonNull(final T obj, final String code, final Object... info) throws FileSystemException {
        if (obj == null) {
            throw new FileSystemException(code, info);
        }
        return obj;
    }

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
     * @param info one context information.
     */
    public FileSystemException(final String code, final Object info) {
        this(code, null, new Object[] {info});
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
     * Constructs exception with the specified detail message and cause.
     *
     * @param code the error code of the message.
     * @param info one context information.
     * @param cause the cause.
     */
    public FileSystemException(final String code, final Object info, final Throwable cause) {
        this(code, cause, info);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code the error code of the message.
     * @param info array of complementary info (context).
     * @param cause the cause.
     * @deprecated Use instead {@link #FileSystemException(String, Throwable, Object[])}. Will be removed in 3.0.
     */
    @Deprecated
    public FileSystemException(final String code, final Object[] info, final Throwable cause) {
        this(code, cause, info);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code the error code of the message.
     * @param cause the original cause
     */
    public FileSystemException(final String code, final Throwable cause) {
        this(code, cause, (Object[]) null);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code the error code of the message.
     * @param info array of complementary info (context).
     * @param cause the cause.
     */
    public FileSystemException(final String code, final Throwable cause, final Object... info) {
        super(code, cause);

        if (info == null) {
            this.info = ArrayUtils.EMPTY_STRING_ARRAY;
        } else {
            this.info = new String[info.length];
            for (int i = 0; i < info.length; i++) {
                String value = String.valueOf(info[i]);
                // mask passwords (VFS-169)
                final Matcher urlMatcher = URL_PATTERN.matcher(value);
                if (urlMatcher.find()) {
                    value = PASSWORD_PATTERN.matcher(value).replaceFirst(":***@");
                }
                this.info[i] = value;
            }
        }
    }

    /**
     * Constructs wrapper exception.
     *
     * @param cause the root cause to wrap.
     */
    public FileSystemException(final Throwable cause) {
        this(cause.getMessage(), cause, (Object[]) null);
    }

    /**
     * Gets error code of the exception. Could be used as key for internationalization.
     *
     * @return the code.
     */
    public String getCode() {
        return super.getMessage();
    }

    /**
     * Gets array of complementary info (context). Could be used as parameter for internationalization.
     *
     * @return the context info.
     */
    public String[] getInfo() {
        return info;
    }

    /**
     * Gets message from bundle.
     *
     * @return The exception message.
     */
    @Override
    public String getMessage() {
        return Messages.getString(super.getMessage(), (Object[]) getInfo());
    }
}
