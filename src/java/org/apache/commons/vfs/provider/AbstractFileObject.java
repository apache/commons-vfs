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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.Selectors;

/**
 * A partial file object implementation.   
 *
 * @todo Chop this class up - move all the protected methods to several
 *       interfaces, so that structure and content can be separately overridden.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.11 $ $Date: 2002/07/05 04:08:17 $
 */
public abstract class AbstractFileObject
    implements FileObject
{
    private static final FileObject[] EMPTY_FILE_ARRAY = {};

    private final FileName name;
    private final AbstractFileSystem fs;
    private DefaultFileContent content;

    // Cached info
    private boolean attached;
    private AbstractFileObject parent;
    private FileType type;
    private FileObject[] children;

    protected AbstractFileObject( final FileName name,
                                  final AbstractFileSystem fs )
    {
        this.name = name;
        this.fs = fs;
    }

    /**
     * Attaches this file object to its file resource.  This method is called
     * before any of the doBlah() or onBlah() methods.  Sub-classes can use
     * this method to perform lazy initialisation.
     */
    protected void doAttach() throws Exception
    {
    }

    /**
     * Detaches this file object from its file resource.
     *
     * <p>Called when this file is closed, or its type changes.  Note that
     * the file object may be reused later, so should be able to be reattached.
     */
    protected void doDetach() throws FileSystemException
    {
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.  The return value of this method is cached, so the
     * implementation can be expensive.
     */
    protected abstract FileType doGetType() throws Exception;

    /**
     * Determines if this file can be read.  Is only called if {@link #doGetType}
     * does not return null. This implementation always returns true.
     */
    protected boolean doIsReadable() throws Exception
    {
        return true;
    }

    /**
     * Determines if this file can be written to.  Is only called if
     * {@link #doGetType} does not return null.  This implementation always
     * returns true.
     */
    protected boolean doIsWriteable() throws Exception
    {
        return true;
    }

    /**
     * Lists the children of the file.  Is only called if {@link #doGetType}
     * returns {@link FileType#FOLDER}.  The return value of this method
     * is cached, so the implementation can be expensive.
     */
    protected abstract String[] doListChildren() throws Exception;

    /**
     * Deletes the file.  Is only called when:
     * <ul>
     * <li>{@link #isWriteable} returns true.
     * <li>{@link #doGetType} does not return null.
     * <li>This file has no children.
     * </ul>
     */
    protected void doDelete() throws Exception
    {
        throw new FileSystemException( "vfs.provider/delete-not-supported.error" );
    }

    /**
     * Creates this file as a folder.  Is only called when:
     * <ul>
     * <li>{@link #isWriteable} returns true.
     * <li>{@link #doGetType} returns null.
     * <li>The parent folder exists or this file is the root of the file
     *     system.
     * </ul>
     */
    protected void doCreateFolder() throws Exception
    {
        throw new FileSystemException( "vfs.provider/create-folder-not-supported.error" );
    }

    /**
     * Called when the children of this file change.
     */
    protected void onChildrenChanged()
    {
    }

    /**
     * Called from {@link DefaultFileContent#getLastModifiedTime}.
     * The default is to just throw an exception so filesystems must
     * override it to use it.
     */
    protected long doGetLastModifiedTime() throws Exception
    {
        throw new FileSystemException( "vfs.provider/get-last-modified-not-supported.error" );
    }

    /**
     * Called from {@link DefaultFileContent#setLastModifiedTime}.
     * The default is to just throw an exception so filesystems must
     * override it to use it.
     */
    protected void doSetLastModifiedTime( long modtime )
        throws Exception
    {
        throw new FileSystemException( "vfs.provider/set-last-modified-not-supported.error" );
    }

    /**
     * Called from {@link DefaultFileContent#getAttribute}.
     * The default implementation just returns null so filesystems must
     * override it to use it.
     */
    protected Object doGetAttribute( final String attrName )
        throws Exception
    {
        return null;
    }

    /**
     * Called from {@link DefaultFileContent#setAttribute}.
     * The default is to just throw an exception so filesystems must
     * override it to use it.
     */
    protected void doSetAttribute( String atttrName, Object value )
        throws Exception
    {
        throw new FileSystemException( "vfs.provider/set-attribute-not-supported.error" );
    }

    /**
     * Called from {@link DefaultFileContent#getCertificates}.
     * The default implementation just returns null so filesystems must
     * override it to use it.
     */
    protected Certificate[] doGetCertificates() throws FileSystemException
    {
        return null;
    }

    /**
     * Returns the size of the file content (in bytes).  Is only called if
     * {@link #doGetType} returns {@link FileType#FILE}.
     */
    protected abstract long doGetContentSize() throws Exception;

    /**
     * Creates an input stream to read the file content from.  Is only called
     * if  {@link #doGetType} returns {@link FileType#FILE}.
     *
     * <p>There is guaranteed never to be more than one stream for this file
     * (input or output) open at any given time.
     *
     * <p>The returned stream does not have to be buffered.
     */
    protected abstract InputStream doGetInputStream() throws Exception;

    /**
     * Creates an output stream to write the file content to.  Is only
     * called if:
     * <ul>
     * <li>This file is not read-only.
     * <li>{@link #doGetType} returns {@link FileType#FILE}, or
     * {@link #doGetType} returns null, and the file's parent exists
     * and is a folder.
     * </ul>
     *
     * <p>There is guaranteed never to be more than one stream for this file
     * (input or output) open at any given time.
     *
     * <p>The returned stream does not have to be buffered.
     */
    protected OutputStream doGetOutputStream() throws Exception
    {
        throw new FileSystemException( "vfs.provider/write-not-supported.error" );
    }

    /**
     * Notification of the output stream being closed.
     * TODO - get rid of this.
     */
    protected void doEndOutput() throws Exception
    {
    }

    /**
     * Notification of the input stream being closed.
     * TODO - get rid of this.
     */
    protected void doEndInput() throws Exception
    {
    }

    /**
     * Returns the URI of the file.
     */
    public String toString()
    {
        return name.getURI();
    }

    /**
     * Returns the name of the file.
     */
    public FileName getName()
    {
        return name;
    }

    /**
     * Returns the file system this file belongs to.
     */
    public FileSystem getFileSystem()
    {
        return fs;
    }

    /**
     * Returns a URL representation of the file.
     */
    public URL getURL() throws MalformedURLException
    {
        final StringBuffer buf = new StringBuffer();
        try
        {
            return (URL)AccessController.doPrivileged(
                new PrivilegedExceptionAction()
                {
                    public Object run() throws MalformedURLException
                    {
                        return new URL( UriParser.extractScheme( name.getURI(), buf ), null, -1,
                                        buf.toString(), new DefaultURLStreamHandler( fs.getContext() ) );
                    }
                } );
        }
        catch ( PrivilegedActionException e )
        {
            throw (MalformedURLException)e.getException();
        }
    }

    /**
     * Determines if the file exists.
     */
    public boolean exists() throws FileSystemException
    {
        attach();
        return ( type != null );
    }

    /**
     * Returns the file's type.
     */
    public FileType getType() throws FileSystemException
    {
        attach();
        if ( type == null )
        {
            throw new FileSystemException( "vfs.provider/get-type-no-exist.error", name );
        }
        return type;
    }

    /**
     * Determines if this file can be read.
     */
    public boolean isReadable() throws FileSystemException
    {
        try
        {
            attach();
            if ( exists() )
            {
                return doIsReadable();
            }
            else
            {
                return false;
            }
        }
        catch ( final Exception exc )
        {
            throw new FileSystemException( "vfs.provider/check-is-readable.error", name, exc );
        }
    }

    /**
     * Determines if this file can be written to.
     */
    public boolean isWriteable() throws FileSystemException
    {
        try
        {
            attach();
            if ( exists() )
            {
                return doIsWriteable();
            }
            else
            {
                final FileObject parent = getParent();
                if ( parent != null )
                {
                    return parent.isWriteable();
                }
                return true;
            }
        }
        catch ( final Exception exc )
        {
            throw new FileSystemException( "vfs.provider/check-is-writeable.error", name, exc );
        }
    }

    /**
     * Returns the parent of the file.
     */
    public FileObject getParent() throws FileSystemException
    {
        if ( this == fs.getRoot() )
        {
            if ( fs.getParentLayer() != null )
            {
                // Return the parent of the parent layer
                return fs.getParentLayer().getParent();
            }
            else
            {
                // Root file has no parent
                return null;
            }
        }

        // Locate the parent of this file
        if ( parent == null )
        {
            parent = (AbstractFileObject)fs.resolveFile( name.getParent() );
        }
        return parent;
    }

    /**
     * Returns the children of the file.
     */
    public FileObject[] getChildren() throws FileSystemException
    {
        attach();
        if ( type == null )
        {
            throw new FileSystemException( "vfs.provider/list-children-no-exist.error", name );
        }
        if ( type != FileType.FOLDER )
        {
            throw new FileSystemException( "vfs.provider/list-children-not-folder.error", name );
        }

        // Use cached info, if present
        if ( children != null )
        {
            return children;
        }

        // List the children
        String[] files;
        try
        {
            files = doListChildren();
        }
        catch ( RuntimeException re )
        {
            throw re;
        }
        catch ( Exception exc )
        {
            throw new FileSystemException( "vfs.provider/list-children.error", new Object[]{name}, exc );
        }

        if ( files == null || files.length == 0 )
        {
            // No children
            children = EMPTY_FILE_ARRAY;
        }
        else
        {
            // Create file objects for the children
            children = new FileObject[ files.length ];
            for ( int i = 0; i < files.length; i++ )
            {
                String file = files[ i ];
                children[ i ] = fs.resolveFile( name.resolveName( file, NameScope.CHILD ) );
            }
        }

        return children;
    }

    /**
     * Returns a child of this file.
     */
    public FileObject getChild( final String name ) throws FileSystemException
    {
        // TODO - use a hashtable when there are a large number of children
        getChildren();
        for ( int i = 0; i < children.length; i++ )
        {
            final FileObject child = children[ i ];
            // TODO - use a comparator to compare names
            if ( child.getName().getBaseName().equals( name ) )
            {
                return child;
            }
        }
        return null;
    }

    /**
     * Returns a child by name.
     */
    public FileObject resolveFile( final String name, final NameScope scope )
        throws FileSystemException
    {
        // TODO - cache children (only if they exist)
        return fs.resolveFile( this.name.resolveName( name, scope ) );
    }

    /**
     * Finds a file, relative to this file.
     *
     * @param path
     *          The path of the file to locate.  Can either be a relative
     *          path, which is resolved relative to this file, or an
     *          absolute path, which is resolved relative to the file system
     *          that contains this file.
     */
    public FileObject resolveFile( final String path ) throws FileSystemException
    {
        final FileName otherName = name.resolveName( path );
        return fs.resolveFile( otherName );
    }

    /**
     * Deletes this file, once all its children have been deleted
     */
    private void deleteSelf() throws FileSystemException
    {
        if ( !isWriteable() )
        {
            throw new FileSystemException( "vfs.provider/delete-read-only.error", name );
        }

        // Delete the file
        try
        {
            doDelete();
        }
        catch ( RuntimeException re )
        {
            throw re;
        }
        catch ( Exception exc )
        {
            throw new FileSystemException( "vfs.provider/delete.error", new Object[]{name}, exc );
        }

        // Update cached info
        handleDelete();
    }

    /**
     * Deletes this file.
     *
     * @todo This will not fail if this is a non-empty folder.
     */
    public void delete() throws FileSystemException
    {
        delete( Selectors.SELECT_SELF );
    }

    /**
     * Deletes this file, and all children.
     */
    public void delete( final FileSelector selector ) throws FileSystemException
    {
        attach();
        if ( type == null )
        {
            // File does not exist
            return;
        }

        // Locate all the files to delete
        ArrayList files = new ArrayList();
        findFiles( selector, true, files );

        // Delete 'em
        final int count = files.size();
        for ( int i = 0; i < count; i++ )
        {
            final AbstractFileObject file = (AbstractFileObject)files.get( i );
            file.attach();

            // If the file is a folder, make sure all its children have been deleted
            if ( file.type == FileType.FOLDER && file.getChildren().length != 0 )
            {
                // TODO - fail??
                // Skip
                continue;
            }

            // Delete the file
            file.deleteSelf();
        }
    }

    /**
     * Creates this file, if it does not exist.
     */
    public void createFile() throws FileSystemException
    {
        try
        {
            getOutputStream().close();
            endOutput();
        }
        catch ( final RuntimeException re )
        {
            throw re;
        }
        catch ( final Exception e )
        {
            throw new FileSystemException( "vfs.provider/create-file.error", name, e );
        }
    }

    /**
     * Creates this folder, if it does not exist.  Also creates any ancestor
     * files which do not exist.
     */
    public void createFolder() throws FileSystemException
    {
        attach();
        if ( this.type == FileType.FOLDER )
        {
            // Already exists as correct type
            return;
        }
        if ( this.type != null )
        {
            throw new FileSystemException( "vfs.provider/create-mismatched-type.error", new Object[]{type, name, this.type}, null );
        }
        if ( !isWriteable() )
        {
            throw new FileSystemException( "vfs.provider/create-read-only.error", new Object[]{type, name}, null );
        }

        // Traverse up the heirarchy and make sure everything is a folder
        FileObject parent = getParent();
        if ( parent != null )
        {
            parent.createFolder();
        }

        // Create the folder
        try
        {
            doCreateFolder();
        }
        catch ( final RuntimeException re )
        {
            throw re;
        }
        catch ( final Exception exc )
        {
            throw new FileSystemException( "vfs.provider/create-folder.error", name, exc );
        }

        // Update cached info
        handleCreate( FileType.FOLDER );
    }

    /**
     * Copies another file to this file.
     */
    public void copyFrom( final FileObject file, final FileSelector selector )
        throws FileSystemException
    {
        if ( !file.exists() )
        {
            throw new FileSystemException( "vfs.provider/copy-missing-file.error", file );
        }
        if ( !isWriteable() )
        {
            throw new FileSystemException( "vfs.provider/copy-read-only.error", new Object[]{file.getType(), file.getName(), this}, null );
        }

        // Locate the files to copy across
        final ArrayList files = new ArrayList();
        ( (AbstractFileObject)file ).findFiles( selector, false, files );

        // Copy everything across
        final int count = files.size();
        for ( int i = 0; i < count; i++ )
        {
            final FileObject srcFile = (FileObject)files.get( i );

            // Determine the destination file
            final String relPath = file.getName().getRelativeName( srcFile.getName() );
            final FileObject destFile = resolveFile( relPath, NameScope.DESCENDENT_OR_SELF );

            // Clean up the destination file, if necessary
            if ( destFile.exists() && destFile.getType() != srcFile.getType() )
            {
                // The destination file exists, and is not of the same type,
                // so delete it
                // TODO - add a pluggable policy for deleting and overwriting existing files
                destFile.delete( Selectors.SELECT_ALL );
            }

            // Copy across
            if ( srcFile.getType() == FileType.FILE )
            {
                copyContent( srcFile, destFile );
            }
            else
            {
                destFile.createFolder();
            }
        }
    }

    /**
     * Finds the set of matching descendents of this file, in depthwise
     * order.
     */
    public FileObject[] findFiles( final FileSelector selector ) throws FileSystemException
    {
        final ArrayList list = new ArrayList();
        findFiles( selector, true, list );
        return (FileObject[])list.toArray( new FileObject[ list.size() ] );
    }

    /**
     * Copies the content of another file to this file.
     */
    private static void copyContent( final FileObject srcFile,
                                     final FileObject destFile )
        throws FileSystemException
    {
        try
        {
            final InputStream instr = srcFile.getContent().getInputStream();
            try
            {
                // Create the output stream via getContent(), to pick up the
                // validation it does
                final OutputStream outstr = destFile.getContent().getOutputStream();
                try
                {
                    final byte[] buffer = new byte[ 1024 * 4 ];
                    int n = 0;
                    while ( -1 != ( n = instr.read( buffer ) ) )
                    {
                        outstr.write( buffer, 0, n );
                    }
                }
                finally
                {
                    outstr.close();
                }
            }
            finally
            {
                instr.close();
            }
        }
        catch ( RuntimeException re )
        {
            throw re;
        }
        catch ( final Exception exc )
        {
            throw new FileSystemException( "vfs.provider/copy-file.error", new Object[]{srcFile, destFile}, exc );
        }
    }

    /**
     * Returns true if this is a Folder.
     */
    boolean isFolder()
    {
        return ( type == FileType.FOLDER );
    }

    /**
     * Returns the file's content.
     */
    public FileContent getContent() throws FileSystemException
    {
        attach();
        if ( content == null )
        {
            content = new DefaultFileContent( this );
        }
        return content;
    }

    /**
     * Closes this file, and its content.
     */
    public void close() throws FileSystemException
    {
        FileSystemException exc = null;

        // Close the content
        if ( content != null )
        {
            try
            {
                content.close();
            }
            catch ( FileSystemException e )
            {
                exc = e;
            }
        }

        // Detach from the file
        detach();

        if ( exc != null )
        {
            throw exc;
        }
    }

    /**
     * Prepares this file for writing.  Makes sure it is either a file,
     * or its parent folder exists.  Returns an output stream to use to
     * write the content of the file to.
     */
    public OutputStream getOutputStream() throws FileSystemException
    {
        attach();
        if ( !isWriteable() )
        {
            throw new FileSystemException( "vfs.provider/write-read-only.error", name );
        }
        if ( type == FileType.FOLDER )
        {
            throw new FileSystemException( "vfs.provider/write-folder.error", name );
        }

        if ( type == null )
        {
            // Does not exist - make sure parent does
            FileObject parent = getParent();
            if ( parent != null )
            {
                parent.createFolder();
            }
        }

        // Get the raw output stream
        try
        {
            return doGetOutputStream();
        }
        catch ( RuntimeException re )
        {
            throw re;
        }
        catch ( Exception exc )
        {
            throw new FileSystemException( "vfs.provider/write.error", new Object[]{name}, exc );
        }
    }

    /**
     * Detaches this file, invaliating all cached info.  This will force
     * a call to {@link #doAttach} next time this file is used.
     */
    protected void detach() throws FileSystemException
    {
        if ( attached )
        {
            try
            {
                doDetach();
            }
            finally
            {
                attached = false;
                type = null;
                children = null;
            }
        }
    }

    /**
     * Attaches to the file.
     */
    private void attach() throws FileSystemException
    {
        if ( attached )
        {
            return;
        }

        try
        {
            // Attach and determine the file type
            doAttach();
            attached = true;
            type = doGetType();
        }
        catch ( RuntimeException re )
        {
            throw re;
        }
        catch ( Exception exc )
        {
            throw new FileSystemException( "vfs.provider/get-type.error", new Object[]{name}, exc );
        }
    }

    /**
     * Called when the ouput stream for this file is closed.
     */
    protected void endOutput() throws Exception
    {
        boolean newFile = ( type == null );

        doEndOutput();

        if ( newFile )
        {
            // File was created
            handleCreate( FileType.FILE );
        }
    }

    /**
     * Called when this file is created.  Updates cached info and notifies
     * the parent and file system.
     */
    protected void handleCreate( final FileType newType )
    {
        // Fix up state
        type = newType;
        children = EMPTY_FILE_ARRAY;

        // Notify parent that its child list may no longer be valid
        notifyParent();

        // Notify the file system
        fs.fireFileCreated( this );
    }

    /**
     * Called when this file is deleted.  Updates cached info and notifies
     * subclasses, parent and file system.
     */
    protected void handleDelete()
    {
        // Fix up state
        type = null;
        children = null;

        // Notify parent that its child list may no longer be valid
        notifyParent();

        // Notify the file system
        fs.fireFileDeleted( this );
    }

    /**
     * Notify the parent of a change to its children, when a child is created
     * or deleted.
     */
    private void notifyParent()
    {
        if ( parent == null )
        {
            // Locate the parent, if it is cached
            parent = (AbstractFileObject)fs.getFile( name.getParent() );
        }

        if ( parent != null )
        {
            parent.invalidateChildren();
        }
    }

    /**
     * Notifies a file that children have been created or deleted.
     * @todo Indicate whether the child was added or removed, and which child.
     */
    private void invalidateChildren()
    {
        children = null;
        onChildrenChanged();
    }

    /**
     * Traverses the descendents of this file, and builds a list of selected
     * files.
     */
    private void findFiles( final FileSelector selector,
                            final boolean depthwise,
                            final List selected ) throws FileSystemException
    {
        try
        {
            if ( exists() )
            {
                // Traverse starting at this file
                final DefaultFileSelectorInfo info = new DefaultFileSelectorInfo();
                info.setBaseFolder( this );
                info.setDepth( 0 );
                info.setFile( this );
                traverse( info, selector, depthwise, selected );
            }
        }
        catch ( final Exception e )
        {
            throw new FileSystemException( "vfs.provider/find-files.error", name, e );
        }
    }

    /**
     * Traverses a file.
     */
    private static void traverse( final DefaultFileSelectorInfo fileInfo,
                                  final FileSelector selector,
                                  final boolean depthwise,
                                  final List selected )
        throws Exception
    {
        // Check the file itself
        final FileObject file = fileInfo.getFile();
        final int index = selected.size();

        // If the file is a folder, traverse it
        if ( file.getType() == FileType.FOLDER && selector.traverseDescendents( fileInfo ) )
        {
            final int curDepth = fileInfo.getDepth();
            fileInfo.setDepth( curDepth + 1 );

            // Traverse the children
            final FileObject[] children = file.getChildren();
            for ( int i = 0; i < children.length; i++ )
            {
                final FileObject child = children[ i ];
                fileInfo.setFile( child );
                traverse( fileInfo, selector, depthwise, selected );
            }
            
            fileInfo.setFile( file );
            fileInfo.setDepth( curDepth );
        }

        // Add the file if doing depthwise traversal
        if ( selector.includeFile( fileInfo ) )
        {
            if ( depthwise )
            {
                // Add this file after its descendents
                selected.add( file );
            }
            else
            {
                // Add this file before its descendents
                selected.add( index, file );
            }
        }
    }

}
