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
import java.util.List;

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileSelectInfo;

/**
 * Filters file names for a certain name.
 * <p>
 * For example, to print all files and directories in the current directory
 * whose name is {@code Test}:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * FileObject[] files = dir.findFiles(new FileFilterSelector(new NameFileFilter(&quot;Test&quot;)));
 * for (int i = 0; i &lt; files.length; i++) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @author This code was originally ported from Apache Commons IO File Filter
 * @see "http://commons.apache.org/proper/commons-io/"
 * @since 2.4
 */
public class NameFileFilter implements FileFilter, Serializable {

    private static final long serialVersionUID = 1L;

    /** Whether the comparison is case sensitive. */
    private final IOCase caseSensitivity;

    /** The file names to search for. */
    private final List<String> names;

    /**
     * Constructs a new case-sensitive name file filter for a list of names.
     *
     * @param names the names to allow, must not be null
     */
    public NameFileFilter(final List<String> names) {
        this((IOCase) null, names);
    }

    /**
     * Constructs a new name file filter for a list of names specifying
     * case-sensitivity.
     *
     * @param caseSensitivity how to handle case sensitivity, null means
     *                        case-sensitive
     * @param names           the names to allow, must not be null
     */
    public NameFileFilter(final IOCase caseSensitivity, final List<String> names) {
        if (names == null) {
            throw new IllegalArgumentException("The list of names must not be null");
        }
        this.names = new ArrayList<>(names);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Constructs a new case-sensitive name file filter for an array of names.
     * <p>
     * The array is not cloned, so could be changed after constructing the instance.
     * This would be inadvisable however.
     * </p>
     *
     * @param names the names to allow, must not be null
     */
    public NameFileFilter(final String... names) {
        this((IOCase) null, names);
    }

    /**
     * Constructs a new name file filter for an array of names specifying
     * case-sensitivity.
     *
     * @param caseSensitivity how to handle case sensitivity, null means
     *                        case-sensitive
     * @param names           the names to allow, must not be null
     */
    public NameFileFilter(final IOCase caseSensitivity, final String... names) {
        if (names == null) {
            throw new IllegalArgumentException("The array of names must not be null");
        }
        this.names = new ArrayList<>(Arrays.asList(names));
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Checks to see if the file name matches.
     *
     * @param fileInfo the File to check
     *
     * @return true if the file name matches
     */
    @Override
    public boolean accept(final FileSelectInfo fileInfo) {
        final String name = fileInfo.getFile().getName().getBaseName();
        for (final String name2 : this.names) {
            if (caseSensitivity.checkEquals(name, name2)) {
                return true;
            }
        }
        return false;
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
        if (names != null) {
            for (int i = 0; i < names.size(); i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(names.get(i));
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

}
