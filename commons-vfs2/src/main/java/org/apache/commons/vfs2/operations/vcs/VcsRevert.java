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
package org.apache.commons.vfs2.operations.vcs;

import org.apache.commons.vfs2.operations.FileOperation;

/**
 * <p>
 * Restores pristine working copy file and cancels all local modifications. In other words, VcsRevert replaces working
 * copy file with the latest version from the repository.
 * </p>
 *
 * @since 0.1
 */
public interface VcsRevert extends FileOperation {
    /**
     *
     * @param recursive true if children should be processed.
     */
    void setRecursive(final boolean recursive);

    /**
     *
     * @param listener The Listener to add.
     */
    void addModifyListener(final VcsModifyListener listener);

    /**
     *
     * @param listener The Listener to remove.
     */
    void removeModifyListener(final VcsModifyListener listener);
}
