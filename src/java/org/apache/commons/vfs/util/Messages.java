/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Formats messages.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class Messages
{
    /**
     * Map from message code to MessageFormat object for the message.
     */
    private static Map messages = new HashMap();
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
    public static String getString(final String code)
    {
        return getString(code, new Object[0]);
    }

    /**
     * Formats a message.
     *
     * @param code  The message code.
     * @param param The message parameter.
     * @return The formatted message.
     */
    public static String getString(final String code, final Object param)
    {
        return getString(code, new Object[]{param});
    }

    /**
     * Formats a message.
     *
     * @param code   The message code.
     * @param params The message parameters.
     * @return The formatted message.
     */
    public static String getString(final String code, final Object[] params)
    {
        try
        {
            if (code == null)
            {
                return null;
            }

            final MessageFormat msg = findMessage(code);
            return msg.format(params);
        }
        catch (final MissingResourceException mre)
        {
            return "Unknown message with code \"" + code + "\".";
        }
    }

    /**
     * Locates a message by its code.
     */
    private static synchronized MessageFormat findMessage(final String code)
        throws MissingResourceException
    {
        // Check if the message is cached
        MessageFormat msg = (MessageFormat) messages.get(code);
        if (msg != null)
        {
            return msg;
        }

        // Locate the message
        if (resources == null)
        {
            resources = ResourceBundle.getBundle("org.apache.commons.vfs.Resources");
        }
        final String msgText = resources.getString(code);
        msg = new MessageFormat(msgText);
        messages.put(code, msg);
        return msg;
    }
}
