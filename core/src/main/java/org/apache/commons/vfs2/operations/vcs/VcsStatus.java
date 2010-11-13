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
 *
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsStatus extends FileOperation
{
    int UNKNOWN = -1;
    int NOT_MODIFIED = 0;
    int ADDED = 1;
    int CONFLICTED = 2;
    int DELETED = 3;
    int MERGED = 4;
    int IGNORED = 5;
    int MODIFIED = 6;
    int REPLACED = 7;
    int UNVERSIONED = 8;
    int MISSING = 9;
    int OBSTRUCTED = 10;
    int REVERTED = 11;
    int RESOLVED = 12;
    int COPIED = 13;
    int MOVED = 14;
    int RESTORED = 15;
    int UPDATED = 16;
    int EXTERNAL = 18;
    int CORRUPTED = 19;
    int NOT_REVERTED = 20;

    /**
     *
     * @return the status of FileObject
     */
    int getStatus();
}
