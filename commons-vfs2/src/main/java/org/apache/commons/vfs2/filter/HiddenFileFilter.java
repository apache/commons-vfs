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

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;

/**
 * This filter accepts {@code File}s that are hidden.
 * <p>
 * Example, showing how to print out a list of the current directory's
 * <i>hidden</i> files:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * FileObject[] files = dir.findFiles(new FileFilterSelector(HiddenFileFilter.HIDDEN));
 * for (int i = 0; i &lt; files.length; i++) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * <p>
 * Example, showing how to print out a list of the current directory's
 * <i>visible</i> (i.e. not hidden) files:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * FileObject[] files = dir.findFiles(new FileFilterSelector(HiddenFileFilter.VISIBLE));
 * for (int i = 0; i &lt; files.length; i++) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @author This code was originally ported from Apache Commons IO File Filter
 * @see "http://commons.apache.org/proper/commons-io/"
 * @since 2.4
 */
public class HiddenFileFilter implements FileFilter, Serializable {

    private static final long serialVersionUID = 1L;

    /** Singleton instance of <i>hidden</i> filter. */
    public static final FileFilter HIDDEN = new HiddenFileFilter();

    /** Singleton instance of <i>visible</i> filter. */
    public static final FileFilter VISIBLE = new NotFileFilter(HIDDEN);

    /**
     * Restrictive constructor.
     */
    protected HiddenFileFilter() {
    }

    /**
     * Checks to see if the file is hidden. Non existing files won't be accepted.
     *
     * @param fileInfo the File to check
     *
     * @return {@code true} if the file is <i>hidden</i>, otherwise {@code false}.
     * @throws FileSystemException Thrown for file system errors.
     */
    @Override
    public boolean accept(final FileSelectInfo fileInfo) throws FileSystemException {
        if (!fileInfo.getFile().exists()) {
            return false;
        }
        return fileInfo.getFile().isHidden();
    }

}
