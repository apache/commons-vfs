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
package org.apache.commons.vfs.impl;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.DelegateFileObject;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A logical file system, made up of set of junctions, or links, to files from
 * other file systems.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/11/01 03:27:30 $
 */
public class VirtualFileSystem
    extends AbstractFileSystem
{
    private final Map junctions = new HashMap();

    public VirtualFileSystem( final FileName rootName )
    {
        super( rootName, null );
    }

    /**
     * Creates a file object.  This method is called only if the requested
     * file is not cached.
     */
    protected FileObject createFile( final FileName name )
        throws FileSystemException
    {
        FileObject realFile;

        // Check if junction point itself
        realFile = (FileObject)junctions.get( name );
        if ( realFile == null )
        {
            // Find longest matching junction
            FileName matchingName = null;
            FileObject matchingFile = null;
            for ( Iterator iterator = junctions.entrySet().iterator(); iterator.hasNext(); )
            {
                final Map.Entry entry = (Map.Entry)iterator.next();
                final FileName junctionPoint = (FileName)entry.getKey();
                if ( junctionPoint.isDescendent( name )
                     && ( matchingName == null || matchingName.isDescendent( junctionPoint ) ) )
                {
                    matchingName = junctionPoint;
                    matchingFile = (FileObject)entry.getValue();
                }
            }

            if ( matchingName != null )
            {
                // Lookup the name
                final String relName = matchingName.getRelativeName( name );
                realFile = matchingFile.resolveFile( relName, NameScope.DESCENDENT );
            }
        }

        return new DelegateFileObject( name, this, realFile );
    }

    /**
     * Adds a junction to this file system.
     */
    public void addJunction( final FileName junctionPoint,
                             final FileObject targetFile )
        throws FileSystemException
    {
        if ( !getRootName().getRootURI().equals( junctionPoint.getRootURI() ) )
        {
            throw new FileSystemException( "vfs.provider/mismatched-fs-for-name.error", new Object[] { junctionPoint, getRootName() } );
        }

        if ( junctions.containsKey( junctionPoint ) )
        {
            // TODO - something
        }
        junctions.put( junctionPoint, targetFile );

        // TODO - merge into ancestors of junction point
    }

    /**
     * Removes a junction from this file system.
     */
    public void removeJuntion( final FileName junctionPoint )
        throws FileSystemException
    {
        junctions.remove( junctionPoint );

        // TODO - remove from parents of junction point
    }
}
