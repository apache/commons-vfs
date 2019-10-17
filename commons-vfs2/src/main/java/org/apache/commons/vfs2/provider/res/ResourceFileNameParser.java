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
import org.apache.commons.vfs2.provider.local.GenericFileNameParser;

/**
 * Slightly modified filename parser for resource URIs.
 */
public class ResourceFileNameParser extends GenericFileNameParser {

    private static final ResourceFileNameParser INSTANCE = new ResourceFileNameParser();

    /**
     * retrieve a instance to this parser.
     *
     * @return the parser
     */
    public static GenericFileNameParser getInstance() {
        return INSTANCE;
    }

    @Override
    protected String extractRootPrefix(final String uri, final StringBuilder name) throws FileSystemException {
        // Resource URI (as used by ClassLoader.getResource()) are assumed to be absolute despite
        // lacking a leading '/'. All leading '/' will be stripped from the name.

        int index = 0;
        while (index < name.length() && name.charAt(index) == '/') {
            ++index;
        }
        if (index > 0) {
            name.delete(0, index);
        }

        if (name.length() == 0) {
            throw new FileSystemException("vfs.provider.res/not-valid-resource-location.error", uri);
        }

        return "/";
    }

    @Override
    protected FileName createFileName(final String scheme, final String rootFile, final String path,
            final FileType type) {
        return new ResourceFileName(scheme, path, type);
    }
}
