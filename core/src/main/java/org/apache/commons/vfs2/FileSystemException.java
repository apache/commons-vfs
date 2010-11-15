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

import org.apache.commons.vfs2.util.Messages;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thrown for file system errors.
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @version $Revision$ $Date$
 */
public class FileSystemException
    extends IOException
{
    /** URL pattern */
    private static final Pattern URL_PATTERN = Pattern.compile("[a-z]+://.*");

    /** Password pattern */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(":(?:[^/]+)@");

    /**
     * The Throwable that caused this exception to be thrown.
     */
    private final Throwable throwable;

    /**
     * The message code.
     */
    private final String code;

    /**
     * array of complementary info (context).
     */
    private final String[] info;

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code the error code of the message.
     */
    public FileSystemException(final String code)
    {
        this(code, null, null);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code  the error code of the message.
     * @param info0 one context information.
     */
    public FileSystemException(final String code, final Object info0)
    {
        this(code, new Object[]{info0}, null);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code      the error code of the message.
     * @param info0     one context information.
     * @param throwable the cause.
     */
    public FileSystemException(final String code,
                               final Object info0,
                               final Throwable throwable)
    {
        this(code, new Object[]{info0}, throwable);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code the error code of the message.
     * @param info array of complementary info (context).
     */
    public FileSystemException(final String code, final Object[] info)
    {
        this(code, info, null);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code the error code of the message.
     * @param throwable the original cause
     */
    public FileSystemException(final String code, final Throwable throwable)
    {
        this(code, null, throwable);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param code      the error code of the message.
     * @param info      array of complementary info (context).
     * @param throwable the cause.
     */
    public FileSystemException(final String code,
                               final Object[] info,
                               final Throwable throwable)
    {
        super(code);

        if (info == null)
        {
            this.info = new String[0];
        }
        else
        {
            this.info = new String[info.length];
            for (int i = 0; i < info.length; i++)
            {
                String value = String.valueOf(info[i]);
                // mask passwords (VFS-169)
                final Matcher urlMatcher = URL_PATTERN.matcher(value);
                if (urlMatcher.find())
                {
                    final Matcher pwdMatcher = PASSWORD_PATTERN.matcher(value);
                    value = pwdMatcher.replaceFirst(":***@");
                }
                this.info[i] = value;
            }
        }
        this.code = code;
        this.throwable = throwable;
    }

    /**
     * Constructs wrapper exception.
     *
     * @param throwable the root cause to wrap.
     */
    public FileSystemException(final Throwable throwable)
    {
        this(throwable.getMessage(), null, throwable);
    }

    /**
     * retrieve message from bundle.
     * @return The exception message.
     */
    @Override
    public String getMessage()
    {
        return Messages.getString(super.getMessage(), getInfo());
    }

    /**
     * Retrieve root cause of the exception.
     *
     * @return the root cause
     */
    @Override
    public final Throwable getCause()
    {
        return throwable;
    }

    /**
     * Retrieve error code of the exception.
     * Could be used as key for internationalization.
     *
     * @return the code.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Retrieve array of complementary info (context).
     * Could be used as parameter for internationalization.
     *
     * @return the context info.
     */
    public String[] getInfo()
    {
        return info;
    }
}
