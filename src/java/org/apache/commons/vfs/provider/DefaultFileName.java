/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.NameScope;

/**
 * A default file name implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.4 $ $Date: 2002/07/05 04:08:17 $
 */
public final class DefaultFileName
    implements FileName
{
    private final UriParser parser;
    private final String rootPrefix;
    private final String absPath;

    // Cached stuff
    private String uri;
    private String baseName;

    public DefaultFileName( final UriParser parser,
                            final String rootPrefix,
                            final String absPath )
    {
        this.parser = parser;
        this.rootPrefix = rootPrefix;
        this.absPath = absPath;
    }

    /**
     * Returns the hashcode for this name.
     */
    public int hashCode()
    {
        return ( rootPrefix.hashCode() ^ absPath.hashCode() );
    }

    /**
     * Determines if this object is equal to another.
     */
    public boolean equals( final Object obj )
    {
        final DefaultFileName name = (DefaultFileName)obj;
        return ( rootPrefix.equals( name.rootPrefix ) && absPath.equals( absPath ) );
    }

    /**
     * Returns the URI of the file.
     */
    public String toString()
    {
        return getURI();
    }

    /**
     * Returns the base name of the file.
     */
    public String getBaseName()
    {
        if ( baseName == null )
        {
            baseName = parser.getBaseName( absPath );
        }
        return baseName;
    }

    /**
     * Returns the absolute path of the file, relative to the root of the
     * file system that the file belongs to.
     */
    public String getPath()
    {
        return absPath;
    }

    /**
     * Returns the name of a child of the file.
     */
    public FileName resolveName( final String name,
                                 final NameScope scope )
        throws FileSystemException
    {
        final String otherAbsPath = parser.resolvePath( absPath, name, scope );
        return new DefaultFileName( parser, rootPrefix, otherAbsPath );
    }

    /**
     * Returns the name of the parent of the file.
     */
    public FileName getParent()
    {
        final String parentPath = parser.getParentPath( absPath );
        if ( parentPath == null )
        {
            return null;
        }
        return new DefaultFileName( parser, rootPrefix, parentPath );
    }

    /**
     * Resolves a name, relative to the file.  If the supplied name is an
     * absolute path, then it is resolved relative to the root of the
     * file system that the file belongs to.  If a relative name is supplied,
     * then it is resolved relative to this file name.
     */
    public FileName resolveName( final String path ) throws FileSystemException
    {
        return resolveName( path, NameScope.FILE_SYSTEM );
    }

    /**
     * Returns the absolute URI of the file.
     */
    public String getURI()
    {
        if ( uri == null )
        {
            uri = parser.getUri( rootPrefix, absPath );
        }
        return uri;
    }

    /**
     * Converts a file name to a relative name, relative to this file name.
     */
    public String getRelativeName( final FileName name ) throws FileSystemException
    {
        return parser.makeRelative( absPath, name.getPath() );
    }
}
