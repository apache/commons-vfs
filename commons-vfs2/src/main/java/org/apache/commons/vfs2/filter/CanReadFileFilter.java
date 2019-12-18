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
 * This filter accepts {@code File}s that can be read.
 * <p>
 * Example, showing how to print out a list of the current directory's
 * <i>readable</i> files:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * FileObject[] files = dir.findFiles(new FileFilterSelector(CanReadFileFilter.CAN_READ));
 * for (int i = 0; i &lt; files.length; i++) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * <p>
 * Example, showing how to print out a list of the current directory's
 * <i>un-readable</i> files:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * FileObject[] files = dir.findFiles(new FileFilterSelector(CanReadFileFilter.CANNOT_READ));
 * for (int i = 0; i &lt; files.length; i++) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * <p>
 * Example, showing how to print out a list of the current directory's
 * <i>read-only</i> files:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * FileObject[] files = dir.findFiles(new FileFilterSelector(CanReadFileFilter.READ_ONLY));
 * for (int i = 0; i &lt; files.length; i++) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @author This code was originally ported from Apache Commons IO File Filter
 * @see "http://commons.apache.org/proper/commons-io/"
 * @since 2.4
 */
public class CanReadFileFilter implements FileFilter, Serializable {

    /** Singleton instance of <i>readable</i> filter. */
    public static final FileFilter CAN_READ = new CanReadFileFilter();

    /** Singleton instance of not <i>readable</i> filter. */
    public static final FileFilter CANNOT_READ = new NotFileFilter(CAN_READ);

    /** Singleton instance of <i>read-only</i> filter. */
    public static final FileFilter READ_ONLY = new AndFileFilter(CAN_READ, CanWriteFileFilter.CANNOT_WRITE);

    private static final long serialVersionUID = 1L;

    /**
     * Restrictive constructor.
     */
    protected CanReadFileFilter() {
    }

    /**
     * Checks to see if the file can be read.
     *
     * @param fileInfo the File to check.
     *
     * @return {@code true} if the file can be read, otherwise {@code false}.
     * @throws FileSystemException Thrown for file system errors.
     */
    @Override
    public boolean accept(final FileSelectInfo fileInfo) throws FileSystemException {
        return fileInfo.getFile().isReadable();
    }

}
