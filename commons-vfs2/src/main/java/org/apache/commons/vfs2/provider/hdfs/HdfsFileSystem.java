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
package org.apache.commons.vfs2.provider.hdfs;

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * A VFS FileSystem that interacts with HDFS.
 *
 * @since 2.1
 */
public class HdfsFileSystem extends AbstractFileSystem {
    private static final Log log = LogFactory.getLog(HdfsFileSystem.class);

    private FileSystem fs;

    /**
     * Construct file system.
     *
     * @param rootName Name of the root directory of this file system.
     * @param fileSystemOptions options for this file system instance.
     */
    protected HdfsFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions) {
        super(rootName, null, fileSystemOptions);
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileSystem#addCapabilities(Collection)
     */
    @Override
    protected void addCapabilities(final Collection<Capability> capabilities) {
        capabilities.addAll(HdfsFileProvider.CAPABILITIES);
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileSystem#close()
     */
    @Override
    public void close() {
        try {
            if (null != fs) {
                fs.close();
            }
        } catch (final IOException e) {
            throw new RuntimeException("Error closing HDFS client", e);
        }
        super.close();
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileSystem#createFile(AbstractFileName)
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws Exception {
        throw new FileSystemException("Operation not supported");
    }

    /**
     * Resolve FileName into FileObject.
     *
     * @param name The name of a file on the HdfsFileSystem.
     * @return resolved FileObject.
     * @throws FileSystemException if an error occurred.
     */
    @Override
    public FileObject resolveFile(final FileName name) throws FileSystemException {
        synchronized (this) {
            if (this.fs == null) {
                final String hdfsUri = name.getRootURI();
                final HdfsFileSystemConfigBuilder builder = HdfsFileSystemConfigBuilder.getInstance();
                final FileSystemOptions options = getFileSystemOptions();
                final String[] configNames = builder.getConfigNames(options);
                final Path[] configPaths = builder.getConfigPaths(options);
                final URL[] configURLs = builder.getConfigURLs(options);
                final InputStream configStream = builder.getConfigInputStream(options);
                final Configuration configConfiguration = builder.getConfigConfiguration(options);

                final Configuration conf = new Configuration(true);
                conf.set(FileSystem.FS_DEFAULT_NAME_KEY, hdfsUri);

                // Load any alternate configuration parameters that may have been specified
                // no matter where they might come from
                if (configNames != null) {
                    for (final String configName : configNames) {
                        log.debug("Adding HDFS configuration resource: " + configName);
                        conf.addResource(configName);
                    }
                }
                if (configPaths != null) {
                    for (final Path path : configPaths) {
                        log.debug("Adding HDFS configuration path: " + path);
                        conf.addResource(path);
                    }
                }
                if (configURLs != null) {
                    for (final URL url : configURLs) {
                        log.debug("Adding HDFS configuration URL: " + url);
                        conf.addResource(url);
                    }
                }
                if (configStream != null) {
                    log.debug("Adding HDFS configuration stream");
                    conf.addResource(configStream);
                }
                if (configConfiguration != null) {
                    log.debug("Adding HDFS configuration object");
                    conf.addResource(configConfiguration);
                }

                try {
                    fs = FileSystem.get(conf);
                } catch (final IOException e) {
                    log.error("Error connecting to filesystem " + hdfsUri, e);
                    throw new FileSystemException("Error connecting to filesystem " + hdfsUri, e);
                }
            }
        }

        final boolean useCache = null != getContext().getFileSystemManager().getFilesCache();
        FileObject file;
        if (useCache) {
            file = this.getFileFromCache(name);
        } else {
            file = null;
        }
        if (null == file) {
            String path = null;
            try {
                path = URLDecoder.decode(name.getPath(), "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                path = name.getPath();
            }
            final Path filePath = new Path(path);
            file = new HdfsFileObject((AbstractFileName) name, this, fs, filePath);
            if (useCache) {
                this.putFileToCache(file);
            }
        }
        /**
         * resync the file information if requested
         */
        if (getFileSystemManager().getCacheStrategy().equals(CacheStrategy.ON_RESOLVE)) {
            file.refresh();
        }
        return file;
    }

}
