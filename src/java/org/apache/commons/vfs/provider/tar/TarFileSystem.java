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
package org.apache.commons.vfs.provider.tar;

import org.apache.commons.compress.tar.TarEntry;
import org.apache.commons.compress.tar.TarInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VfsLog;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.bzip2.Bzip2FileObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * A read-only file system for Tar files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class TarFileSystem
    extends AbstractFileSystem
    implements FileSystem
{
    private final static Log log = LogFactory.getLog(TarFileSystem.class);

    private final File file;
    private TarInputStream tarFile;

    protected TarFileSystem(final FileName rootName,
                            final FileObject parentLayer,
                            final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        super(rootName, parentLayer, fileSystemOptions);

        // Make a local copy of the file
        file = parentLayer.getFileSystem().replicateFile(parentLayer, Selectors.SELECT_SELF);

        // Open the Tar file
        if (!file.exists())
        {
            // Don't need to do anything
            tarFile = null;
            return;
        }

        // tarFile = createTarFile(this.file);
    }

    public void init() throws FileSystemException
    {
        super.init();

        // Build the index
        try
        {
            List strongRef = new ArrayList(100);
            TarEntry entry;
            while ((entry = getTarFile().getNextEntry()) != null)
            {
                FileName name = getFileSystemManager().resolveName(getRootName(), UriParser.encode(entry.getName()));

                // Create the file
                TarFileObject fileObj;
                if (entry.isDirectory() && getFileFromCache(name) != null)
                {
                    fileObj = (TarFileObject) getFileFromCache(name);
                    fileObj.setTarEntry(entry);
                    continue;
                }

                fileObj = createTarFileObject(name, entry);
                putFileToCache(fileObj);
                strongRef.add(fileObj);
                fileObj.holdObject(strongRef);

                // Make sure all ancestors exist
                // TODO - create these on demand
                TarFileObject parent = null;
                for (FileName parentName = name.getParent();
                     parentName != null;
                     fileObj = parent, parentName = parentName.getParent())
                {
                    // Locate the parent
                    parent = (TarFileObject) getFileFromCache(parentName);
                    if (parent == null)
                    {
                        parent = createTarFileObject(parentName, null);
                        putFileToCache(parent);
                        strongRef.add(parent);
                        parent.holdObject(strongRef);
                    }

                    // Attach child to parent
                    parent.attachChild(fileObj.getName());
                }
            }
        }
        catch (IOException e)
        {
            throw new FileSystemException(e);
        }
        finally
        {
            closeCommunicationLink();
        }
    }

    public InputStream getInputStream(TarEntry entry) throws FileSystemException
    {
        resetTarFile();
        try
        {
            while (!tarFile.getNextEntry().equals(entry))
            {
            }
            return tarFile;
        }
        catch (IOException e)
        {
            throw new FileSystemException(e);
        }
    }

    protected void resetTarFile() throws FileSystemException
    {
        // Reading specific entries requires skipping through the tar file from the beginning
        // Not especially elegant, but we don't have the ability to seek to specific positions
        // with an input stream.
        if (this.file.exists())
        {
            recreateTarFile();
        }
    }

    private void recreateTarFile() throws FileSystemException
    {
        if (this.tarFile != null)
        {
            try
            {
                this.tarFile.close();
            }
            catch (IOException e)
            {
                throw new FileSystemException("vfs.provider.tar/close-tar-file.error", file, e);
            }
            tarFile = null;
        }
        TarInputStream tarFile = createTarFile(this.file);
        this.tarFile = tarFile;
    }

    protected TarInputStream getTarFile() throws FileSystemException
    {
        if (tarFile == null && this.file.exists())
        {
            recreateTarFile();
        }

        return tarFile;
    }

    protected TarFileObject createTarFileObject(final FileName name,
                                                final TarEntry entry) throws FileSystemException
    {
        return new TarFileObject(name, entry, this, true);
    }

    protected TarInputStream createTarFile(final File file) throws FileSystemException
    {
        try
        {
            if ("tgz".equalsIgnoreCase(getRootName().getScheme()))
            {
                return new TarInputStream(new GZIPInputStream(new FileInputStream(file)));
            }
            else if ("tbz2".equalsIgnoreCase(getRootName().getScheme()))
            {
                return new TarInputStream(Bzip2FileObject.wrapInputStream(file.getAbsolutePath(), new FileInputStream(file)));
            }
            return new TarInputStream(new FileInputStream(file));
        }
        catch (IOException ioe)
        {
            throw new FileSystemException("vfs.provider.tar/open-tar-file.error", file, ioe);
        }
    }

    protected void doCloseCommunicationLink()
    {
        // Release the tar file
        try
        {
            if (tarFile != null)
            {
                tarFile.close();
                tarFile = null;
            }
        }
        catch (final IOException e)
        {
            // getLogger().warn("vfs.provider.tar/close-tar-file.error :" + file, e);
            VfsLog.warn(getLogger(), log, "vfs.provider.tar/close-tar-file.error :" + file, e);
        }
    }

    /**
     * Returns the capabilities of this file system.
     */
    protected void addCapabilities(final Collection caps)
    {
        caps.addAll(TarFileProvider.capabilities);
    }

    /**
     * Creates a file object.
     */
    protected FileObject createFile(final FileName name) throws FileSystemException
    {
        // This is only called for files which do not exist in the Tar file
        return new TarFileObject(name, null, this, false);
    }

    /**
     * will be called after all file-objects closed their streams.
     */
    protected void notifyAllStreamsClosed()
    {
        closeCommunicationLink();
    }
}
