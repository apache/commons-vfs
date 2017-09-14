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

import java.io.Closeable;
import java.net.URL;
import java.util.List;

import org.apache.commons.vfs2.operations.FileOperations;

/**
 * Represents a file, and is used to access the content and structure of the file.
 * <p>
 * Files are arranged in a hierarchy. Each hierarchy forms a <i>file system</i>. A file system represents things like a
 * local OS file system, a windows share, an HTTP server, or the contents of a Zip file.
 * <p>
 * There are two types of files: <i>Folders</i>, which contain other files, and <i>normal files</i>, which contain data,
 * or <i>content</i>. A folder may not have any content, and a normal file cannot contain other files.
 *
 * <h2>File Naming</h2>
 *
 * TODO - write this.
 *
 * <h2>Reading and Writing a File</h2>
 *
 * Reading and writing a file, and all other operations on the file's <i>content</i>, is done using the
 * {@link FileContent} object returned by {@link #getContent}.
 *
 * <h2>Creating and Deleting a File</h2>
 *
 * A file is created using either {@link #createFolder}, {@link #createFile}, or by writing to the file using one of the
 * {@link FileContent} methods.
 * <p>
 * A file is deleted using {@link #delete}. Recursive deletion can be done using {@link #delete(FileSelector)}.
 *
 * <h2>Finding Files</h2>
 *
 * Other files in the <i>same</i> file system as this file can be found using:
 * <ul>
 * <li>{@link #findFiles} to find a set of matching descendants in in the same file system.</li>
 * <li>{@link #getChildren} and {@link #getChild} to find the children of this file.</li>
 * <li>{@link #getParent} to find the folder containing this file.</li>
 * <li>{@link #getFileSystem} to find another file in the same file system.</li>
 * <li>{@link #resolveFile} to find another file relative to this file.</li>
 * </ul>
 * To find files in another file system, use a {@link FileSystemManager}.
 *
 * <h2>Iterating Files</h2>
 *
 * You can iterate over a FileObject using the Java "foreach" statement, which provides all descendants of a File
 * Object.
 *
 * <h2>Sorting Files</h2>
 *
 * Files may be sorted using {@link java.util.Arrays#sort(Object[]) Arrays.sort()} and
 * {@link java.util.Collections#sort(List) Collections.sort()}.
 *
 * @see FileSystemManager
 * @see FileContent
 * @see FileName
 */
public interface FileObject extends Comparable<FileObject>, Iterable<FileObject>, Closeable {
    /**
     * Queries the file if it is possible to rename it to newfile.
     *
     * @param newfile the new file(-name)
     * @return true it this is the case
     */
    boolean canRenameTo(FileObject newfile);

    /**
     * Closes this file, and its content. This method is a hint to the implementation that it can release any resources
     * associated with the file.
     * <p>
     * The file object can continue to be used after this method is called.
     * </p>
     *
     * @throws FileSystemException On error closing the file.
     * @see FileContent#close
     */
    @Override
    void close() throws FileSystemException;

    /**
     * Copies another file, and all its descendants, to this file.
     * <p>
     * If this file does not exist, it is created. Its parent folder is also created, if necessary. If this file does
     * exist, it is deleted first.
     * </p>
     * <p>
     * This method is not transactional. If it fails and throws an exception, this file will potentially only be
     * partially copied.
     * </p>
     *
     * @param srcFile The source file to copy.
     * @param selector The selector to use to select which files to copy.
     * @throws FileSystemException If this file is read-only, or if the source file does not exist, or on error copying
     *             the file.
     */
    void copyFrom(FileObject srcFile, FileSelector selector) throws FileSystemException;

    /**
     * Creates this file, if it does not exist. Also creates any ancestor folders which do not exist. This method does
     * nothing if the file already exists and is a file.
     *
     * @throws FileSystemException If the file already exists with the wrong type, or the parent folder is read-only, or
     *             on error creating this file or one of its ancestors.
     */
    void createFile() throws FileSystemException;

    /**
     * Creates this folder, if it does not exist. Also creates any ancestor folders which do not exist. This method does
     * nothing if the folder already exists.
     *
     * @throws FileSystemException If the folder already exists with the wrong type, or the parent folder is read-only,
     *             or on error creating this folder or one of its ancestors.
     */
    void createFolder() throws FileSystemException;

