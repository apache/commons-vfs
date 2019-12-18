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
package org.apache.commons.vfs2.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;

/**
 * A filter providing conditional AND logic across a list of file filters. This
 * filter returns {@code true} if all filters in the list return {@code true}.
 * Otherwise, it returns {@code false}. Checking of the file filter list stops
 * when the first filter returns {@code false}.
 *
 * @author This code was originally ported from Apache Commons IO File Filter
 * @see "http://commons.apache.org/proper/commons-io/"
 * @since 2.4
 */
public class AndFileFilter implements FileFilter, ConditionalFileFilter, Serializable {

    private static final long serialVersionUID = 1L;

    /** The list of file filters. */
    private final List<FileFilter> fileFilters;

    /**
     * Default constructor.
     */
    public AndFileFilter() {
        this.fileFilters = new ArrayList<>();
    }

    /**
     * Constructs a new file filter that ANDs the result of other filters.
     *
     * @param filters array of filters, must not be null or empty
     */
    public AndFileFilter(final FileFilter... filters) {
        if (filters == null || filters.length == 0) {
            throw new IllegalArgumentException("The filters must not be null or empty");
        }
        for (final FileFilter filter : filters) {
            if (filter == null) {
                throw new IllegalArgumentException("Null filters are not allowed");
            }
        }
        this.fileFilters = new ArrayList<>(Arrays.asList(filters));
    }

    /**
     * Constructs a new instance of {@code AndFileFilter} with the specified
     * list of filters.
     *
     * @param fileFilters a List of FileFilter instances, copied, null ignored
     */
    public AndFileFilter(final List<FileFilter> fileFilters) {
        if (fileFilters == null) {
            this.fileFilters = new ArrayList<>();
        } else {
            this.fileFilters = new ArrayList<>(fileFilters);
        }
    }

    @Override
    public boolean accept(final FileSelectInfo fileInfo) throws FileSystemException {
        if (this.fileFilters.isEmpty()) {
            return false;
        }
        for (final FileFilter fileFilter : fileFilters) {
            if (!fileFilter.accept(fileInfo)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addFileFilter(final FileFilter fileFilter) {
        this.fileFilters.add(fileFilter);
    }

    @Override
    public List<FileFilter> getFileFilters() {
        return Collections.unmodifiableList(this.fileFilters);
    }

    @Override
    public boolean removeFileFilter(final FileFilter fileFilter) {
        return this.fileFilters.remove(fileFilter);
    }

    @Override
    public void setFileFilters(final List<FileFilter> fileFilters) {
        this.fileFilters.clear();
        this.fileFilters.addAll(fileFilters);
    }

    /**
     * Provide a String representation of this file filter.
     *
     * @return a String representation
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(super.toString());
        buffer.append("(");
        if (fileFilters != null) {
            for (int i = 0; i < fileFilters.size(); i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                final Object filter = fileFilters.get(i);
                buffer.append(filter == null ? "null" : filter.toString());
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

}
