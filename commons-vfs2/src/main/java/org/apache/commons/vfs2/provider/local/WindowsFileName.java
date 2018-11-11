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
package org.apache.commons.vfs2.provider.local;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;

/**
 * A local file URI.
 */
public class WindowsFileName extends LocalFileName {
    protected WindowsFileName(final String scheme, final String rootFile, final String path, final FileType type) {
        super(scheme, rootFile, path, type);
    }

    /**
     * Factory method for creating name instances.
     *
     * @param path The file path.
     * @param type The file type.
     * @return The FileName.
     */
    @Override
    public FileName createName(final String path, final FileType type) {
        return new WindowsFileName(getScheme(), getRootFile(), path, type);
    }

    /**
     * Builds the root URI for this file name.
     */
    @Override
    protected void appendRootUri(final StringBuilder buffer, final boolean addPassword) {
        buffer.append(getScheme());
        buffer.append("://");
        if (getRootFile() != null && !getRootFile().startsWith("/")) {
            // next is drive-letter (else unc name)
            buffer.append("/");
        }
        buffer.append(getRootFile());
    }
}
