/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.util;

/**
 * Formats messages.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/10/22 11:41:49 $
 */
public class Messages
{
    private Messages()
    {
    }

    /**
     * Formats a message.
     *
     * @param code The message code.
     * @return The formatted message.
     */
    public static String getString( final String code )
    {
        return getString( code, new Object[ 0 ] );
    }

    /**
     * Formats a message.
     *
     * @param code The message code.
     * @param param The message parameter.
     * @return The formatted message.
     */
    public static String getString( final String code, final Object param )
    {
        return getString( code, new Object[] { param } );
    }

    /**
     * Formats a message.
     *
     * @param code The message code.
     * @param params The message parameters.
     * @return The formatted message.
     */
    public static String getString( final String code, final Object[] params )
    {
        StringBuffer sb = new StringBuffer( code );
        sb.append( '{' );
        if ( params != null )
        {
            for ( int i = 0; i < params.length; i++ )
            {
                sb.append( params[ i ] );
                sb.append( ',' );
            }
        }
        sb.append( '}' );
        return sb.toString();
    }
}
