/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.impl;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;

/**
 * A simple file name to hold the scheme for to be created virtual file system.
 */
public class VirtualFileName extends AbstractFileName {

    /**
     * Constructs a new instance.
     *
     * @param scheme The scheme.
     * @param absolutePath the absolute path, maybe empty or null.
     * @param type the file type.
     */
    public VirtualFileName(final String scheme, final String absolutePath, final FileType type) {
        super(scheme, absolutePath, type);
    }

    @Override
    protected void appendRootUri(final StringBuilder buffer, final boolean addPassword) {
        buffer.append(getScheme());
    }

    @Override
    public FileName createName(final String absolutePath, final FileType type) {
        return new VirtualFileName(getScheme(), absolutePath, type);
    }
}
