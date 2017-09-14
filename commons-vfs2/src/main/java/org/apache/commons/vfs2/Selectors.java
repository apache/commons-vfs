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
package org.apache.commons.vfs2;

/**
 * Several standard file selectors.
 */
public final class Selectors {
    /**
     * A {@link FileSelector} that selects only the base file/folder.
     */
    public static final FileSelector SELECT_SELF = new FileDepthSelector();

    /**
     * A {@link FileSelector} that selects the base file/folder and its direct children.
     */
    public static final FileSelector SELECT_SELF_AND_CHILDREN = new FileDepthSelector(0, 1);

    /**
     * A {@link FileSelector} that selects only the direct children of the base folder.
     */
    public static final FileSelector SELECT_CHILDREN = new FileDepthSelector(1);

    /**
     * A {@link FileSelector} that selects all the descendants of the base folder, but does not select the base folder
     * itself.
     */
    public static final FileSelector EXCLUDE_SELF = new FileDepthSelector(1, Integer.MAX_VALUE);

    /**
     * A {@link FileSelector} that only files (not folders).
     */
    public static final FileSelector SELECT_FILES = new FileTypeSelector(FileType.FILE);

    /**
     * A {@link FileSelector} that only folders (not files).
     */
    public static final FileSelector SELECT_FOLDERS = new FileTypeSelector(FileType.FOLDER);

    /**
     * A {@link FileSelector} that selects the base file/folder, plus all its descendants.
     */
    public static final FileSelector SELECT_ALL = new AllFileSelector();

    /**
     * Prevent the class from being instantiated.
     */
    private Selectors() {
    }
}
