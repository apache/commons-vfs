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
import java.io.IOException;
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
import org.apache.commons.vfs.FileUtil;

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
    private FileType type;
    private AbstractFileObject parent;
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
     *
     * This implementation does nothing.
     */
    protected void doAttach() throws Exception
    {
    }

    /**
     * Detaches this file object from its file resource.
     *
     * <p>Called when this file is closed.  Note that the file object may be
     * reused later, so should be able to be reattached.
     *
     * This implementation does nothing.
     */
    protected void doDetach() throws Exception
    {
    }

    /**
     * Determines the type of this file.  Must not return null.  The return
     * value of this method is cached, so the implementation can be expensive.
     */
    protected abstract FileType doGetType() throws Exception;

    /**
     * Determines if this file can be read.  Is only called if {@link #doGetType}
     * does not return null.
     *
     * This implementation always returns true.
     */
    protected boolean doIsReadable() throws Exception
    {
        return true;
    }

    /**
     * Determines if this file can be written to.  Is only called if
     * {@link #doGetType} does not return null.
     *
     * This implementation always returns true.
     */
    protected boolean doIsWriteable() throws Exception
    {
        return true;
    }

    /**
     * Lists the children of this file.  Is only called if {@link #doGetType}
     * returns {@link FileType#FOLDER}.  The return value of this method
     * is cached, so the implementation can be expensive.
     */
    protected abstract String[] doListChildren() throws Exception;

    /**
     * Deletes the file.  Is only called when:
     * <ul>
     * <li>{@link #doGetType} does not return null.
     * <li>{@link #doIsWriteable} returns true.
     * <li>This file has no children, if a folder.
     * </ul>
     *
     * This implementation throws an exception.
     */
    protected void doDelete() throws Exception
    {
        throw new FileSystemException( "vfs.provider/delete-not-supported.error" );
    }

    /**
     * Creates this file as a folder.  Is only called when:
     * <ul>
     * <li>{@link #doGetType} returns null.
     * <li>The parent folder exists and is writeable, or this file is the
     *     root of the file system.
     * </ul>
     *
     * This implementation throws an exception.
     */
    protected void doCreateFolder() throws Exception
    {
        throw new FileSystemException( "vfs.provider/create-folder-not-supported.error" );
    }

    /**
     * Called when the children of this file change.  Allows subclasses to
     * refresh any cached information about the children of this file.
     *
     * This implementation does nothing.
     */
    protected void onChildrenChanged() throws Exception
    {
    }

    /**
     * Called when the type or content of this file changes.
     *
     * This implementation does nothing.
     */
    protected void onChange() throws Exception
    {
    }

    /**
     * Returns the last modified time of this file.  Is only called if
     * {@link #doGetType} does not return null.
     *
     * This implementation throws an exception.
     */
    protected long doGetLastModifiedTime() throws Exception
    {
        throw new FileSystemException( "vfs.provider/get-last-modified-not-supported.error" );
    }

    /**
     * Sets the last modified time of this file.  Is only called if
     * {@link #doGetType} does not return null.
     *
     * This implementation throws an exception.
     */
    protected void doSetLastModifiedTime( final long modtime )
        throws Exception
    {
        throw new FileSystemException( "vfs.provider/set-last-modified-not-supported.error" );
    }

    /**
     * Gets an attribute of this file.  Is only called if {@link #doGetType}
     * does not return null.
     *
     * This implementation always returns null.
     */
    protected Object doGetAttribute( final String attrName )
        throws Exception
    {
        return null;
    }

    /**
     * Sets an attribute of this file.  Is only called if {@link #doGetType}
     * does not return null.
     *
     * This implementation throws an exception.
     */
    protected void doSetAttribute( final String atttrName, final Object value )
        throws Exception
    {
        throw new FileSystemException( "vfs.provider/set-attribute-not-supported.error" );
    }

    /**
     * Returns the certificates used to sign this file.  Is only called if
     * {@link #doGetType} does not return null.
     *
     * This implementation always returns null.
     */
    protected Certificate[] doGetCertificates() throws Exception
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
     * if {@link #doGetType} returns {@link FileType#FILE}.
     *
     * <p>It is guaranteed that there are no open output streams for this file
     * when this method is called.
     *
     * <p>The returned stream does not have to be buffered.
     */
    protected abstract InputStream doGetInputStream() throws Exception;

    /**
     * Creates an output stream to write the file content to.  Is only
     * called if:
     * <ul>
     * <li>{@link #doIsWriteable} returns true.
     * <li>{@link #doGetType} returns {@link FileType#FILE}, or
     * {@link #doGetType} returns null, and the file's parent exists
     * and is a folder.
     * </ul>
     *
     * <p>It is guaranteed that there are no open stream (input or output) for
     * this file when this method is called.
     *
     * <p>The returned stream does not have to be buffered.
     *
     * This implementation throws an exception.
     */
    protected OutputStream doGetOutputStream() throws Exception
    {
        throw new FileSystemException( "vfs.provider/write-not-supported.error" );
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
    public URL getURL() throws FileSystemException
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
        catch ( final PrivilegedActionException e )
        {
            throw new FileSystemException( "vfs.provider/get-url.error", name, e.getException() );
        }
    }

    /**
     * Determines if the file exists.
     */
    public boolean exists() throws FileSystemException
    {
        attach();
        return ( type != FileType.IMAGINARY );
    }

    /**
     * Returns the file's type.
     */
    public FileType getType() throws FileSystemException
    {
        attach();
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
        if ( !type.hasChildren() )
        {
            throw new FileSystemException( "vfs.provider/list-children-not-folder.error", name );
        }

        // Use cached info, if present
        if ( children != null )
        {
            return children;
        }

        // List the children
        final String[] files;
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
                final String file = files[ i ];
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

        try
        {
            // Delete the file
            doDelete();

            // Update cached info
            handleDelete();
        }
        catch ( final RuntimeException re )
        {
            throw re;
        }
        catch ( final Exception exc )
        {
            throw new FileSystemException( "vfs.provider/delete.error", new Object[]{name}, exc );
        }
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
        if ( type == FileType.IMAGINARY )
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
        if ( type == FileType.FOLDER )
        {
            // Already exists as correct type
            return;
        }
        if ( type != FileType.IMAGINARY )
        {
            throw new FileSystemException( "vfs.provider/create-folder-mismatched-type.error", name );
        }
        if ( !isWriteable() )
        {
            throw new FileSystemException( "vfs.provider/create-folder-read-only.error", name );
        }

        // Traverse up the heirarchy and make sure everything is a folder
        final FileObject parent = getParent();
        if ( parent != null )
        {
            parent.createFolder();
        }

        try
        {
            // Create the folder
            doCreateFolder();

            // Update cached info
            handleCreate( FileType.FOLDER );
        }
        catch ( final RuntimeException re )
        {
            throw re;
        }
        catch ( final Exception exc )
        {
            throw new FileSystemException( "vfs.provider/create-folder.error", name, exc );
        }
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
            try
            {
                if ( srcFile.getType().hasContent() )
                {
                    FileUtil.copyContent( srcFile, destFile );
                }
                else if ( srcFile.getType().hasChildren() )
                {
                    destFile.createFolder();
                }
            }
            catch ( final IOException e )
            {
                throw new FileSystemException( "vfs.provider/copy-file.error", new Object[]{srcFile, destFile}, e );
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
        try
        {
            detach();
        }
        catch ( final Exception e )
        {
            exc = new FileSystemException( "vfs.provider/close.error", name, e );
        }

        if ( exc != null )
        {
            throw exc;
        }
    }

    /**
     * Returns an input stream to use to read the content of the file.
     */
    public InputStream getInputStream() throws FileSystemException
    {
        attach();
        if ( ! type.hasContent() )
        {
            throw new FileSystemException( "vfs.provider/read-not-file.error", name );
        }
        if ( !isReadable() )
        {
            throw new FileSystemException( "vfs.provider/read-not-readable.error", name );
        }

        // Get the raw input stream
        try
        {
            return doGetInputStream();
        }
        catch ( final Exception exc )
        {
            throw new FileSystemException( "vfs.provider/read.error", name, exc );
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
        if ( type != FileType.IMAGINARY && !type.hasContent() )
        {
            throw new FileSystemException( "vfs.provider/write-not-file.error", name );
        }
        if ( !isWriteable() )
        {
            throw new FileSystemException( "vfs.provider/write-read-only.error", name );
        }

        if ( type == FileType.IMAGINARY )
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
    private void detach() throws Exception
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
            if ( type == null )
            {
                type = FileType.IMAGINARY;
            }
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
        if ( type == FileType.IMAGINARY )
        {
            // File was created
            handleCreate( FileType.FILE );
        }
        else
        {
            // File has changed
            onChange();
        }
    }

    /**
     * Called when this file is created.  Updates cached info and notifies
     * the parent and file system.
     */
    protected void handleCreate( final FileType newType ) throws Exception
    {
        if ( attached )
        {
            // Fix up state
            type = newType;
            children = EMPTY_FILE_ARRAY;

            // Notify subclass
            onChange();
        }

        // Notify parent that its child list may no longer be valid
        notifyParent();

        // Notify the file system
        fs.fireFileCreated( this );
    }

    /**
     * Called when this file is deleted.  Updates cached info and notifies
     * subclasses, parent and file system.
     */
    protected void handleDelete() throws Exception
    {
        if ( attached )
        {
            // Fix up state
            type = FileType.IMAGINARY;
            children = null;

            // Notify subclass
            onChange();
        }

        // Notify parent that its child list may no longer be valid
        notifyParent();

        // Notify the file system
        fs.fireFileDeleted( this );
    }

    /**
     * Notifies the file that its children have changed.
     * @todo Indicate whether the child was added or removed, and which child.
     */
    protected void childrenChanged() throws Exception
    {
        // TODO - this may be called when not attached

        children = null;
        onChildrenChanged();
    }

    /**
     * Notify the parent of a change to its children, when a child is created
     * or deleted.
     */
    private void notifyParent() throws Exception
    {
        if ( parent == null )
        {
            // Locate the parent, if it is cached
            parent = (AbstractFileObject)fs.getFile( name.getParent() );
        }

        if ( parent != null )
        {
            parent.childrenChanged();
        }
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
        if ( file.getType().hasChildren() && selector.traverseDescendents( fileInfo ) )
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
