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
import org.apache.commons.vfs.provider.UriParser;

/**
 * A name parser.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public abstract class LocalFileNameParser
{
    /**
     * Determines if a name is an absolute file name.
     */
    public boolean isAbsoluteName(final String name)
    {
        // TODO - this is yucky
        StringBuffer b = new StringBuffer(name);
        try
        {
            UriParser.fixSeparators(b);
            extractRootPrefix(name, b);
            return true;
        }
        catch (FileSystemException e)
        {
            return false;
        }
    }

    /**
     * Pops the root prefix off a URI, which has had the scheme removed.
     */
    protected abstract String extractRootPrefix(final String uri,
                                                final StringBuffer name)
        throws FileSystemException;

}
