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

import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.vfs.FileChangeEvent;
import org.apache.commons.vfs.FileListener;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;

/**
 * A file backed by another file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.8 $ $Date: 2003/06/28 10:58:51 $
 *
 * @todo Extract subclass that overlays the children
 */
public class DelegateFileObject
    extends AbstractFileObject
    implements FileListener
{
    private FileObject file;
    private final Set children = new HashSet();
    private boolean ignoreEvent;

    public DelegateFileObject( final FileName name,
                               final AbstractFileSystem fileSystem,
                               final FileObject file ) throws FileSystemException
    {
        super( name, fileSystem );
        this.file = file;
        if ( file != null )
        {
            file.getFileSystem().addListener( file, this );
        }
    }

    /** Adds a child to this file. */
    public void attachChild( final String baseName ) throws Exception
    {
        final FileType oldType = doGetType();
        if ( children.add( baseName ) )
        {
            childrenChanged();
        }
        maybeTypeChanged( oldType );
    }

    /** Attaches or detaches the target file. */
    public void setFile( final FileObject file ) throws Exception
    {
        final FileType oldType = doGetType();

        if ( file != null )
        {
            file.getFileSystem().addListener( file, this );
        }
        this.file = file;
        maybeTypeChanged( oldType );
    }

    /**
     * Checks whether the file's type has changed, and fires the appropriate
     * events.
     */
    private void maybeTypeChanged( final FileType oldType ) throws Exception
    {
        final FileType newType = doGetType();
        if ( oldType == FileType.IMAGINARY && newType != FileType.IMAGINARY )
        {
            handleCreate( newType );
        }
        else if ( oldType != FileType.IMAGINARY && newType == FileType.IMAGINARY )
        {
            handleDelete();
        }
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     */
    protected FileType doGetType() throws FileSystemException
    {
        if ( file != null )
        {
            return file.getType();
        }
        else if ( children.size() > 0 )
        {
            return FileType.FOLDER;
        }
        else
        {
            return FileType.IMAGINARY;
        }
    }

    /**
     * Determines if this file can be read.
     */
    protected boolean doIsReadable() throws FileSystemException
    {
        if ( file != null )
        {
            return file.isReadable();
        }
        else
        {
            return true;
        }
    }

    /**
     * Determines if this file can be written to.
     */
    protected boolean doIsWriteable() throws FileSystemException
    {
        if ( file != null )
        {
            return file.isWriteable();
        }
        else
        {
            return false;
        }
    }

    /**
     * Lists the children of the file.
     */
    protected String[] doListChildren() throws Exception
    {
        if ( file != null )
        {
            final FileObject[] children = file.getChildren();
            final String[] childNames = new String[ children.length ];
            for ( int i = 0; i < children.length; i++ )
            {
                childNames[ i ] = children[ i ].getName().getBaseName();
            }
            return childNames;
        }
        else
        {
            return (String[])children.toArray( new String[ children.size() ] );
        }
    }

    /**
     * Creates this file as a folder.
     */
    protected void doCreateFolder() throws Exception
    {
        ignoreEvent = true;
        try
        {
            file.createFolder();
        }
        finally
        {
            ignoreEvent = false;
        }
    }

    /**
     * Deletes the file.
     */
    protected void doDelete() throws Exception
    {
        ignoreEvent = true;
        try
        {
            file.delete();
        }
        finally
        {
            ignoreEvent = false;
        }
    }

    /**
     * Returns the size of the file content (in bytes).  Is only called if
     * {@link #doGetType} returns {@link FileType#FILE}.
     */
    protected long doGetContentSize() throws Exception
    {
        return file.getContent().getSize();
    }

    /**
     * Returns the attributes of this file.
     */
    protected Map doGetAttributes()
        throws Exception
    {
        return file.getContent().getAttributes();
    }

    /**
     * Sets an attribute of this file.
     */
    protected void doSetAttribute( final String atttrName,
                                   final Object value )
        throws Exception
    {
        file.getContent().setAttribute( atttrName, value );
    }

    /**
     * Returns the certificates of this file.
     */
    protected Certificate[] doGetCertificates() throws Exception
    {
        return file.getContent().getCertificates();
    }

    /**
     * Returns the last-modified time of this file.
     */
    protected long doGetLastModifiedTime() throws Exception
    {
        return file.getContent().getLastModifiedTime();
    }

    /**
     * Sets the last-modified time of this file.
     */
    protected void doSetLastModifiedTime( final long modtime )
        throws Exception
    {
        file.getContent().setLastModifiedTime( modtime );
    }

    /**
     * Creates an input stream to read the file content from.
     */
    protected InputStream doGetInputStream() throws Exception
    {
        return file.getContent().getInputStream();
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream() throws Exception
    {
        return file.getContent().getOutputStream();
    }

    /**
     * Called when a file is created.
     */
    public void fileCreated( final FileChangeEvent event ) throws Exception
    {
        if ( !ignoreEvent )
        {
            handleCreate( file.getType() );
        }
    }

    /**
     * Called when a file is deleted.
     */
    public void fileDeleted( final FileChangeEvent event ) throws Exception
    {
        if ( !ignoreEvent )
        {
            handleDelete();
        }
    }
}
