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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link FileSelector} that selects based on file extensions.
 * <p>
 * The extension comparison is case insensitive.
 * </p>
 * <p>
 * The selector makes a copy of a given Collection or array. Changing the object passed in the constructors will not
 * affect the selector.
 * </p>
 *
 * @since 2.1
 */
public class FileExtensionSelector implements FileSelector {

    /**
     * The extensions to select.
     */
    private final Set<String> extensions = new HashSet<>();

    /**
     * Creates a new selector for the given extensions.
     *
     * @param extensions The extensions to be included by this selector.
     */
    public FileExtensionSelector(final Collection<String> extensions) {
        if (extensions != null) {
            this.extensions.addAll(extensions);
        }
    }

    /**
     * Creates a new selector for the given extensions.
     *
     * @param extensions The extensions to be included by this selector.
     */
    public FileExtensionSelector(final String... extensions) {
        if (extensions != null) {
            this.extensions.addAll(Arrays.asList(extensions));
        }
    }

    /**
     * Determines if a file or folder should be selected.
     *
     * @param fileInfo The file selection information.
     * @return true if the file should be selected, false otherwise.
     */
    @Override
    public boolean includeFile(final FileSelectInfo fileInfo) {
        if (this.extensions == null) {
            return false;
        }
        for (final String extension : this.extensions) {
            if (fileInfo.getFile().getName().getExtension().equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether a folder should be traversed.
     *
     * @param fileInfo The file selection information.
     * @return true if descendants should be traversed, fase otherwise.
     */
    @Override
    public boolean traverseDescendents(final FileSelectInfo fileInfo) {
        return true;
    }
}
