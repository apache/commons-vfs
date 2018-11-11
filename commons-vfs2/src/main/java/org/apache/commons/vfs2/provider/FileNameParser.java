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
package org.apache.commons.vfs2.provider;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;

/**
 * Provides methods to parse a filename into a {@link org.apache.commons.vfs2.FileName}.
 */
public interface FileNameParser {
    /**
     * Check if a character needs encoding (%nn).
     *
     * @param ch the character
     * @return true if character should be encoded
     */
    boolean encodeCharacter(char ch);

    /**
     * parses a String into a filename.
     *
     * @param context The component context.
     * @param base The base FileName.
     * @param uri The target file name.
     * @return A FileName that represents the taret file.
     * @throws FileSystemException if an error occurs parsing the URI.
     */
    FileName parseUri(final VfsComponentContext context, final FileName base, final String uri)
            throws FileSystemException;
}
