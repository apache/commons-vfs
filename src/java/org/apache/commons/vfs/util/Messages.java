/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.util;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Map;
import java.util.HashMap;
import java.text.MessageFormat;

/**
 * Formats messages.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/10/22 13:01:57 $
 */
public class Messages
{
    /** Map from message code to MessageFormat object for the message. */
    private static final Map messages = new HashMap();
    private static ResourceBundle resources;

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
        try
        {
            final MessageFormat msg = findMessage( code );
            return msg.format( params );
        }
        catch ( final MissingResourceException mre )
        {
            return "Unknown message with code \"" + code + "\".";
        }
    }

    /**
     * Locates a message by its code.
     */
    private static synchronized MessageFormat findMessage( final String code )
        throws MissingResourceException
    {
        // Check if the message is cached
        MessageFormat msg = (MessageFormat)messages.get( code );
        if ( msg != null )
        {
            return msg;
        }

        // Locate the message
        if ( resources == null )
        {
            resources = ResourceBundle.getBundle( "org.apache.commons.vfs.Resources" );
        }
        final String msgText = resources.getString( code );
        msg = new MessageFormat( msgText );
        messages.put( code, msg );
        return msg;
    }
}
