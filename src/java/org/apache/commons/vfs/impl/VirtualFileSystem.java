/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002, 2003 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.DelegateFileObject;

/**
 * A logical file system, made up of set of junctions, or links, to files from
 * other file systems.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.10 $ $Date: 2003/10/13 08:43:51 $
 *
 * @todo Handle nested junctions.
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
     * Adds the capabilities of this file system.
     */
    protected void addCapabilities( final Collection caps )
    {
        // TODO - this isn't really true
        caps.add( Capability.ATTRIBUTES );
        caps.add( Capability.CREATE );
        caps.add( Capability.DELETE );
        caps.add( Capability.GET_TYPE );
        caps.add( Capability.JUNCTIONS );
        caps.add( Capability.GET_LAST_MODIFIED );
        caps.add( Capability.SET_LAST_MODIFIED );
        caps.add( Capability.LIST_CHILDREN );
        caps.add( Capability.READ_CONTENT );
        caps.add( Capability.SIGNING );
        caps.add( Capability.WRITE_CONTENT );
    }

    /**
     * Creates a file object.  This method is called only if the requested
     * file is not cached.
     */
    protected FileObject createFile( final FileName name )
        throws Exception
    {
        // Find the file that the name points to
        final FileName junctionPoint = getJunctionForFile( name );
        final FileObject file;
        if ( junctionPoint != null )
        {
            // Resolve the real file
            final FileObject junctionFile = (FileObject)junctions.get( junctionPoint );
            final String relName = junctionPoint.getRelativeName( name );
            file = junctionFile.resolveFile( relName, NameScope.DESCENDENT_OR_SELF );
        }
        else
        {
            file = null;
        }

        // Return a wrapper around the file
        return new DelegateFileObject( name, this, file );
    }

    /**
     * Adds a junction to this file system.
     */
    public void addJunction( final String junctionPoint,
                             final FileObject targetFile )
        throws FileSystemException
    {
        final FileName junctionName = getRootName().resolveName( junctionPoint );

        // Check for nested junction - these are not supported yet
        if ( getJunctionForFile( junctionName ) != null )
        {
            throw new FileSystemException( "vfs.impl/nested-junction.error", junctionName );
        }

        try
        {
            // Add to junction table
            junctions.put( junctionName, targetFile );

            // Attach to file
            final DelegateFileObject junctionFile = (DelegateFileObject)getFile( junctionName );
            if ( junctionFile != null )
            {
                junctionFile.setFile( targetFile );
            }

            // Create ancestors of junction point
            FileName childName = junctionName;
            boolean done = false;
            for ( FileName parentName = childName.getParent();
                  !done && parentName != null;
                  childName = parentName, parentName = parentName.getParent() )
            {
                DelegateFileObject file = (DelegateFileObject)getFile( parentName );
                if ( file == null )
                {
                    file = new DelegateFileObject( parentName, this, null );
                    putFile( file );
                }
                else
                {
                    done = file.exists();
                }
                file.attachChild( childName.getBaseName() );
            }

            // TODO - attach all cached children of the junction point to their real file
        }
        catch ( final Exception e )
        {
            throw new FileSystemException( "vfs.impl/create-junction.error", junctionName );
        }
    }

    /**
     * Removes a junction from this file system.
     */
    public void removeJunction( final String junctionPoint )
        throws FileSystemException
    {
        final FileName junctionName = getRootName().resolveName( junctionPoint );
        junctions.remove( junctionName );

        // TODO - remove from parents of junction point
        // TODO - detach all cached children of the junction point from their real file
    }

    /**
     * Locates the junction point for the junction containing the given file.
     */
    private FileName getJunctionForFile( final FileName name )
    {
        if ( junctions.containsKey( name ) )
        {
            // The name points to the junction point directly
            return name;
        }

        // Find matching junction
        for ( Iterator iterator = junctions.keySet().iterator(); iterator.hasNext(); )
        {
            final FileName junctionPoint = (FileName)iterator.next();
            if ( junctionPoint.isDescendent( name ) )
            {
                return junctionPoint;
            }
        }

        // None
        return null;
    }
}
