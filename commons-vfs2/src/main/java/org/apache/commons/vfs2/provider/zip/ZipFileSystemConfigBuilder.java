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

import java.nio.charset.Charset;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

public class ZipFileSystemConfigBuilder extends FileSystemConfigBuilder {

    private static final String _PREFIX = ZipFileSystemConfigBuilder.class.getName();
    private static final ZipFileSystemConfigBuilder INSTANCE = new ZipFileSystemConfigBuilder();
    private static final String KEY_CHARSET = _PREFIX + ".charset";

    public static final ZipFileSystemConfigBuilder getInstance() {
        return INSTANCE;
    }

    private ZipFileSystemConfigBuilder() {
        super("zip.");
    }

    public Charset getCharset(final FileSystemOptions opts) {
        return (Charset) getParam(opts, KEY_CHARSET);
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return ZipFileSystem.class;
    }

    public void setCharset(final FileSystemOptions opts, final Charset charset) {
        setParam(opts, KEY_CHARSET, charset);
    }

}
