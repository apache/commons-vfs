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
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
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
    private static final char[] ENC = {'!'};

    private static final Log LOG = LogFactory.getLog(ZipFileSystem.class);

    private final File file;
    private final Charset charset;
    private ZipFileThreadLocal zipFile = new ZipFileThreadLocal();

    private class ZipFileCreationException extends RuntimeException {
        ZipFileCreationException(Throwable cause) {
            super(cause);
        }
    }

    private class ZipFileThreadLocal {

        private ThreadLocal<Boolean> isPresent = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return Boolean.FALSE;
            }
        };
        private ThreadLocal<ZipFile> zipFile = new ThreadLocal<ZipFile>() {
            @Override
            public ZipFile initialValue() {
                if (isPresent.get()) {
                    throw new IllegalStateException("Creating an initial value but we already have one");
                }
                try {
                    isPresent.set(Boolean.TRUE);
                    return createZipFile(ZipFileSystem.this.file);
                } catch (FileSystemException fse) {
                    throw new ZipFileCreationException(fse);
                }
            }
        };

        public ZipFile getFile() throws FileSystemException {
            try {
                return zipFile.get();
            } catch (ZipFileCreationException e) {
                throw new FileSystemException(e);
            }
        }

        public void closeFile() throws IOException {
            if (isPresent.get()) {
                ZipFile file = zipFile.get();
                file.close();
                zipFile.remove();
                isPresent.set(Boolean.FALSE);
            }
        }
    }

    /**
     * Cache doesn't need to be synchronized since it is read-only.
     */
    private final Map<FileName, FileObject> cache = new HashMap<>();

    /**
     * Constructs a new instance.
     *
     * @param rootFileName The root file name of this file system.
     * @param parentLayer The parent layer of this file system.
     * @param fileSystemOptions Options to build this file system.
     * @throws FileSystemException If the parent layer does not exist, or on error replicating the file.
     */
    public ZipFileSystem(final AbstractFileName rootFileName, final FileObject parentLayer, final FileSystemOptions fileSystemOptions)
        throws FileSystemException {
        super(rootFileName, parentLayer, fileSystemOptions);

        // Make a local copy of the file
        file = parentLayer.getFileSystem().replicateFile(parentLayer, Selectors.SELECT_SELF);
        this.charset = ZipFileSystemConfigBuilder.getInstance().getCharset(fileSystemOptions);
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

    protected ZipFile createZipFile(final File file) throws FileSystemException {
        try {
            return charset == null ? new ZipFile(file) : new ZipFile(file, charset);
        } catch (final IOException ioe) {
            throw new FileSystemException("vfs.provider.zip/open-zip-file.error", file, ioe);
        }
    }

    protected ZipFileObject createZipFileObject(final AbstractFileName name, final ZipEntry entry)
            throws FileSystemException {
        return new ZipFileObject(name, entry, this, true);
    }

    @Override
    protected void doCloseCommunicationLink() {
        // Release the zip files
        try {
            zipFile.closeFile();
        } catch (final IOException e) {
            // getLogger().warn("vfs.provider.zip/close-zip-file.error :" + file, e);
            VfsLog.warn(getLogger(), LOG, "vfs.provider.zip/close-zip-file.error :" + file, e);
        }
    }

    protected Charset getCharset() {
        return charset;
    }

    /**
     * Returns a cached file.
     */
    @Override
    protected FileObject getFileFromCache(final FileName name) {
        return cache.get(name);
    }

    protected ZipFile getZipFile() throws FileSystemException {
        return zipFile.getFile();
    }

    @Override
    public void init() throws FileSystemException {
        super.init();

        try {
            // Build the index
            final Enumeration<? extends ZipEntry> entries = getZipFile().entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                final AbstractFileName name = (AbstractFileName) getFileSystemManager().resolveName(getRootName(),
                        UriParser.encode(entry.getName(), ENC));

                // Create the file
                ZipFileObject fileObj;
                if (entry.isDirectory() && getFileFromCache(name) != null) {
                    fileObj = (ZipFileObject) getFileFromCache(name);
                    fileObj.setZipEntry(entry);
                    continue;
                }

                fileObj = createZipFileObject(name, entry);
                putFileToCache(fileObj);

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
                    }

                    // Attach child to parent
                    parent.attachChild(fileObj.getName());
                }
            }
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

    /*
      will be called after all file-objects closed their streams. protected void notifyAllStreamsClosed() {
      closeCommunicationLink(); }
     */
}
