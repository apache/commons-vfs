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

import java.util.Objects;

/**
 * Inverts file inclusion of a delegate FileSelector, folder traversal is delegated.
 *
 * @since 2.2
 */
public class InvertIncludeFileSelector implements FileSelector {

    public InvertIncludeFileSelector(final FileSelector delegateFileSelector) {
        this.delegateFileSelector = Objects.requireNonNull(delegateFileSelector, "delegateFileSelector");
    }

    private final FileSelector delegateFileSelector;

    /**
     * Inverts the result of calling {@link #includeFile(FileSelectInfo)} on the delegate.
     */
    @Override
    public boolean includeFile(final FileSelectInfo fileInfo) throws Exception {
        return !delegateFileSelector.includeFile(fileInfo);
    }

    /**
     * Calls {@link #traverseDescendents(FileSelectInfo)} on the delegate.
     */
    @Override
    public boolean traverseDescendents(final FileSelectInfo fileInfo) throws Exception {
        return delegateFileSelector.traverseDescendents(fileInfo);
    }

}
