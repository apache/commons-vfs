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
 *
 * @since 0.1
 */
public enum VcsStatus {
    UNKNOWN(-1), NOT_MODIFIED(0), ADDED(1), CONFLICTED(2), DELETED(3), MERGED(4), IGNORED(5), MODIFIED(6), REPLACED(
            7), UNVERSIONED(8), MISSING(9), OBSTRUCTED(10), REVERTED(11), RESOLVED(12), COPIED(
                    13), MOVED(14), RESTORED(15), UPDATED(16), EXTERNAL(18), CORRUPTED(19), NOT_REVERTED(20);

    private int status;

    private VcsStatus(final int status) {
        this.status = status;
    }

    /**
     *
     * @return the status of FileObject
     */
    public int getStatus() {
        return status;
    }
}
