/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider.nfs;

import com.emc.ecs.nfsclient.nfs.NfsException;
import com.emc.ecs.nfsclient.nfs.NfsStatus;
import com.emc.ecs.nfsclient.nfs.io.Nfs3File;
import com.emc.ecs.nfsclient.nfs.io.NfsFile;
import com.emc.ecs.nfsclient.nfs.io.NfsFileInputStream;
import com.emc.ecs.nfsclient.nfs.io.NfsFileOutputStream;
import com.emc.ecs.nfsclient.nfs.nfs3.Nfs3;
import com.emc.ecs.nfsclient.rpc.CredentialUnix;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.RandomAccessMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * A file in an NFS file system.
 */
public class NfsFileObject extends AbstractFileObject<NfsFileSystem> {
    // private final String fileName;
    private NfsFile file;


    protected NfsFileObject(final AbstractFileName name, final NfsFileSystem fileSystem) throws IOException {
        super(name, fileSystem);
        // this.fileName = UriParser.decode(name.getURI());
    }

    /**
     * Attaches this file object to its file resource.
     */
    @Override
    protected void doAttach() throws Exception {
        // Defer creation of the NfsFile to here
        if (file == null) {
            file = createNfsFile(getName());
        }
    }

    @Override
    protected void doDetach() throws Exception {
        // file closed through content-streams
        file = null;
    }

    private Nfs3File createNfsFile(final FileName fileName) throws IOException {
        final NfsFileName nfsFileName = (NfsFileName) fileName;

//        final String path = nfsFileName.getUriWithoutAuth();
        Nfs3 nfs3 = new Nfs3("10.65.10.146:/srv/nfs", new CredentialUnix(0, 0, null), 3);

        return new Nfs3File(nfs3, fileName.getBaseName());

    }

    /**
     * Determines the type of the file, returns null if the file does not exist.
     */
    @Override
    protected FileType doGetType() throws Exception {
        if (!file.exists()) {
            return FileType.IMAGINARY;
        }
        if (file.isDirectory()) {
            return FileType.FOLDER;
        }
        if (file.isFile()) {
            return FileType.FILE;
        }

        throw new FileSystemException("vfs.provider.nfs/get-type.error", getName());
    }

    /**
     * Lists the children of the file. Is only called if {@link #doGetType} returns {@link FileType#FOLDER}.
     */
    @Override
    protected String[] doListChildren() throws Exception {
        // VFS-210: do not try to get listing for anything else than directories
        if (!file.isDirectory()) {
            return null;
        }
        List<String> list = file.list();
        return UriParser.encode(list.toArray(new String[0]));
    }

    /**
     * Determines if this file is hidden.
     */
    @Override
    protected boolean doIsHidden() throws Exception {
        return false;
    }

    /**
     * Deletes the file.
     */
    @Override
    protected void doDelete() throws Exception {
        file.delete();
    }

    @Override
    protected void doRename(final FileObject newfile) throws Exception {
        file.renameTo(createNfsFile(newfile.getName()));
    }

    /**
     * Creates this file as a folder.
     */
    @Override
    protected void doCreateFolder() throws Exception {
        file.mkdir();
        file = createNfsFile(getName());
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    @Override
    protected long doGetContentSize() throws Exception {
        return file.length();
    }

    /**
     * Returns the last modified time of this file.
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        return file.lastModified();
    }

    /**
     * Creates an input stream to read the file content from.
     */
    @Override
    protected InputStream doGetInputStream(final int bufferSize) throws Exception {
        if (!getType().hasContent()) {
            throw new FileSystemException("vfs.provider/read-not-file.error", getName());
        }
        try {
            return new NfsFileInputStream(file);
        } catch (final NfsException e) {
            if (e.getStatus() == NfsStatus.NFS3ERR_NOENT) {
                throw new FileNotFoundException(getName());
            }
            if (file.isDirectory()) {
                throw new FileTypeHasNoContentException(getName());
            }

            throw e;
        }
    }

    /**
     * Creates an output stream to write the file content to.
     */
    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception {
        return new NfsFileOutputStream(file);
    }

    /**
     * random access
     */
    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
        return new NfsFileRandomAccessContent(file, mode);
    }

    @Override
    protected boolean doSetLastModifiedTime(final long modtime) throws Exception {
        file.setLastModified(modtime);
        return true;
    }
}
