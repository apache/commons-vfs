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

/**
 * This interface is used to select files when traversing a file hierarchy.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 * @see Selectors
 */
public interface FileSelector
{
    /**
     * Determines if a file or folder should be selected.  This method is
     * called in depthwise order (that is, it is called for the children
     * of a folder before it is called for the folder itself).
     *
     * @param fileInfo the file or folder to select.
     * @return true if the file should be selected.
     */
    boolean includeFile(FileSelectInfo fileInfo)
        throws Exception;

    /**
     * Determines whether a folder should be traversed.  If this method returns
     * true, {@link #includeFile} is called for each of the children of
     * the folder, and each of the child folders is recursively traversed.
     * <p/>
     * <p>This method is called on a folder before {@link #includeFile}
     * is called.
     *
     * @param fileInfo the file or folder to select.
     * @return true if the folder should be traversed.
     */
    boolean traverseDescendents(FileSelectInfo fileInfo)
        throws Exception;
}
