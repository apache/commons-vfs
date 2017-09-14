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
package org.apache.commons.vfs2.provider.zip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VfsLog;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.UriParser;

/**
 * A read-only file system for ZIP and JAR files.
 */
public class ZipFileSystem extends AbstractFileSystem {
    private static final Log LOG = LogFactory.getLog(ZipFileSystem.class);

    private final File file;
    private ZipFile zipFile;

    /**
     * Cache doesn't need to be synchronized since it is read-only.
     */
    private final Map<FileName, FileObject> cache = new HashMap<>();

    public ZipFileSystem(final AbstractFileName rootName, final FileObject parentLayer,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        super(rootName, parentLayer, fileSystemOptions);

        // Make a local copy of the file
        file = parentLayer.getFileSystem().replicateFile(parentLayer, Selectors.SELECT_SELF);

        // Open the Zip file
        if (!file.exists()) {
            // Don't need to do anything
            zipFile = null;
            return;
        }

        // zipFile = createZipFile(this.file);
    }

    @Override
    public void init() throws FileSystemException {
        super.init();

        try {
            // Build the index
            final List<ZipFileObject> strongRef = new ArrayList<>(getZipFile().size());
            final Enumeration<? extends ZipEntry> entries = getZipFile().entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                final AbstractFileName name = (AbstractFileName) getFileSystemManager().resolveName(getRootName(),
                        UriParser.encode(entry.getName()));

                // Create the file
                ZipFileObject fileObj;
                if (entry.isDirectory() && getFileFromCache(name) != null) {
                    fileObj = (ZipFileObject) getFileFromCache(name);
                    fileObj.setZipEntry(entry);
                    continue;
                }

                fileObj = createZipFileObject(name, entry);
                putFileToCache(fileObj);
                strongRef.add(fileObj);
                fileObj.holdObject(strongRef);

                // Make sure all ancestors exist
                // TODO - create these on demand
                ZipFileObject parent;
                for (AbstractFileName parentName = (AbstractFileName) name
                        .getParent(); parentName != null; fileObj = parent, parentName = (AbstractFileName) parentName
                                .getParent()) {
                    // Locate the parent
                    parent = (ZipFileObject) getFileFromCache(parentName);
                    if (parent == null) {
                        parent = createZipFileObject(parentName, null);
                        putFileToCache(parent);
                        strongRef.add(parent);
                        parent.holdObject(strongRef);
                    }

                    // Attach child to parent
                    parent.attachChild(fileObj.getName());
                }
            }
        } finally {
            closeCommunicationLink();
        }
    }

    protected ZipFile getZipFile() throws FileSystemException {
        if (zipFile == null && this.file.exists()) {
            this.zipFile = createZipFile(this.file);
        }

        return zipFile;
    }

    protected ZipFileObject createZipFileObject(final AbstractFileName name, final ZipEntry entry)
            throws FileSystemException {
        return new ZipFileObject(name, entry, this, true);
    }

    protected ZipFile createZipFile(final File file) throws FileSystemException {
        try {
            return new ZipFile(file);
        } catch (final IOException ioe) {
            throw new FileSystemException("vfs.provider.zip/open-zip-file.error", file, ioe);
        }
    }

    @Override
    protected void doCloseCommunicationLink() {
        // Release the zip file
        try {
            if (zipFile != null) {
                zipFile.close();
                zipFile = null;
            }
        } catch (final IOException e) {
            // getLogger().warn("vfs.provider.zip/close-zip-file.error :" + file, e);
            VfsLog.warn(getLogger(), LOG, "vfs.provider.zip/close-zip-file.error :" + file, e);
        }
    }

    /**
     * Returns the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps) {
        caps.addAll(ZipFileProvider.capabilities);
    }

    /**
     * Creates a file object.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws FileSystemException {
        // This is only called for files which do not exist in the Zip file
        return new ZipFileObject(name, null, this, false);
    }

    /**
     * Adds a file object to the cache.
     */
    @Override
    protected void putFileToCache(final FileObject file) {
        cache.put(file.getName(), file);
    }

    /**
     * Returns a cached file.
     */
    @Override
    protected FileObject getFileFromCache(final FileName name) {
        return cache.get(name);
    }

    /**
     * remove a cached file.
     */
    @Override
    protected void removeFileFromCache(final FileName name) {
        cache.remove(name);
    }

    @Override
    public String toString() {
        return super.toString() + " for " + file;
    }

    /**
     * will be called after all file-objects closed their streams. protected void notifyAllStreamsClosed() {
     * closeCommunicationLink(); }
     */
}
