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
package org.apache.commons.vfs.provider.local;

import java.io.File;
import java.io.FilePermission;
import java.util.Collection;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * A local file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.4 $ $Date: 2002/07/05 04:08:18 $
 */
public final class LocalFileSystem
    extends AbstractFileSystem
    implements FileSystem
{
    private final String rootFile;

    public LocalFileSystem( final FileName rootName,
                            final String rootFile )
    {
        super( rootName, null );
        this.rootFile = rootFile;
    }

    /**
     * Creates a file object.
     */
    protected FileObject createFile( final FileName name )
        throws FileSystemException
    {
        // Create the file
        final String fileName = rootFile + name.getPath();
        return new LocalFile( this, fileName, name );
    }

    /**
     * Returns the capabilities of this file system.
     */
    protected void addCapabilities( final Collection caps )
    {
        caps.add( Capability.CREATE );
        caps.add( Capability.DELETE );
        caps.add( Capability.LAST_MODIFIED );
        caps.add( Capability.LIST_CHILDREN );
        caps.add( Capability.READ_CONTENT );
        caps.add( Capability.URI );
        caps.add( Capability.WRITE_CONTENT );
    }

    /**
     * Creates a temporary local copy of a file and its descendents.
     */
    protected File doReplicateFile( final FileObject fileObject,
                                    final FileSelector selector )
        throws Exception
    {
        final LocalFile localFile = (LocalFile)fileObject;
        final File file = localFile.getLocalFile();
        final SecurityManager sm = System.getSecurityManager();
        if ( sm != null )
        {
            final FilePermission requiredPerm = new FilePermission( file.getAbsolutePath(), "read" );
            sm.checkPermission( requiredPerm );
        }
        return file;
    }

}