    /**
     * Deletes this file. Does nothing if this file does not exist of if it is a folder that has children. Does not
     * delete any descendants of this file, use {@link #delete(FileSelector)} or {@link #deleteAll()} for that.
     *
     * @return true if this object has been deleted
     * @throws FileSystemException If this file is a non-empty folder, or if this file is read-only, or on error
     *             deleteing this file.
     */
    boolean delete() throws FileSystemException;

    /**
     * Deletes all descendants of this file that match a selector. Does nothing if this file does not exist.
     *
     * <p>
     * This method is not transactional. If it fails and throws an exception, this file will potentially only be
     * partially deleted.
     * </p>
     *
     * @param selector The selector to use to select which files to delete.
     * @return the number of deleted objects
     * @throws FileSystemException If this file or one of its descendants is read-only, or on error deleting this file
     *             or one of its descendants.
     */
    int delete(FileSelector selector) throws FileSystemException;

    /**
     * Deletes this file and all children.
     *
     * @return the number of deleted files.
     * @throws FileSystemException if an error occurs.
     * @see #delete(FileSelector)
     * @see Selectors#SELECT_ALL
     */
    int deleteAll() throws FileSystemException;

    /**
     * Determines if this file exists.
     *
     * @return {@code true} if this file exists, {@code false} if not.
     * @throws FileSystemException On error determining if this file exists.
     */
    boolean exists() throws FileSystemException;

    /**
     * Finds the set of matching descendants of this file, in depthwise order.
     *
     * @param selector The selector to use to select matching files.
     * @return The matching files. The files are returned in depthwise order (that is, a child appears in the list
     *         before its parent).
     * @throws FileSystemException if an error occurs.
     */
    FileObject[] findFiles(FileSelector selector) throws FileSystemException;

