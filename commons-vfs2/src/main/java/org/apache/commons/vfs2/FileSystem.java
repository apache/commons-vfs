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

import java.io.File;

/**
 * A file system, made up of a hierarchy of files.
 */
public interface FileSystem {
    /**
     * Returns the root file of this file system.
     *
     * @return The root FileObject.
     * @throws FileSystemException if an error occurs.
     */
    FileObject getRoot() throws FileSystemException;

    /**
     * Returns the name of the root file of this file system. The root name always contains a path String of "/".
     *
     * @return the root FileName.
     */
    FileName getRootName();

    /**
     * The root URI passed as a file system option or obtained from the rootName.
     *
     * @return The root URI.
     */
    String getRootURI();

    /**
     * Determines if this file system has a particular capability.
     * <p>
     * TODO - Move this to another interface, so that set of capabilities can be queried.
     *
     * @param capability The capability to check for.
     * @return true if this filesystem has the requested capability. Note that not all files in the file system may have
     *         the capability.
     */
    boolean hasCapability(Capability capability);

    /**
     * Returns the parent layer if this is a layered file system.
     *
     * @return The parent layer, or null if this is not a layered file system.
     * @throws FileSystemException if an error occurs.
     */
    FileObject getParentLayer() throws FileSystemException;

    /**
     * Gets the value of an attribute of the file system.
     * <p>
     * TODO - change to {@code Map getAttributes()} instead?<br>
     * TODO - define the standard attribute names, and define which attrs are guaranteed to be present.
     *
     * @param attrName The name of the attribute.
     * @return The value of the attribute.
     * @throws org.apache.commons.vfs2.FileSystemException If the file does not exist, or is being written, or if the
     *             attribute is unknown.
     * @see org.apache.commons.vfs2.FileContent#getAttribute
     */
    Object getAttribute(String attrName) throws FileSystemException;

    /**
     * Sets the value of an attribute of the file's content. Creates the file if it does not exist.
     *
     * @param attrName The name of the attribute.
     * @param value The value of the attribute.
     * @throws FileSystemException If the file is read-only, or is being read, or if the attribute is not supported, or
     *             on error setting the attribute.
     * @see FileContent#setAttribute
     */
    void setAttribute(String attrName, Object value) throws FileSystemException;

    /**
     * Finds a file in this file system.
     *
     * @param name The name of the file.
     * @return The file. Never returns null.
     * @throws FileSystemException if an error occurs.
     */
    FileObject resolveFile(FileName name) throws FileSystemException;

    /**
     * Finds a file in this file system.
     *
     * @param name The name of the file. This must be an absolute path.
     * @return The file. Never returns null.
     * @throws FileSystemException if an error occurs.
     */
    FileObject resolveFile(String name) throws FileSystemException;

    /**
     * Adds a listener on a file in this file system.
     *
     * @param file The file to attach the listener to.
     * @param listener The listener to add.
     */
    void addListener(FileObject file, FileListener listener);

    /**
     * Removes a listener from a file in this file system.
     *
     * @param file The file to remove the listener from.
     * @param listener The listener to remove.
     */
    void removeListener(FileObject file, FileListener listener);

    /**
     * Adds a junction to this file system. A junction is a link that attaches the supplied file to a point in this file
     * system, making it look like part of the file system.
     *
     * @param junctionPoint The point in this file system to add the junction.
     * @param targetFile The file to link to.
     * @throws FileSystemException If this file system does not support junctions, or the junction point or target file
     *             is invalid (the file system may not support nested junctions, for example).
     */
    void addJunction(String junctionPoint, FileObject targetFile) throws FileSystemException;

    /**
     * Removes a junction from this file system.
     *
     * @param junctionPoint The junction to remove.
     * @throws FileSystemException On error removing the junction.
     */
    void removeJunction(String junctionPoint) throws FileSystemException;

    /**
     * Creates a temporary local copy of a file and its descendants. If this file is already a local file, a copy is not
     * made.
     * <p>
     * Note that the local copy may include additonal files, that were not selected by the given selector.
     * <p>
     * TODO - Add options to indicate whether the caller is happy to deal with extra files being present locally (eg if
     * the file has been replicated previously), or whether the caller expects only the selected files to be present.
     *
     * @param file The file to replicate.
     * @param selector The selector to use to select the files to replicate.
     * @return The local copy of this file.
     * @throws FileSystemException If this file does not exist, or on error replicating the file.
     */
    File replicateFile(FileObject file, FileSelector selector) throws FileSystemException;

    /**
     * Returns the FileSystemOptions used to instantiate this filesystem.
     *
     * @return The FileSystemOptions.
     */
    FileSystemOptions getFileSystemOptions();

    /**
     * Returns a reference to the FileSytemManager.
     *
     * @return The FileSystemManager.
     */
    FileSystemManager getFileSystemManager();

    /**
     * Returns the accuracy of the last modification time.
     * <p>
     * The local file provider is not very smart in figuring this out, for remote access to file systems the providers
     * typically don't know the value of the underlying real file system.
     *
     * @return the accuracy of the last modification time in milliseconds. A value of 0 means perfectly accurate,
     *         anything {@literal > 0} might be off by this value. For example, sftp has an accuracy of 1000 ms.
     */
    double getLastModTimeAccuracy();
}
