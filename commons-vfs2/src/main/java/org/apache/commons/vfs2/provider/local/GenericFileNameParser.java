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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

/**
 * A general-purpose file name parser.
 */
public class GenericFileNameParser extends LocalFileNameParser {

    private static final GenericFileNameParser INSTANCE = new GenericFileNameParser();

    /**
     * Gets the singleton instance.
     *
     * @return the singleton instance.
     */
    public static GenericFileNameParser getInstance() {
        return INSTANCE;
    }

    /*
     * ... this is why why need this: here the rootFilename can only be "/" (see above) put this "/" is also in the
     * pathname so its of no value for the LocalFileName instance
     */
    @Override
    protected FileName createFileName(final String scheme, final String rootFile, final String path,
            final FileType type) {
        return new LocalFileName(scheme, "", path, type);
    }

    /**
     * Extracts the root prefix from a URI string, which has had the scheme removed.
     */
    @Override
    protected String extractRootPrefix(final String uri, final StringBuilder name) throws FileSystemException {
        // TODO - this class isn't generic at all. Need to fix this

        // Looking for <sep>
        if (StringUtils.isEmpty(name) || name.charAt(0) != '/') {
            throw new FileSystemException("vfs.provider.local/not-absolute-file-name.error", uri);
        }

        // do not strip the separator, BUT also return it ...
        return "/";
    }
}
