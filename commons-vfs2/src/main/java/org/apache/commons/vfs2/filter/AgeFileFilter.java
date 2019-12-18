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
import java.util.Date;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;

/**
 * Filters files based on a cutoff time, can filter either newer files or files
 * equal to or older.
 * <p>
 * For example, to print all files and directories in the current directory
 * older than one day:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * // We are interested in files older than one day
 * long cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
 * AgeFileFilter filter = new AgeFileFilter(cutoff);
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
public class AgeFileFilter implements FileFilter, Serializable {

    private static final long serialVersionUID = 1L;

    /** Whether the files accepted will be older or newer. */
    private final boolean acceptOlder;

    /** The cutoff time threshold. */
    private final long cutoff;

    /**
     * Tests if the specified {@code File} is newer than the specified time
     * reference.
     *
     * @param fileObject the {@code File} of which the modification date must
     *                   be compared, must not be {@code null}
     * @param timeMillis the time reference measured in milliseconds since the epoch
     *                   (00:00:00 GMT, January 1, 1970)
     * @return true if the {@code File} exists and has been modified after the
     *         given time reference.
     * @throws FileSystemException Thrown for file system errors.
     * @throws IllegalArgumentException if the file is {@code null}
     */
    private static boolean isFileNewer(final FileObject fileObject, final long timeMillis) throws FileSystemException {
        if (fileObject == null) {
            throw new IllegalArgumentException("No specified file");
        }
        if (!fileObject.exists()) {
            return false;
        }
        try (final FileContent content = fileObject.getContent()) {
            final long lastModified = content.getLastModifiedTime();
            return lastModified > timeMillis;
        }
    }

    /**
     * Constructs a new age file filter for files older than (at or before) a
     * certain cutoff date.
     *
     * @param cutoffDate the threshold age of the files
     */
    public AgeFileFilter(final Date cutoffDate) {
        this(cutoffDate, true);
    }

    /**
     * Constructs a new age file filter for files on any one side of a certain
     * cutoff date.
     *
     * @param cutoffDate  the threshold age of the files
     * @param acceptOlder if true, older files (at or before the cutoff) are
     *                    accepted, else newer ones (after the cutoff).
     */
    public AgeFileFilter(final Date cutoffDate, final boolean acceptOlder) {
        this(cutoffDate.getTime(), acceptOlder);
    }

    /**
     * Constructs a new age file filter for files older than (at or before) a
     * certain File (whose last modification time will be used as reference).
     *
     * @param cutoffReference the file whose last modification time is usesd as the
     *                        threshold age of the files
     *
     * @throws FileSystemException Error reading the last modification time from the
     *                             reference file object.
     */
    public AgeFileFilter(final FileObject cutoffReference) throws FileSystemException {
        this(cutoffReference, true);
    }

    /**
     * Constructs a new age file filter for files on any one side of a certain File
     * (whose last modification time will be used as reference).
     *
     * @param cutoffReference the file whose last modification time is usesd as the
     *                        threshold age of the files
     * @param acceptOlder     if true, older files (at or before the cutoff) are
     *                        accepted, else newer ones (after the cutoff).
     *
     * @throws FileSystemException Error reading the last modification time from the
     *                             reference file object.
     */
    public AgeFileFilter(final FileObject cutoffReference, final boolean acceptOlder) throws FileSystemException {
        this(cutoffReference.getContent().getLastModifiedTime(), acceptOlder);
    }

    /**
     * Constructs a new age file filter for files equal to or older than a certain
     * cutoff.
     *
     * @param cutoff the threshold age of the files
     */
    public AgeFileFilter(final long cutoff) {
        this(cutoff, true);
    }

    /**
     * Constructs a new age file filter for files on any one side of a certain
     * cutoff.
     *
     * @param cutoff      the threshold age of the files
     * @param acceptOlder if true, older files (at or before the cutoff) are
     *                    accepted, else newer ones (after the cutoff).
     */
    public AgeFileFilter(final long cutoff, final boolean acceptOlder) {
        this.acceptOlder = acceptOlder;
        this.cutoff = cutoff;
    }

    /**
     * Checks to see if the last modification of the file matches cutoff favorably.
     * <p>
     * If last modification time equals cutoff and newer files are required, file
     * <b>IS NOT</b> selected. If last modification time equals cutoff and older
     * files are required, file <b>IS</b> selected.
     * </p>
     *
     * @param fileInfo the File to check
     *
     * @return true if the file name matches
     * @throws FileSystemException Thrown for file system errors.
     */
    @Override
    public boolean accept(final FileSelectInfo fileInfo) throws FileSystemException {
        final boolean newer = isFileNewer(fileInfo.getFile(), cutoff);
        return acceptOlder ? !newer : newer;
    }

    /**
     * Provide a String representaion of this file filter.
     *
     * @return a String representaion
     */
    @Override
    public String toString() {
        final String condition = acceptOlder ? "<=" : ">";
        return super.toString() + "(" + condition + cutoff + ")";
    }
}
