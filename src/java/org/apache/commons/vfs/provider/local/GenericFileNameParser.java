/*
* Copyright (C) The Apache Software Foundation. All rights reserved.
*
* This software is published under the terms of the Apache Software License
* version 1.1, a copy of which has been included  with this distribution in
* the LICENSE.txt file.
*/
package org.apache.commons.vfs.provider.local;

import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.commons.vfs.FileSystemException;

/**
 * A general-purpose file name parser.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/07/05 04:08:18 $
 */
final class GenericFileNameParser
    extends LocalFileNameParser
{
    private static final Resources REZ
        = ResourceManager.getPackageResources( GenericFileNameParser.class );

    /**
     * Pops the root prefix off a URI, which has had the scheme removed.
     */
    protected String extractRootPrefix( final String uri,
                                        final StringBuffer name )
        throws FileSystemException
    {
        // TODO - this class isn't generic at all.  Need to fix this

        // Looking for <sep>
        if ( name.length() == 0 || name.charAt( 0 ) != '/' )
        {
            final String message = REZ.getString( "not-absolute-file-name.error", uri );
            throw new FileSystemException( message );
        }

        return "/";
    }
}
