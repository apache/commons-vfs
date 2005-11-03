/*
 * Copyright 2002-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.provider.smb;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.util.RandomAccessMode;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

/**
 * A file in an SMB file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class SmbFileObject
    extends AbstractFileObject
    implements FileObject
{
    // private final String fileName;
    private SmbFile file;

    protected SmbFileObject(final FileName name,
                            final SmbFileSystem fileSystem) throws FileSystemException
    {
        super(name, fileSystem);
        // this.fileName = UriParser.decode(name.getURI());
    }

    /**
     * Attaches this file object to its file resource.
     */
    protected void doAttach() throws Exception
    {
        // Defer creation of the SmbFile to here
        if (file == null)
        {
            file = createSmbFile(getName());
        }
    }

    protected void doDetach() throws Exception
    {
        // file closed through content-streams
        file = null;
    }

    private SmbFile createSmbFile(FileName fileName) throws MalformedURLException, SmbException, FileSystemException
    {
        SmbFileName smbFileName = (SmbFileName) fileName;

        String path = smbFileName.getUriWithoutAuth();

        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
            smbFileName.getDomain(), smbFileName.getUserName(), smbFileName.getPassword());
        SmbFile file = new SmbFile(path, auth);

        if (file.isDirectory() && !file.toString().endsWith("/"))
        {
            file = new SmbFile(path + "/", auth);
        }

        return file;
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     */
    protected FileType doGetType() throws Exception
    {
        if (!file.exists())
        {
            return FileType.IMAGINARY;
        }
        else if (file.isDirectory())
        {
            return FileType.FOLDER;
        }
        else if (file.isFile())
        {
            return FileType.FILE;
        }

        throw new FileSystemException("vfs.provider.smb/get-type.error", getName());
    }

    /**
     * Lists the children of the file.  Is only called if {@link #doGetType}
     * returns {@link FileType#FOLDER}.
     */
    protected String[] doListChildren() throws Exception
    {
        return UriParser.encode(file.list());
    }

    /**
     * Determines if this file is hidden.
     */
    protected boolean doIsHidden() throws Exception
    {
        return file.isHidden();
    }

    /**
     * Deletes the file.
     */
    protected void doDelete() throws Exception
    {
        file.delete();
    }

    protected void doRename(FileObject newfile) throws Exception
    {
        file.renameTo(createSmbFile(newfile.getName()));
    }

    /**
     * Creates this file as a folder.
     */
    protected void doCreateFolder() throws Exception
    {
        file.mkdir();
        file = createSmbFile(getName());
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize() throws Exception
    {
        return file.length();
    }

    /**
     * Returns the last modified time of this file.
     */
    protected long doGetLastModifiedTime()
        throws Exception
    {
        return file.getLastModified();
    }

    /**
     * Creates an input stream to read the file content from.
     */
    protected InputStream doGetInputStream() throws Exception
    {
        return new SmbFileInputStream(file);
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream(boolean bAppend) throws Exception
    {
        return new SmbFileOutputStream(file, bAppend);
    }

    /**
     * random access
     */
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception
    {
        return new SmbFileRandomAccessContent(file, mode);
    }
}
