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
package org.apache.commons.vfs.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import junit.framework.Assert;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;

/**
 * A file selector that asserts that all files are visited, in the correct
 * order.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/10/23 11:59:39 $
 */
public class VerifyingFileSelector
    extends Assert
    implements FileSelector
{
    private final FileInfo rootFile;
    private final List files = new ArrayList();

    private FileInfo currentFolderInfo;
    private FileObject currentFolder;
    private Set children;
    private List stack = new ArrayList();

    public VerifyingFileSelector( final FileInfo fileInfo )
    {
        this.rootFile = fileInfo;
        children = new HashSet();
        children.add( rootFile.baseName );
    }

    /**
     * Determines if a file or folder should be selected.
     */
    public boolean includeFile( final FileSelectInfo fileInfo )
        throws FileSystemException
    {
        final FileObject file = fileInfo.getFile();
        if ( file == currentFolder )
        {
            // Pop current folder
            assertEquals( 0, children.size() );
            currentFolder = currentFolder.getParent();
            currentFolderInfo = currentFolderInfo.getParent();
            children = (Set)stack.remove( 0 );
        }

        final String baseName = file.getName().getBaseName();

        final FileInfo childInfo = getChild( baseName );
        assertSame( childInfo.type, file.getType() );

        final boolean isChild = children.remove( baseName );
        assertTrue( isChild );

        files.add( file );
        return true;
    }

    /**
     * Determines whether a folder should be traversed.
     */
    public boolean traverseDescendents( final FileSelectInfo fileInfo )
        throws FileSystemException
    {
        // Check that the given file is a folder
        final FileObject folder = fileInfo.getFile();
        assertSame( FileType.FOLDER, folder.getType() );

        // Locate the info for the folder
        final String baseName = folder.getName().getBaseName();
        if ( currentFolder == null )
        {
            assertEquals( rootFile.baseName, baseName );
            currentFolderInfo = rootFile;
        }
        else
        {
            assertSame( currentFolder, folder.getParent() );

            // Locate the info for the child, and make sure it is folder
            currentFolderInfo = getChild( baseName );
            assertSame( FileType.FOLDER, currentFolderInfo.type );
        }

        // Push the folder
        stack.add( 0, children );
        children = new HashSet( currentFolderInfo.children.keySet() );
        currentFolder = folder;

        return true;
    }

    /**
     * Finds a child of the current folder.
     */
    private FileInfo getChild( final String baseName )
    {
        if ( currentFolderInfo == null )
        {
            assertEquals( rootFile.baseName, baseName );
            return rootFile;
        }
        else
        {
            final FileInfo child = (FileInfo)currentFolderInfo.children.get( baseName );
            assertNotNull( child );
            return child;
        }
    }

    /**
     * Asserts that the selector has seen all the files.
     * @return The files in the order they where visited.
     */
    public List finish()
    {
        assertEquals( 0, children.size() );
        return files;
    }
}
