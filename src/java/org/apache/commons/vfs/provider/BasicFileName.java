/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileName;

import java.net.URL;

/**
 * A simple file name, made up of a root URI and an absolute path.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class BasicFileName
    extends AbstractFileName
{
    private final String rootUri;

    public BasicFileName(final String rootUri, final String path)
    {
        this(UriParser.extractScheme(rootUri), rootUri, path, false);
    }

    public BasicFileName(final String scheme,
                         final String rootUri,
                         final String path)
    {
        this(scheme, rootUri, path, false);
    }

    private BasicFileName(final String scheme,
                          final String rootUri,
                          final String path,
                          final boolean normalised)
    {
        super(scheme, path);
        if (!normalised && rootUri.endsWith(SEPARATOR))
        {
            // Remove trailing separator
            this.rootUri = rootUri.substring(0, rootUri.length() - 1);
        }
        else
        {
            this.rootUri = rootUri;
        }
    }

    public BasicFileName(final FileName rootUri, final String path)
    {
        this(rootUri.getScheme(), rootUri.getURI(), path, false);
    }

    public BasicFileName(final URL rootUrl, final String path)
    {
        this(rootUrl.getProtocol(), rootUrl.toExternalForm(), path, false);
    }

    /**
     * Factory method for creating name instances.
     */
    protected FileName createName(final String path)
    {
        return new BasicFileName(getScheme(), rootUri, path, true);
    }

    /**
     * Builds the root URI for this file name.
     */
    protected void appendRootUri(final StringBuffer buffer)
    {
        buffer.append(rootUri);
    }
}
