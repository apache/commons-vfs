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
package org.apache.commons.vfs2.provider.ram;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

/**
 * A RAM File System.
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public class RamFileSystem extends AbstractFileSystem implements Serializable
{
    /**
     * serialVersionUID format is YYYYMMDD for the date of the last binary change.
     */
    private static final long serialVersionUID = 20101208L;

    /**
     * Cache of RAM File Data
     */
    private final Map<FileName, RamFileData> cache;

    /**
     * @param rootName The root file name.
     * @param fileSystemOptions The FileSystem options.
     */
    protected RamFileSystem(FileName rootName, FileSystemOptions fileSystemOptions)
    {
        super(rootName, null, fileSystemOptions);
        this.cache = Collections.synchronizedMap(new HashMap<FileName, RamFileData>());
        // create root
        RamFileData rootData = new RamFileData(rootName);
        rootData.setType(FileType.FOLDER);
        rootData.setLastModified(System.currentTimeMillis());
        this.cache.put(rootName, rootData);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileSystem#createFile(org.apache.commons.vfs2.FileName)
     */
    @Override
    protected FileObject createFile(AbstractFileName name) throws Exception
    {
        RamFileObject file = new RamFileObject(name, this);
        return file;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileSystem#addCapabilities(java.util.Collection)
     */
    @Override
    protected void addCapabilities(Collection<Capability> caps)
    {
        caps.addAll(RamFileProvider.capabilities);
    }

    /**
     * @param name The name of the file.
     * @return children The names of the children.
     */
    String[] listChildren(FileName name)
    {
        RamFileData data = this.cache.get(name);
        if (data == null || !data.getType().hasChildren())
        {
            return null;
        }
        Collection<RamFileData> children = data.getChildren();
        String[] names;

        synchronized (children)
        {
            names = new String[children.size()];

            int pos = 0;
            Iterator<RamFileData> iter = children.iterator();
            while (iter.hasNext())
            {
                RamFileData childData = iter.next();
                names[pos] = childData.getName().getBaseName();
                pos++;
            }
        }

        return names;
    }

    /**
     * Delete a file
     *
     * @param file
     * @throws FileSystemException
     */
    void delete(RamFileObject file) throws FileSystemException
    {
        // root is read only check
        if (file.getParent() == null)
        {
            throw new FileSystemException("unable to delete root");
        }

        // Remove reference from cache
        this.cache.remove(file.getName());
        // Notify the parent
        RamFileObject parent = (RamFileObject) this.resolveFile(file
                .getParent().getName());
        parent.getData().removeChild(file.getData());
        parent.close();
        // Close the file
        file.getData().clear();
        file.close();
    }

    /**
     * Saves a file
     *
     * @param file
     * @throws FileSystemException
     */
    void save(final RamFileObject file) throws FileSystemException
    {

        // Validate name
        if (file.getData().getName() == null)
        {
            throw new FileSystemException(new IllegalStateException(
                    "The data has no name. " + file));
        }

        // Add to the parent
        if (file.getName().getDepth() > 0)
        {
            RamFileData parentData = this.cache.get(file.getParent().getName());
            // Only if not already added
            if (!parentData.hasChildren(file.getData()))
            {
                RamFileObject parent = (RamFileObject) file.getParent();
                parent.getData().addChild(file.getData());
                parent.close();
            }
        }
        // Store in cache
        cache.put(file.getName(), file.getData());
        file.getData().updateLastModified();
        file.close();
    }

    /**
     * @param from The original file.
     * @param to The new file.
     * @throws FileSystemException if an error occurs.
     */
    void rename(RamFileObject from, RamFileObject to)
            throws FileSystemException
    {
        if (!this.cache.containsKey(from.getName()))
        {
            throw new FileSystemException("File does not exist: "
                    + from.getName());
        }
        // Copy data

        to.getData().setBuffer(from.getData().getBuffer());
        to.getData().setLastModified(from.getData().getLastModified());
        to.getData().setType(from.getData().getType());

        this.save(to);
        this.delete(from);
    }

    public void attach(RamFileObject fo)
    {
        if (fo.getName() == null)
        {
            throw new IllegalArgumentException("Null argument");
        }
        RamFileData data = this.cache.get(fo.getName());
        if (data == null)
        {
            data = new RamFileData(fo.getName());
        }
        fo.setData(data);
    }

    /**
     * Import a Tree.
     *
     * @param file The File
     * @throws FileSystemException if an error occurs.
     */
    public void importTree(File file) throws FileSystemException
    {
        FileObject fileFo = getFileSystemManager().toFileObject(file);
        this.toRamFileObject(fileFo, fileFo);
    }

    /**
     * Import the given file with the name relative to the given root
     *
     * @param fo
     * @param root
     * @throws FileSystemException
     */
    void toRamFileObject(FileObject fo, FileObject root)
            throws FileSystemException
    {
        RamFileObject memFo = (RamFileObject) this.resolveFile(fo.getName()
                .getPath().substring(root.getName().getPath().length()));
        if (fo.getType().hasChildren())
        {
            // Create Folder
            memFo.createFolder();
            // Import recursively
            FileObject[] fos = fo.getChildren();
            for (int i = 0; i < fos.length; i++)
            {
                FileObject child = fos[i];
                this.toRamFileObject(child, root);
            }
        }
        else if (fo.getType().equals(FileType.FILE))
        {
            // Read bytes
            try
            {
                InputStream is = fo.getContent().getInputStream();
                try
                {
                    OutputStream os = new BufferedOutputStream(memFo
                            .getOutputStream(), 512);
                    int i;
                    while ((i = is.read()) != -1)
                    {
                        os.write(i);
                    }
                    os.flush();
                    os.close();
                }
                finally
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                        // ignore on close exception
                    }
                }
            }
            catch (IOException e)
            {
                throw new FileSystemException(e.getClass().getName() + " "
                        + e.getMessage());
            }
        }
        else
        {
            throw new FileSystemException("File is not a folder nor a file "
                    + memFo);
        }
    }

    /**
     * @return Returns the size of the FileSystem
     */
    int size()
    {
        int size = 0;
        synchronized (cache)
        {
            Iterator<RamFileData> iter = cache.values().iterator();
            while (iter.hasNext())
            {
                RamFileData data = iter.next();
                size += data.size();
            }
        }
        return size;
    }

    /**
     * Close the RAMFileSystem.
     */
    @Override
    public void close()
    {
        this.cache.clear();
        super.close();
    }
}
