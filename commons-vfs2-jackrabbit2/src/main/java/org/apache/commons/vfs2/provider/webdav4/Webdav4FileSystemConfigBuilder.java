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
package org.apache.commons.vfs2.provider.webdav4;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.http4.Http4FileSystemConfigBuilder;

/**
 * Configuration options for WebDav based on HTTP4.
 *
 * @since 2.5.0
 */
public final class Webdav4FileSystemConfigBuilder extends Http4FileSystemConfigBuilder {

    /**
     * Defines whether a trailing slash ( / ) should be appended to the path.
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     *
     * @since 2.10.0
     */
    protected static final String KEY_APPEND_TRAILING_SLASH = "appendTrailingSlash";

    private static final Webdav4FileSystemConfigBuilder BUILDER = new Webdav4FileSystemConfigBuilder();

    private static final boolean DEFAULT_APPEND_TRAILING_SLASH = false;

    private static final boolean DEFAULT_FOLLOW_REDIRECT = false;

    private Webdav4FileSystemConfigBuilder() {
        super("webdav4.");
    }

    /**
     * Gets the singleton builder.
     *
     * @return the singleton builder.
     */
    public static Webdav4FileSystemConfigBuilder getInstance() {
        return BUILDER;
    }

    /**
     * @return The Webdav FileSystem Class object.
     */
    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return Webdav4FileSystem.class;
    }

    /**
     * Gets whether a trailing slash ( / ) should be appended to the path.
     *
     * @param opts The FileSystem options.
     * @return {@code true} to follow redirects, {@code false} not to.
     * @see #setAppendTrailingSlash
     *
     * @since 2.10.0
     */
    public boolean getAppendTrailingSlash(final FileSystemOptions opts) {
        return getBoolean(opts, KEY_APPEND_TRAILING_SLASH, DEFAULT_APPEND_TRAILING_SLASH);
    }

    /**
     * Return the user name to be associated with changes to the file.
     *
     * @param opts The FileSystem options
     * @return The creatorName.
     */
    public String getCreatorName(final FileSystemOptions opts) {
        return getString(opts, "creatorName");
    }

    /**
     * Gets whether to follow redirects for the connection.
     *
     * @param opts The FileSystem options.
     * @return {@code true} to follow redirects, {@code false} not to.
     * @see #setFollowRedirect
     */
    @Override
    public boolean getFollowRedirect(final FileSystemOptions opts) {
        return getBoolean(opts, KEY_FOLLOW_REDIRECT, DEFAULT_FOLLOW_REDIRECT);
    }

    /**
     * The cookies to add to the request.
     *
     * @param opts The FileSystem options.
     * @return true if versioning is enabled.
     */
    public boolean isVersioning(final FileSystemOptions opts) {
        return getBoolean(opts, "versioning", false);
    }

    /**
     * Sets whether a trailing slash ( / ) should be appended to the path.
     *
     * @param opts The FileSystem options.
     * @param appendTrailingSlash {@code true} to append slash, {@code false} not to.
     *
     * @since 2.10.0
     */
    public void setAppendTrailingSlash(final FileSystemOptions opts, final boolean appendTrailingSlash) {
        setParam(opts, KEY_APPEND_TRAILING_SLASH, appendTrailingSlash);
    }

    /**
     * The user name to be associated with changes to the file.
     *
     * @param opts The FileSystem options
     * @param creatorName The creator name to be associated with the file.
     */
    public void setCreatorName(final FileSystemOptions opts, final String creatorName) {
        setParam(opts, "creatorName", creatorName);
    }

    /**
     * Whether to use versioning.
     *
     * @param opts The FileSystem options.
     * @param versioning true if versioning should be enabled.
     */
    public void setVersioning(final FileSystemOptions opts, final boolean versioning) {
        setParam(opts, "versioning", Boolean.valueOf(versioning));
    }
}
