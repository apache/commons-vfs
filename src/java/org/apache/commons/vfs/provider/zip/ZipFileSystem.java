/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
package org.apache.commons.vfs.provider.zip;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A read-only file system for Zip/Jar files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.8 $ $Date: 2002/07/05 06:52:16 $
 */
public class ZipFileSystem
    extends AbstractFileSystem
    implements FileSystem
{
    private final File file;
    protected final ZipFile zipFile;

    public ZipFileSystem(final FileSystemManager manager,
                         final FileName rootName,
                         final FileObject parentLayer,
                         final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        super(manager, rootName, parentLayer, fileSystemOptions);

        // Make a local copy of the file
        file = parentLayer.getFileSystem().replicateFile(parentLayer, Selectors.SELECT_SELF);

        // Open the Zip file
        if (!file.exists())
        {
            // Don't need to do anything
            zipFile = null;
            return;
        }

        zipFile = createZipFile(this.file);

        // Build the index
        Enumeration entries = zipFile.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            FileName name = rootName.resolveName(entry.getName());

            // Create the file
            ZipFileObject fileObj;
            if (entry.isDirectory() && getFile(name) != null)
            {
                fileObj = (ZipFileObject) getFile(name);
                fileObj.setZipEntry(entry);
                continue;
            }

            fileObj = createZipFileObject(name, entry, zipFile);
            putFile(fileObj);

            // Make sure all ancestors exist
            // TODO - create these on demand
            ZipFileObject parent = null;
            for (FileName parentName = name.getParent();
                 parentName != null;
                 fileObj = parent, parentName = parentName.getParent())
            {
                // Locate the parent
                parent = (ZipFileObject) getFile(parentName);
                if (parent == null)
                {
                    parent = createZipFileObject(parentName, null, zipFile);
                    putFile(parent);
                }

                // Attach child to parent
                parent.attachChild(fileObj.getName());
            }
        }
    }

    protected ZipFileObject createZipFileObject(final FileName name,
                                                final ZipEntry entry,
                                                final ZipFile file)
    {
        return new ZipFileObject(name, entry, file, this);
    }

    protected ZipFile createZipFile(final File file) throws FileSystemException
    {
        try
        {
            return new ZipFile(file);
        }
        catch (IOException ioe)
        {
            throw new FileSystemException("vfs.provider.zip/open-zip-file.error", file, ioe);
        }
    }

    public void close()
    {
        // Release the zip file
        try
        {
            if (zipFile != null)
            {
                zipFile.close();
            }
        }
        catch (final IOException e)
        {
            getLogger().warn("vfs.provider.zip/close-zip-file.error :" + file, e);
        }

        super.close();
    }

    /**
     * Returns the capabilities of this file system.
     */
    protected void addCapabilities(final Collection caps)
    {
        caps.add(Capability.GET_LAST_MODIFIED);
        caps.add(Capability.GET_TYPE);
        caps.add(Capability.LIST_CHILDREN);
        caps.add(Capability.READ_CONTENT);
        caps.add(Capability.URI);
    }

    /**
     * Creates a file object.
     */
    protected FileObject createFile(final FileName name)
    {
        // This is only called for files which do not exist in the Zip file
        return new ZipFileObject(name, null, null, this);
    }
}
