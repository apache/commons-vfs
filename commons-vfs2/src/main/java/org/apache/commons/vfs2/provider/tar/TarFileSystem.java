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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.Capability;
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
    private static final int DEFAULT_INDEX_SIZE = 100;

    private static final Log LOG = LogFactory.getLog(TarFileSystem.class);

    private final File file;
    private TarArchiveInputStream tarFile;

    protected TarFileSystem(final AbstractFileName rootName, final FileObject parentLayer,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        super(rootName, parentLayer, fileSystemOptions);

        // Make a local copy of the file
        file = parentLayer.getFileSystem().replicateFile(parentLayer, Selectors.SELECT_SELF);

        // Open the Tar file
        if (!file.exists()) {
            // Don't need to do anything
            tarFile = null;
            return;
        }

        // tarFile = createTarFile(this.file);
    }

    @Override
    public void init() throws FileSystemException {
        super.init();

        // Build the index
        try {
            final List<TarFileObject> strongRef = new ArrayList<>(DEFAULT_INDEX_SIZE);
            TarArchiveEntry entry;
            while ((entry = getTarFile().getNextTarEntry()) != null) {
                final AbstractFileName name = (AbstractFileName) getFileSystemManager().resolveName(getRootName(),
                        UriParser.encode(entry.getName()));

                // Create the file
                TarFileObject fileObj;
                if (entry.isDirectory() && getFileFromCache(name) != null) {
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
                for (AbstractFileName parentName = (AbstractFileName) name
                        .getParent(); parentName != null; fileObj = parent, parentName = (AbstractFileName) parentName
                                .getParent()) {
                    // Locate the parent
                    parent = (TarFileObject) getFileFromCache(parentName);
                    if (parent == null) {
                        parent = createTarFileObject(parentName, null);
                        putFileToCache(parent);
                        strongRef.add(parent);
                        parent.holdObject(strongRef);
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

    protected void resetTarFile() throws FileSystemException {
        // Reading specific entries requires skipping through the tar file from the beginning
        // Not especially elegant, but we don't have the ability to seek to specific positions
        // with an input stream.
        if (this.file.exists()) {
            recreateTarFile();
        }
    }

    private void recreateTarFile() throws FileSystemException {
        if (this.tarFile != null) {
            try {
                this.tarFile.close();
            } catch (final IOException e) {
                throw new FileSystemException("vfs.provider.tar/close-tar-file.error", file, e);
            }
            tarFile = null;
        }
        final TarArchiveInputStream tarFile = createTarFile(this.file);
        this.tarFile = tarFile;
    }

    protected TarArchiveInputStream getTarFile() throws FileSystemException {
        if (tarFile == null && this.file.exists()) {
            recreateTarFile();
        }

        return tarFile;
    }

    protected TarFileObject createTarFileObject(final AbstractFileName name, final TarArchiveEntry entry)
            throws FileSystemException {
        return new TarFileObject(name, entry, this, true);
    }

    protected TarArchiveInputStream createTarFile(final File file) throws FileSystemException {
        try {
            if ("tgz".equalsIgnoreCase(getRootName().getScheme())) {
                return new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(file)));
            } else if ("tbz2".equalsIgnoreCase(getRootName().getScheme())) {
                return new TarArchiveInputStream(
                        Bzip2FileObject.wrapInputStream(file.getAbsolutePath(), new FileInputStream(file)));
            }
            return new TarArchiveInputStream(new FileInputStream(file));
        } catch (final IOException ioe) {
            throw new FileSystemException("vfs.provider.tar/open-tar-file.error", file, ioe);
        }
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

    /**
     * will be called after all file-objects closed their streams. protected void notifyAllStreamsClosed() {
     * closeCommunicationLink(); }
     */
}
