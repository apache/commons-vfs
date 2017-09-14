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

/**
 * This interface is used to select files when traversing a file hierarchy.
 *
 * @see Selectors
 */
public interface FileSelector {
    /**
     * Determines if a file or folder should be selected. This method is called in depthwise order (that is, it is
     * called for the children of a folder before it is called for the folder itself).
     *
     * @param fileInfo the file or folder to select.
     * @return true if the file should be selected.
     * @throws Exception if an error occurs.
     */
    boolean includeFile(FileSelectInfo fileInfo) throws Exception;

    /**
     * Determines whether a folder should be traversed. If this method returns true, {@link #includeFile} is called for
     * each of the children of the folder, and each of the child folders is recursively traversed.
     * <p>
     * This method is called on a folder before {@link #includeFile} is called.
     *
     * @param fileInfo the file or folder to select.
     * @return true if the folder should be traversed.
     * @throws Exception if an error occurs.
     */
    boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception;
}
