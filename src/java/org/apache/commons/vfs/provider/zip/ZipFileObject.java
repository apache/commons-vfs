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
package org.apache.commons.vfs.provider.zip;

import java.io.InputStream;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;

/**
 * A file in a Zip file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/07/05 04:08:19 $
 */
public class ZipFileObject
    extends AbstractFileObject
    implements FileObject
{
    private final HashSet children = new HashSet();
    protected final ZipFile file;
    protected ZipEntry entry;
    private FileType type;

    public ZipFileObject( FileName name,
                          ZipEntry entry,
                          ZipFile zipFile,
                          ZipFileSystem fs )
    {
        super( name, fs );
        setZipEntry( entry );
        file = zipFile;
        if ( file == null )
        {
            type = null;
        }
    }

    /**
     * Sets the details for this file object.
     */
    protected void setZipEntry( final ZipEntry entry )
    {
        if ( this.entry != null )
        {
            return;
        }

        if ( ( entry == null ) || ( entry.isDirectory() ) )
        {
            type = FileType.FOLDER;
        }
        else
        {
            type = FileType.FILE;
        }

        this.entry = entry;
    }

    /**
     * Attaches a child
     */
    public void attachChild( FileName childName )
    {
        children.add( childName.getBaseName() );
    }

    /**
     * Returns true if this file is read-only.
     */
    public boolean isWriteable()
    {
        return false;
    }

    /**
     * Returns the file's type.
     */
    protected FileType doGetType()
    {
        return type;
    }

    /**
     * Lists the children of the file.
     */
    protected String[] doListChildren()
    {
        return (String[])children.toArray( new String[ children.size() ] );
    }

    /**
     * Returns the size of the file content (in bytes).  Is only called if
     * {@link #doGetType} returns {@link FileType#FILE}.
     */
    protected long doGetContentSize()
    {
        return entry.getSize();
    }

    /**
     * Returns the last modified time of this file.
     */
    protected long doGetLastModifiedTime() throws Exception
    {
        return entry.getTime();
    }

    /**
     * Creates an input stream to read the file content from.  Is only called
     * if  {@link #doGetType} returns {@link FileType#FILE}.  The input stream
     * returned by this method is guaranteed to be closed before this
     * method is called again.
     */
    protected InputStream doGetInputStream() throws Exception
    {
        return file.getInputStream( entry );
    }
}
