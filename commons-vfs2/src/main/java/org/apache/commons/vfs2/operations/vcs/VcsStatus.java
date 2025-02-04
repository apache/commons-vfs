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

/**
 * Enumerates VFS status.
 *
 * @since 0.1
 */
public enum VcsStatus {

    /**
     * Unknown.
     */
    UNKNOWN(-1),

    /**
     * Not modified.
     */
    NOT_MODIFIED(0),

    /**
     * Added.
     */
    ADDED(1),

    /**
     * Conflicted.
     */
    CONFLICTED(2),

    /**
     * Deleted.
     */
    DELETED(3),

    /**
     * Merged.
     */
    MERGED(4),

    /**
     * Ignored.
     */
    IGNORED(5),

    /**
     * Modified.
     */
    MODIFIED(6),

    /**
     * Replaced.
     */
    REPLACED(7),

    /**
     * Unversioned.
     */
    UNVERSIONED(8),

    /**
     * Missing.
     */
    MISSING(9),

    /**
     * Obstructed.
     */
    OBSTRUCTED(10),

    /**
     * Reverted.
     */
    REVERTED(11),

    /**
     * Resolved.
     */
    RESOLVED(12),

    /**
     * Copied.
     */
    COPIED(13),

    /**
     * Moved.
     */
    MOVED(14),

    /**
     * Restored.
     */
    RESTORED(15),

    /**
     * Updated.
     */
    UPDATED(16),

    /**
     * External.
     */
    EXTERNAL(18),

    /**
     * Corrupted.
     */
    CORRUPTED(19),

    /**
     * Not reverted.
     */
    NOT_REVERTED(20);

    private final int status;

    VcsStatus(final int status) {
        this.status = status;
    }

    /**
     * Gets the status.
     *
     * @return the status of FileObject
     */
    public int getStatus() {
        return status;
    }
}
