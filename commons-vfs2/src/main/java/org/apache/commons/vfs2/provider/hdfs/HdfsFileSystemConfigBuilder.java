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
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

/**
 * Configuration settings for the HdfsFileSystem.
 *
 * @since 2.1
 */
public final class HdfsFileSystemConfigBuilder extends FileSystemConfigBuilder {
    private static final HdfsFileSystemConfigBuilder BUILDER = new HdfsFileSystemConfigBuilder();
    private static final String KEY_CONFIG_NAMES = "configNames";
    private static final String KEY_CONFIG_PATHS = "configPaths";
    private static final String KEY_CONFIG_URLS = "configURLs";
    private static final String KEY_CONFIG_STREAM = "configStream";
    private static final String KEY_CONFIG_CONF = "configConf";

    private HdfsFileSystemConfigBuilder() {
        super("hdfs.");
    }

    /**
     * @return HdfsFileSystemConfigBuilder instance
     */
    public static HdfsFileSystemConfigBuilder getInstance() {
        return BUILDER;
    }

    /**
     * @return HDFSFileSystem
     */
    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return HdfsFileSystem.class;
    }

    /**
     * Get names of alternate configuration resources.
     *
     * @return resource name list of alternate configurations or {@code null}.
     * @param opts The FileSystemOptions.
     * @see #setConfigName(FileSystemOptions, String)
     */
    public String[] getConfigNames(final FileSystemOptions opts) {
        final String names = this.getString(opts, KEY_CONFIG_NAMES);
        return names == null || names.isEmpty() ? null : names.split(",");
    }

    /**
     * Sets the name of configuration resource to be loaded after the defaults.
     * <p>
     * Specifies the name of a config resource to override any specific HDFS settings. The property will be passed on to
     * {@code org.apache.hadoop.conf.Configuration#addResource(String)} after the URL was set as the default name with:
     * {@code Configuration#set(FileSystem.FS_DEFAULT_NAME_KEY, url)}.
     * <p>
     * One use for this is to set a different value for the {@code dfs.client.use.datanode.hostname} property in order
     * to access HDFS files stored in an AWS installation (from outside their firewall). There are other possible uses
     * too.
     * <p>
     * This method may be called multiple times and all the specified resources will be loaded in the order they were
     * specified.
     * <p>
     * Note also, that if a list of names is provided, separated by commas ({@code ","}), that this will work the same
     * as calling this method a number of times with just one name each.
     *
     * @param opts The FileSystemOptions to modify.
     * @param name resource name of additional configuration or {@code null} to unset all the values set so far.
     * @see #getConfigNames
     */
    public void setConfigName(final FileSystemOptions opts, final String name) {
        if (name == null || name.isEmpty()) {
            this.setParam(opts, KEY_CONFIG_NAMES, null);
        } else {
            final String previousNames = this.getString(opts, KEY_CONFIG_NAMES);
            if (previousNames == null || previousNames.isEmpty()) {
                this.setParam(opts, KEY_CONFIG_NAMES, name);
            } else {
                this.setParam(opts, KEY_CONFIG_NAMES, previousNames + "," + name);
            }
        }
    }

    /**
     * Get paths of alternate configuration file system files.
     *
     * @return list of full paths of alternate configuration files or {@code null}.
     * @param opts The FileSystemOptions.
     * @see #setConfigPath(FileSystemOptions, Path)
     */
    public Path[] getConfigPaths(final FileSystemOptions opts) {
        final String pathNames = this.getString(opts, KEY_CONFIG_PATHS);
        if (pathNames == null || pathNames.isEmpty()) {
            return null;
        }
        final String[] paths = pathNames.split(",");
        final Path[] realPaths = new Path[paths.length];
        for (int i = 0; i < paths.length; i++) {
            realPaths[i] = new Path(paths[i]);
        }
        return realPaths;
    }

    /**
     * Sets the full path of configuration file to be loaded after the defaults.
     * <p>
     * Specifies the path of a local file system config file to override any specific HDFS settings. The property will
     * be passed on to {@code org.apache.hadoop.conf.Configuration#addResource(Path)} after the URL was set as the
     * default name with: {@code Configuration#set(FileSystem.FS_DEFAULT_NAME_KEY, url)}.
     * <p>
     * One use for this is to set a different value for the {@code dfs.client.use.datanode.hostname} property in order
     * to access HDFS files stored in an AWS installation (from outside their firewall). There are other possible uses
     * too.
     * <p>
     * This method may be called multiple times and all the specified resources will be loaded in the order they were
     * specified.
     *
     * @param opts The FileSystemOptions to modify.
     * @param path full path of additional configuration file (local file system) or {@code null} to unset all the path
     *            values set so far.
     */
    public void setConfigPath(final FileSystemOptions opts, final Path path) {
        if (path == null) {
            this.setParam(opts, KEY_CONFIG_PATHS, null);
        } else {
            final String previousPathNames = this.getString(opts, KEY_CONFIG_PATHS);
            if (previousPathNames == null || previousPathNames.isEmpty()) {
                this.setParam(opts, KEY_CONFIG_PATHS, path.toString());
            } else {
                this.setParam(opts, KEY_CONFIG_PATHS, previousPathNames + "," + path.toString());
            }
        }
    }

    /**
     * Get URLs of alternate configurations.
     *
     * @return list of alternate configuration URLs or {@code null}.
     * @param opts The FileSystemOptions.
     * @see #setConfigURL(FileSystemOptions, URL)
     */
    public URL[] getConfigURLs(final FileSystemOptions opts) {
        try {
            final String urlNames = this.getString(opts, KEY_CONFIG_URLS);
            if (urlNames == null || urlNames.isEmpty()) {
                return null;
            }
            final String[] urls = urlNames.split(",");
            final URL[] realURLs = new URL[urls.length];
            for (int i = 0; i < urls.length; i++) {
                realURLs[i] = new URL(urls[i]);
            }
            return realURLs;
        } catch (final MalformedURLException mue) {
            // This should never happen because we save it in the proper form
        }
        return null;
    }

    /**
     * Sets the URL of configuration file to be loaded after the defaults.
     * <p>
     * Specifies the URL of a config file to override any specific HDFS settings. The property will be passed on to
     * {@code org.apache.hadoop.conf.Configuration#addResource(URL)} after the URL was set as the default name with:
     * {@code Configuration#set(FileSystem.FS_DEFAULT_NAME_KEY, url)}.
     * <p>
     * One use for this is to set a different value for the {@code dfs.client.use.datanode.hostname} property in order
     * to access HDFS files stored in an AWS installation (from outside their firewall). There are other possible uses
     * too.
     * <p>
     * This method may be called multiple times and all the specified resources will be loaded in the order they were
     * specified.
     *
     * @param opts The FileSystemOptions to modify.
     * @param url URL of additional configuration file or {@code null} to unset all the URL values set so far.
     */
    public void setConfigURL(final FileSystemOptions opts, final URL url) {
        if (url == null) {
            this.setParam(opts, KEY_CONFIG_URLS, null);
        } else {
            final String previousURLNames = this.getString(opts, KEY_CONFIG_URLS);
            if (previousURLNames == null || previousURLNames.isEmpty()) {
                this.setParam(opts, KEY_CONFIG_URLS, url.toString());
            } else {
                this.setParam(opts, KEY_CONFIG_URLS, previousURLNames + "," + url.toString());
            }
        }
    }

    /**
     * Get alternate configuration input stream.
     *
     * @return alternate configuration input stream or {@code null}.
     * @param opts The FileSystemOptions.
     * @see #setConfigInputStream(FileSystemOptions, InputStream)
     */
    public InputStream getConfigInputStream(final FileSystemOptions opts) {
        return (InputStream) this.getParam(opts, KEY_CONFIG_STREAM);
    }

    /**
     * Sets the input stream of configuration file to be loaded after the defaults.
     * <p>
     * Specifies an input stream connected to a config file to override any specific HDFS settings. The property will be
     * passed on to {@code org.apache.hadoop.conf.Configuration#addResource(InputStream)} after the URL was set as the
     * default name with: {@code Configuration#set(FileSystem.FS_DEFAULT_NAME_KEY, url)}.
     * <p>
     * One use for this is to set a different value for the {@code dfs.client.use.datanode.hostname} property in order
     * to access HDFS files stored in an AWS installation (from outside their firewall). There are other possible uses
     * too.
     *
     * @param opts The FileSystemOptions to modify.
     * @param inputStream input stream of additional configuration file or {@code null} to unset the configuration input
     *            stream previously set up.
     */
    public void setConfigInputStream(final FileSystemOptions opts, final InputStream inputStream) {
        this.setParam(opts, KEY_CONFIG_STREAM, inputStream);
    }

    /**
     * Get alternate configuration object.
     *
     * @return alternate configuration object or {@code null}.
     * @param opts The FileSystemOptions.
     * @see #setConfigConfiguration(FileSystemOptions, Configuration)
     */
    public Configuration getConfigConfiguration(final FileSystemOptions opts) {
        return (Configuration) this.getParam(opts, KEY_CONFIG_CONF);
    }

    /**
     * Sets the configuration object to be loaded after the defaults.
     * <p>
     * Specifies an already initialized configuration object to override any specific HDFS settings. The property will
     * be passed on to {@code org.apache.hadoop.conf.Configuration#addResource(Configuration)} after the URL was set as
     * the default name with: {@code Configuration#set(FileSystem.FS_DEFAULT_NAME_KEY, url)}.
     * <p>
     * One use for this is to set a different value for the {@code dfs.client.use.datanode.hostname} property in order
     * to access HDFS files stored in an AWS installation (from outside their firewall). There are other possible uses
     * too.
     *
     * @param opts The FileSystemOptions to modify.
     * @param configuration additional configuration object or {@code null} to unset any configuration object previously
     *            set.
     */
    public void setConfigConfiguration(final FileSystemOptions opts, final Configuration configuration) {
        this.setParam(opts, KEY_CONFIG_CONF, configuration);
    }

}
