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
import org.apache.commons.vfs2.FileType;

/**
 * This filter accepts files or directories that are empty.
 * <p>
 * If the {@code File} is a directory it checks that it contains no files.
 * </p>
 * <p>
 * Example, showing how to print out a list of the current directory's empty files/directories:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * FileObject[] files = dir.findFiles(new FileFilterSelector(EmptyFileFilter.EMPTY));
 * for (int i = 0; i &lt; files.length; i++) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * <p>
 * Example, showing how to print out a list of the current directory's non-empty files/directories:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * FileObject[] files = dir.findFiles(new FileFilterSelector(EmptyFileFilter.NOT_EMPTY));
 * for (int i = 0; i &lt; files.length; i++) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @author This code was originally ported from Apache Commons IO File Filter
 * @see "http://commons.apache.org/proper/commons-io/"
 * @since 2.4
 */
public class EmptyFileFilter implements FileFilter, Serializable {

    private static final long serialVersionUID = 1L;

    /** Singleton instance of <i>empty</i> filter. */
    public static final FileFilter EMPTY = new EmptyFileFilter();

    /** Singleton instance of <i>not-empty</i> filter. */
    public static final FileFilter NOT_EMPTY = new NotFileFilter(EMPTY);

    /**
     * Restrictive constructor.
     */
    protected EmptyFileFilter() {
    }

    /**
     * Checks to see if the file is empty. A non-existing file is also considered empty.
     *
     * @param fileInfo the file or directory to check
     *
     * @return {@code true} if the file or directory is <i>empty</i>, otherwise {@code false}.
     * @throws FileSystemException Thrown for file system errors.
     */
    @Override
    public boolean accept(final FileSelectInfo fileInfo) throws FileSystemException {
        try (final FileObject file = fileInfo.getFile()) {
            if (!file.exists()) {
                return true;
            }
            if (file.getType() == FileType.FOLDER) {
                final FileObject[] files = file.getChildren();
                return files == null || files.length == 0;
            }
            try (final FileContent content = file.getContent();) {
                return content.isEmpty();
            }
        }
    }

}
