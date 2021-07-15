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
package org.apache.commons.vfs2.provider.tar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
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
import org.apache.commons.vfs2.provider.bzip2.Bzip2FileObject;

/**
 * A read-only file system for Tar files.
 */
public class TarFileSystem extends AbstractFileSystem {
    private static final Log LOG = LogFactory.getLog(TarFileSystem.class);

    private final char[] ENC = {'!'};

    private final File file;

    private TarArchiveInputStream tarFile;

    /**
     * Cache doesn't need to be synchronized since it is read-only.
     */
    private final Map<FileName, FileObject> cache = new HashMap<>();

    protected TarFileSystem(final AbstractFileName rootName, final FileObject parentLayer,
        final FileSystemOptions fileSystemOptions) throws FileSystemException {
        super(rootName, parentLayer, fileSystemOptions);

        // Make a local copy of the file
        file = parentLayer.getFileSystem().replicateFile(parentLayer, Selectors.SELECT_SELF);

        // Open the Tar file
        if (!file.exists()) {
            // Don't need to do anything
            tarFile = null;
        }

        // tarFile = createTarFile(this.file);
    }

    /**
     * Returns the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps) {
        caps.addAll(TarFileProvider.capabilities);
    }

    /**
     * Creates a file object.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws FileSystemException {
        // This is only called for files which do not exist in the Tar file
        return new TarFileObject(name, null, this, false);
    }

    protected TarArchiveInputStream createTarFile(final File file) throws FileSystemException {
        try {
            if ("tgz".equalsIgnoreCase(getRootName().getScheme())) {
                return new TarArchiveInputStream(new GZIPInputStream(Files.newInputStream(file.toPath())));
            }
            if ("tbz2".equalsIgnoreCase(getRootName().getScheme())) {
                return new TarArchiveInputStream(
                    Bzip2FileObject.wrapInputStream(file.getAbsolutePath(), Files.newInputStream(file.toPath())));
            }
            return new TarArchiveInputStream(Files.newInputStream(file.toPath()));
        } catch (final IOException ioe) {
            throw new FileSystemException("vfs.provider.tar/open-tar-file.error", file, ioe);
        }
    }

    protected TarFileObject createTarFileObject(final AbstractFileName name, final TarArchiveEntry entry) {
        return new TarFileObject(name, entry, this, true);
    }

    @Override
    protected void doCloseCommunicationLink() {
        // Release the tar file
        try {
            if (tarFile != null) {
                tarFile.close();
                tarFile = null;
            }
        } catch (final IOException e) {
            // getLogger().warn("vfs.provider.tar/close-tar-file.error :" + file, e);
            VfsLog.warn(getLogger(), LOG, "vfs.provider.tar/close-tar-file.error :" + file, e);
        }
    }

    /**
     * Returns a cached file.
     */
    @Override
    protected FileObject getFileFromCache(final FileName name) {
        return cache.get(name);
    }

    public InputStream getInputStream(final TarArchiveEntry entry) throws FileSystemException {
        resetTarFile();
        try {
            while (!tarFile.getNextEntry().equals(entry)) {
            }
            return tarFile;
        } catch (final IOException e) {
            throw new FileSystemException(e);
        }
    }

    protected TarArchiveInputStream getTarFile() throws FileSystemException {
        if (tarFile == null && this.file.exists()) {
            recreateTarFile();
        }

        return tarFile;
    }

    @Override
    public void init() throws FileSystemException {
        super.init();

        // Build the index
        try {
            TarArchiveEntry entry;
            while ((entry = getTarFile().getNextTarEntry()) != null) {
                final AbstractFileName name = (AbstractFileName) getFileSystemManager().resolveName(getRootName(),
                    UriParser.encode(entry.getName(), ENC));

                // Create the file
                TarFileObject fileObj;
                if (entry.isDirectory() && getFileFromCache(name) != null) {
                    fileObj = (TarFileObject) getFileFromCache(name);
                    fileObj.setTarEntry(entry);
                    continue;
                }

                fileObj = createTarFileObject(name, entry);
                putFileToCache(fileObj);

                // Make sure all ancestors exist
                // TODO - create these on demand
                TarFileObject parent = null;
                for (AbstractFileName parentName = (AbstractFileName) name
                    .getParent(); parentName != null; fileObj = parent, parentName = (AbstractFileName) parentName
                        .getParent()) {
                    // Locate the parent
                    parent = (TarFileObject) getFileFromCache(parentName);
                    if (parent == null) {
                        parent = createTarFileObject(parentName, null);
                        putFileToCache(parent);
                    }

                    // Attach child to parent
                    parent.attachChild(fileObj.getName());
                }
            }
        } catch (final IOException e) {
            throw new FileSystemException(e);
        } finally {
            closeCommunicationLink();
        }
    }

    /**
     * Adds a file object to the cache.
     */
    @Override
    protected void putFileToCache(final FileObject file) {
        cache.put(file.getName(), file);
    }

    /**
     * will be called after all file-objects closed their streams. protected void notifyAllStreamsClosed() {
     * closeCommunicationLink(); }
     */

    private void recreateTarFile() throws FileSystemException {
        if (this.tarFile != null) {
            try {
                this.tarFile.close();
            } catch (final IOException e) {
                throw new FileSystemException("vfs.provider.tar/close-tar-file.error", file, e);
            }
            tarFile = null;
        }
        this.tarFile = createTarFile(this.file);
    }

    /**
     * remove a cached file.
     */
    @Override
    protected void removeFileFromCache(final FileName name) {
        cache.remove(name);
    }

    protected void resetTarFile() throws FileSystemException {
        // Reading specific entries requires skipping through the tar file from the beginning
        // Not especially elegant, but we don't have the ability to seek to specific positions
        // with an input stream.
        if (this.file.exists()) {
            recreateTarFile();
        }
    }
}
