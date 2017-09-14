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

import java.util.Calendar;

/**
 *
 * @since 0.1
 */
public class VcsLogEntry {
    /**
     */
    private final String author;

    /**
     * Revision.
     */
    private final long revision;

    /**
     * Message.
     */
    private final String message;

    /**
     * Date.
     */
    private final Calendar date;

    /**
     * Path.
     */
    private final String path;

    /**
     *
     * @param author The author.
     * @param revision The revision.
     * @param message The message.
     * @param date The date.
     * @param path The path.
     */
    public VcsLogEntry(final String author, final long revision, final String message, final Calendar date,
            final String path) {
        this.author = author;
        this.revision = revision;
        this.message = message;
        this.date = date;
        this.path = path;
    }

    /**
     *
     * @return The author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     *
     * @return The revision.
     */
    public long getRevision() {
        return revision;
    }

    /**
     *
     * @return The message.
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @return The date.
     */
    public Calendar getDate() {
        return date;
    }

    /**
     *
     * @return The path.
     */
    public String getPath() {
        return path;
    }
}
