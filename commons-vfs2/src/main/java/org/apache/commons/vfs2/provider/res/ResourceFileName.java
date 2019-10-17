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
package org.apache.commons.vfs2.provider.res;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.UriParser;

/**
 * A resource file URI.
 */
public class ResourceFileName extends AbstractFileName {

    protected ResourceFileName(final String scheme, final String path, final FileType type) {
        super(scheme, path, type);
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
        return new ResourceFileName(getScheme(), path, type);
    }

    /**
     * Returns the decoded URI of the file.
     *
     * @return the FileName as a URI.
     */
    @Override
    public String toString() {
        try {
            return UriParser.decode(super.getURI());
        } catch (final FileSystemException e) {
            return super.getURI();
        }
    }

    /**
     * Builds the root URI for this file name.
     */
    @Override
    protected void appendRootUri(final StringBuilder buffer, final boolean addPassword) {
        buffer.append(getScheme());
        buffer.append(":");
    }

    @Override
    public String getRootURI() {
        // resource URIs have a blank root.
        return "";
    }
}
