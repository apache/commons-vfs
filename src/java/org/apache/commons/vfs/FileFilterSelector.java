/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs;

import org.apache.commons.vfs.util.Messages;

/**
 * A {@link org.apache.commons.vfs.FileSelector} that selects all children of the given fileObject.<br />
 * This is to mimic the {@link java.io.FileFilter} interface
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 */
public class FileFilterSelector extends FileDepthSelector
{
    private FileFilter fileFilter;

    public FileFilterSelector()
    {
        super(1, 1);
    }

    public FileFilterSelector(FileFilter fileFilter)
    {
        this();
        this.fileFilter = fileFilter;
    }

    /**
     * Determines if a file or folder should be selected.
     */
    public boolean includeFile(final FileSelectInfo fileInfo)
    {
        if (!super.includeFile(fileInfo))
        {
            return false;
        }

        return accept(fileInfo);
    }

    public boolean accept(final FileSelectInfo fileInfo)
    {
        if (fileFilter != null)
        {
            return fileFilter.accept(fileInfo);
        }

        throw new IllegalArgumentException(Messages.getString("vfs.selectors/filefilter.missing.error"));
    }
}