    /**
     * Finds the set of matching descendants of this file.
     *
     * @param selector the selector used to determine if the file should be selected
     * @param depthwise controls the ordering in the list. e.g. deepest first
     * @param selected container for selected files. list needs not to be empty.
     * @throws FileSystemException if an error occurs.
     */
    void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected) throws FileSystemException;

    /**
     * Returns a child of this file. Note that this method returns {@code null} when the child does not exist. This
     * differs from {@link #resolveFile(String, NameScope)} which never returns null.
     *
     * @param name The name of the child.
     * @return The child, or null if there is no such child.
     * @throws FileSystemException If this file does not exist, or is not a folder, or on error determining this file's
     *             children.
     */
    FileObject getChild(String name) throws FileSystemException;

    /**
     * Lists the children of this file.
     *
     * @return An array containing the children of this file. The array is unordered. If the file does not have any
     *         children, a zero-length array is returned. This method never returns null.
     * @throws FileSystemException If this file does not exist, or is not a folder, or on error listing this file's
     *             children.
     */
    FileObject[] getChildren() throws FileSystemException;

    /**
     * Returns this file's content. The {@link FileContent} returned by this method can be used to read and write the
     * content of the file.
     *
     * <p>
     * This method can be called if the file does not exist, and the returned {@link FileContent} can be used to create
     * the file by writing its content.
     * </p>
     *
     * @return This file's content.
     * @throws FileSystemException On error getting this file's content.
     */
    FileContent getContent() throws FileSystemException;

    /**
     * @return FileOperations interface that provides access to the operations API.
     * @throws FileSystemException if an error occurs.
     */
    FileOperations getFileOperations() throws FileSystemException;

    /**
     * Returns the file system that contains this file.
     *
     * @return The file system.
     */
    FileSystem getFileSystem();

    /**
     * Returns the name of this file.
     *
     * @return the FileName.
     */
    FileName getName();

    /**
     * Returns the folder that contains this file.
     *
     * @return The folder that contains this file. Returns null if this file is the root of a file system.
     * @throws FileSystemException On error finding the file's parent.
     */
    FileObject getParent() throws FileSystemException;

    /**
     * Returns the receiver as a URI String for public display, like, without a password.
     *
     * @return A URI String without a password, never {@code null}.
     */
    String getPublicURIString();

    /**
     * Returns this file's type.
     *
     * @return One of the {@link FileType} constants. Never returns null.
     * @throws FileSystemException On error determining the file's type.
     */
    FileType getType() throws FileSystemException;

    /**
     * Returns a URL representing this file.
     *
     * @return the URL for the file.
     * @throws FileSystemException if an error occurs.
     */
    URL getURL() throws FileSystemException;

    /**
     * Checks if the fileObject is attached.
     *
     * @return true if the FileObject is attached.
     */
    boolean isAttached();

    /**
     * Checks if someone reads/write to this file.
     *
     * @return true if the file content is open.
     */
    boolean isContentOpen();

    /**
     * Determines if this file is executable.
     *
     * @return {@code true} if this file is executable, {@code false} if not.
     * @throws FileSystemException On error determining if this file exists.
     */
    boolean isExecutable() throws FileSystemException;

    /**
     * Checks if this file is a regular file.
     *
     * @return true if this file is a regular file.
     * @throws FileSystemException if an error occurs.
     * @see #getType()
     * @see FileType#FILE
     * @since 2.1
     */
    boolean isFile() throws FileSystemException;

    /**
     * Checks if this file is a folder.
     *
     * @return true if this file is a folder.
     * @throws FileSystemException if an error occurs.
     * @see #getType()
     * @see FileType#FOLDER
     * @since 2.1
     */
    boolean isFolder() throws FileSystemException;

    /**
     * Determines if this file is hidden.
     *
     * @return {@code true} if this file is hidden, {@code false} if not.
     * @throws FileSystemException On error determining if this file exists.
     */
    boolean isHidden() throws FileSystemException;

    /**
     * Determines if this file can be read.
     *
     * @return {@code true} if this file is readable, {@code false} if not.
     * @throws FileSystemException On error determining if this file exists.
     */
    boolean isReadable() throws FileSystemException;

    /**
     * Determines if this file can be written to.
     *
     * @return {@code true} if this file is writeable, {@code false} if not.
     * @throws FileSystemException On error determining if this file exists.
     */
    boolean isWriteable() throws FileSystemException;

    /**
     * Move this file.
     *
     * <p>
     * If the destFile exists, it is deleted first.
     * </p>
     *
     * @param destFile the New filename.
     * @throws FileSystemException If this file is read-only, or if the source file does not exist, or on error copying
     *             the file.
     */
    void moveTo(FileObject destFile) throws FileSystemException;

    /**
     * This will prepare the fileObject to get resynchronized with the underlying file system if required.
     *
     * @throws FileSystemException if an error occurs.
     */
    void refresh() throws FileSystemException;

    /**
     * Finds a file, relative to this file. Equivalent to calling {@code resolveFile( path, NameScope.FILE_SYSTEM )}.
     *
     * @param path The path of the file to locate. Can either be a relative path or an absolute path.
     * @return The file.
     * @throws FileSystemException On error parsing the path, or on error finding the file.
     */
    FileObject resolveFile(String path) throws FileSystemException;

    /**
     * Finds a file relative to this file.
     *
     * Refer to {@link NameScope} for a description of how names are resolved in the different scopes.
     *
     * @param name The name to resolve.
     * @param scope the NameScope for the file.
     * @return The file.
     * @throws FileSystemException On error parsing the path, or on error finding the file.
     */
    FileObject resolveFile(String name, NameScope scope) throws FileSystemException;

    /**
     * Sets the owner's (or everybody's) write permission.
     *
     * @param executable True to allow read access, false to disallow.
     * @param ownerOnly If {@code true}, the permission applies only to the owner; otherwise, it applies to everybody.
     * @return true if the operation succeeded.
     * @throws FileSystemException On error determining if this file exists.
     * @since 2.1
     */
    boolean setExecutable(boolean executable, boolean ownerOnly) throws FileSystemException;

    /**
     * Sets the owner's (or everybody's) read permission.
     *
     * @param readable True to allow read access, false to disallow
     * @param ownerOnly If {@code true}, the permission applies only to the owner; otherwise, it applies to everybody.
     * @return true if the operation succeeded
     * @throws FileSystemException On error determining if this file exists.
     * @since 2.1
     */
    boolean setReadable(boolean readable, boolean ownerOnly) throws FileSystemException;

    /**
     * Sets the owner's (or everybody's) write permission.
     *
     * @param writable True to allow read access, false to disallow
     * @param ownerOnly If {@code true}, the permission applies only to the owner; otherwise, it applies to everybody.
     * @return true if the operation succeeded
     * @throws FileSystemException On error determining if this file exists.
     * @since 2.1
     */
    boolean setWritable(boolean writable, boolean ownerOnly) throws FileSystemException;
}
