/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs;

/**
 * Thrown for file system errors.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.5 $ $Date: 2002/07/05 04:08:17 $
 */
public final class FileSystemException
    extends Exception
{
    /**
     * The Throwable that caused this exception to be thrown.
     */
    private final Throwable throwable;

    /**
     * array of complementary info (context).
     */
    private final String[] info;

    /**
     * Constructs exception with the specified detail message.
     *
     * @param   code   the error code of the message.
     */
    public FileSystemException( final String code )
    {
        this( code, null , null);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param   code   the error code of the message.
     * @param   info0  one context information.
     */
    public FileSystemException( final String code, final Object info0 )
    {
        this( code, new Object[]{info0} , null);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param   code   the error code of the message.
     * @param   info0  one context information.
     * @param   throwable the cause.
     */
    public FileSystemException( final String code,
                                final Object info0,
                                final Throwable throwable)
    {
        this( code, new Object[]{info0}, throwable );
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param   code   the error code of the message.
     * @param   info   array of complementary info (context).
     */
    public FileSystemException( final String code, final Object[] info )
    {
        this( code, info , null);
    }

    /**
     * Constructs exception with the specified detail message.
     *
     * @param   code   the error code of the message.
     * @param   info   array of complementary info (context).
     * @param   throwable the cause.
     */
    public FileSystemException( final String code,
                                final Object[] info,
                                final Throwable throwable )
    {
        super( code );
        if (info == null)
        {
            this.info = new String[0];
        }
        else
        {
            this.info = new String[info.length];
            for (int i = 0; i<info.length; i++)
            {
                this.info[i] = String.valueOf(info[i]);
            }
        }
        this.throwable = throwable;
    }

    /**
     * Constructs wrapper exception.
     *
     * @param throwable the root cause to wrap.
     */
    public FileSystemException( final Throwable throwable )
    {
        this(throwable.getMessage(),  null, throwable);
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
        return super.getMessage();
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

    /**
     * Returns the message for this exception.
     * @todo Look up message in resources.
     */
    public String getMessage()
    {
        StringBuffer sb = new StringBuffer( getCode() );
        sb.append( '{' );
        for ( int i = 0; i < info.length; i++ )
        {
            sb.append( info[ i ] ).append( ',' );
        }
        sb.append( throwable );
        sb.append( '}' );
        return sb.toString();
    }

}
