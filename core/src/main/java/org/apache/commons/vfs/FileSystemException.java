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
package org.apache.commons.vfs;

import org.apache.commons.vfs.util.Messages;

import java.io.IOException;

/**
 * Thrown for file system errors.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class FileSystemException
    extends IOException
{
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
     * @param info array of complementary info (context).
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
                this.info[i] = String.valueOf(info[i]);
            }
        }
        this.code = code;
        this.throwable = throwable;
    }

    /**
     * retrieve message from bundle
     */
    public String getMessage()
	{
    	return Messages.getString(super.getMessage(), getInfo());
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
     * Retrieve root cause of the exception.
     *
     * @return the root cause
     */
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
