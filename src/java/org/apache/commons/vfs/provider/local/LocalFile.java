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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.DefaultFileContent;

/**
 * A file object implementation which uses direct file access.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2002/04/07 02:27:57 $
 */
final class LocalFile
    extends AbstractFileObject
    implements FileObject
{
    private File file;
    private final String fileName;
    private FilePermission requiredPerm;

    /**
     * Creates a non-root file.
     */
    public LocalFile( final LocalFileSystem fileSystem,
                      final String fileName,
                      final FileName name )
    {
        super( name, fileSystem );
        this.fileName = fileName;
    }

    /**
     * Attaches this file object to its file resource.
     */
    protected void doAttach()
        throws Exception
    {
        if ( file == null )
        {
            file = new File( fileName );
            requiredPerm = new FilePermission( file.getAbsolutePath(), "read" );
        }
    }

    /**
     * Returns the file's type.
     */
    protected FileType doGetType()
        throws Exception
    {
        if ( !file.exists() )
        {
            return null;
        }
        if ( file.isDirectory() )
        {
            return FileType.FOLDER;
        }
        if ( file.isFile() )
        {
            return FileType.FILE;
        }

        throw new FileSystemException( "vfs.provider.local/get-type.error", file );
    }

    /**
     * Returns the children of the file.
     */
    protected String[] doListChildren()
        throws Exception
    {
        return file.list();
    }

    /**
     * Deletes this file, and all children.
     */
    protected void doDelete()
        throws Exception
    {
        if ( !file.delete() )
        {
            throw new FileSystemException( "vfs.provider.local/delete-file.error", file );
        }
    }

    /**
     * Creates this folder.
     */
    protected void doCreateFolder()
        throws Exception
    {
        if ( !file.mkdir() )
        {
            throw new FileSystemException( "vfs.provider.local/create-folder.error", file );
        }
    }

    /**
     * Determines if this file can be written to.
     */
    protected boolean doIsWriteable() throws FileSystemException
    {
        return file.canWrite();
    }

    /**
     * Determines if this file can be read.
     */
    protected boolean doIsReadable() throws FileSystemException
    {
        return file.canRead();
    }

    /**
     * Gets the last modified time of this file.
     */
    protected long doGetLastModifiedTime() throws FileSystemException
    {
        return file.lastModified();
    }

    /**
     * Sets the last modified time of this file.
     */
    protected void doSetLastModifiedTime( final long modtime )
        throws FileSystemException
    {
        file.setLastModified( modtime );
    }

    /**
     * Creates an input stream to read the content from.
     */
    protected InputStream doGetInputStream()
        throws Exception
    {
        return new FileInputStream( file );
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream()
        throws Exception
    {
        return new FileOutputStream( file );
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize()
        throws Exception
    {
        return file.length();
    }

    /**
     * Creates a temporary local copy of this file, and its descendents.
     */
    protected File doReplicateFile( final FileSelector selector )
        throws FileSystemException
    {
        final SecurityManager sm = System.getSecurityManager();
        if ( sm != null )
        {
            sm.checkPermission( requiredPerm );
        }
        return file;
    }
}
