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

import java.util.List;

import org.apache.commons.vfs2.FileFilter;

/**
 * Defines operations for conditional file filters.
 *
 * @author This code was originally ported from Apache Commons IO File Filter
 * @see "http://commons.apache.org/proper/commons-io/"
 * @since 2.4
 */
public interface ConditionalFileFilter {

    /**
     * Adds the specified file filter to the list of file filters at the end of the
     * list.
     *
     * @param fileFilter the filter to be added
     */
    void addFileFilter(FileFilter fileFilter);

    /**
     * Returns this conditional file filter's list of file filters.
     *
     * @return the file filter list
     */
    List<FileFilter> getFileFilters();

    /**
     * Removes the specified file filter.
     *
     * @param fileFilter filter to be removed
     *
     * @return {@code true} if the filter was found in the list, {@code false}
     *         otherwise
     */
    boolean removeFileFilter(FileFilter fileFilter);

    /**
     * Sets the list of file filters, replacing any previously configured file
     * filters on this filter.
     *
     * @param fileFilters the list of filters
     */
    void setFileFilters(List<FileFilter> fileFilters);

}
