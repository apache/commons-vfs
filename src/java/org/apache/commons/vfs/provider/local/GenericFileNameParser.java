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
package org.apache.commons.vfs.provider.local;

import org.apache.commons.vfs.FileSystemException;

/**
 * A general-purpose file name parser.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class GenericFileNameParser
    extends LocalFileNameParser
{
    /**
     * Pops the root prefix off a URI, which has had the scheme removed.
     */
    protected String extractRootPrefix(final String uri,
                                       final StringBuffer name)
        throws FileSystemException
    {
        // TODO - this class isn't generic at all.  Need to fix this

        // Looking for <sep>
        if (name.length() == 0 || name.charAt(0) != '/')
        {
            throw new FileSystemException("vfs.provider.local/not-absolute-file-name.error", uri);
        }

        return "/";
    }
}
