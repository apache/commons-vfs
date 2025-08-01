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
package org.apache.commons.vfs2.cache;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.provider.AbstractVfsComponent;

/**
 * Abstracts {@link FilesCache} implementations.
 */
public abstract class AbstractFilesCache extends AbstractVfsComponent implements FilesCache {

    /**
     * Constructs a new instance for subclasses.
     */
    public AbstractFilesCache() {
        // empty
    }

    /**
     * Default implementation is a NOOP.
     *
     * @param file touch this file.
     */
    public void touchFile(final FileObject file) {
        // empty
    }
}
