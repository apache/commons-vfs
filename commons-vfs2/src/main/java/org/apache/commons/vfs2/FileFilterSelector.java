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

import org.apache.commons.vfs2.util.Messages;

/**
 * A {@link org.apache.commons.vfs2.FileSelector} that selects all children of the given fileObject.
 * <p>
 * This is to mimic the {@link java.io.FileFilter} interface.
 */
public class FileFilterSelector extends FileDepthSelector {
    /**
     * The FileFilter.
     */
    private final FileFilter fileFilter;

    public FileFilterSelector() {
        this(null);
    }

    public FileFilterSelector(final FileFilter fileFilter) {
        super(1, 1);
        this.fileFilter = fileFilter;
    }

    /**
     * Determines if a file or folder should be selected.
     *
     * @param fileInfo The file selection information.
     * @return true if the file or folder should be included, false otherwise.
     */
    @Override
    public boolean includeFile(final FileSelectInfo fileInfo) {
        if (!super.includeFile(fileInfo)) {
            return false;
        }

        return accept(fileInfo);
    }

    /**
     * Determines whether the file should be selected.
     *
     * @param fileInfo The file selection information.
     * @return true if the file should be selected, false otherwise.
     */
    public boolean accept(final FileSelectInfo fileInfo) {
        if (fileFilter != null) {
            return fileFilter.accept(fileInfo);
        }

        throw new IllegalArgumentException(Messages.getString("vfs.selectors/filefilter.missing.error"));
    }
}
