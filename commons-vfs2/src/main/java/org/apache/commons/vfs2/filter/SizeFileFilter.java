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
package org.apache.commons.vfs2.filter;

import java.io.Serializable;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;

/**
 * Filters files based on size, can filter either smaller files or files equal
 * to or larger than a given threshold.
 * <p>
 * For example, to print all files and directories in the current directory
 * whose size is greater than 1 MB:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * SizeFileFilter filter = new SizeFileFilter(1024 * 1024);
 * FileObject[] files = dir.findFiles(new FileFilterSelector(filter));
 * for (int i = 0; i &lt; files.length; i++) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @author This code was originally ported from Apache Commons IO File Filter
 * @see "http://commons.apache.org/proper/commons-io/"
 * @since 2.4
 */
public class SizeFileFilter implements FileFilter, Serializable {

    private static final long serialVersionUID = 1L;

    /** Whether the files accepted will be larger or smaller. */
    private final boolean acceptLarger;

    /** The size threshold. */
    private final long size;

    /**
     * Constructs a new size file filter for files equal to or larger than a certain
     * size.
     *
     * @param size the threshold size of the files - Must be non-negative.
     */
    public SizeFileFilter(final long size) {
        this(size, true);
    }

    /**
     * Constructs a new size file filter for files based on a certain size
     * threshold.
     *
     * @param size         the threshold size of the files - Must be non-negative.
     * @param acceptLarger if true, files equal to or larger are accepted, otherwise
     *                     smaller ones (but not equal to)
     */
    public SizeFileFilter(final long size, final boolean acceptLarger) {
        if (size < 0) {
            throw new IllegalArgumentException("The size must be non-negative");
        }
        this.size = size;
        this.acceptLarger = acceptLarger;
    }

    /**
     * Checks to see if the size of the file is favorable.
     * <p>
     * If size equals threshold and smaller files are required, file <b>IS NOT</b>
     * selected. If size equals threshold and larger files are required, file
     * <b>IS</b> selected.
     * </p>
     * <p>
     * Non-existing files return always false (will never be accepted).
     * </p>
     *
     * @param fileInfo the File to check
     *
     * @return true if the file name matches
     * @throws FileSystemException Thrown for file system errors.
     */
    @Override
    public boolean accept(final FileSelectInfo fileInfo) throws FileSystemException {
        try (final FileObject file = fileInfo.getFile()) {
            if (!file.exists()) {
                return false;
            }
            try (final FileContent content = file.getContent();) {
                final long length = content.getSize();
                final boolean smaller = length < size;
                return acceptLarger ? !smaller : smaller;
            }
        }
    }

    /**
     * Provide a String representation of this file filter.
     *
     * @return a String representation
     */
    @Override
    public String toString() {
        final String condition = acceptLarger ? ">=" : "<";
        return super.toString() + "(" + condition + size + ")";
    }

}
