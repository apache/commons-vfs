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
 * Filters files based on the suffix (what the file name ends with). This is used
 * in retrieving all the files of a particular type.
 * <p>
 * For example, to retrieve and print all {@code *.java} files in the
 * current directory:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * FileObject[] files = dir.findFiles(new FileFilterSelector(new SuffixFileFilter(&quot;.java&quot;)));
 * for (int i = 0; i &lt; files.length; i++) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @author This code was originally ported from Apache Commons IO File Filter
 * @see "http://commons.apache.org/proper/commons-io/"
 * @since 2.4
 */
public class SuffixFileFilter implements FileFilter, Serializable {

    private static final long serialVersionUID = 1L;

    /** Whether the comparison is case sensitive. */
    private final IOCase caseSensitivity;

    /** The file name suffixes to search for. */
    private final List<String> suffixes;

    /**
     * Constructs a new Suffix file filter for a list of suffixes.
     *
     * @param suffixes the suffixes to allow, must not be null
     */
    public SuffixFileFilter(final List<String> suffixes) {
        this(IOCase.SENSITIVE, suffixes);
    }

    /**
     * Constructs a new Suffix file filter for a list of suffixes specifying
     * case-sensitivity.
     *
     * @param suffixes        the suffixes to allow, must not be null
     * @param caseSensitivity how to handle case sensitivity, null means
     *                        case-sensitive
     */
    public SuffixFileFilter(final IOCase caseSensitivity, final List<String> suffixes) {
        if (suffixes == null) {
            throw new IllegalArgumentException("The list of suffixes must not be null");
        }
        this.suffixes = new ArrayList<>(suffixes);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Constructs a new Suffix file filter for an array of suffixes.
     *
     * @param suffixes the suffixes to allow, must not be null
     */
    public SuffixFileFilter(final String... suffixes) {
        this(IOCase.SENSITIVE, suffixes);
    }

    /**
     * Constructs a new Suffix file filter for an array of suffixs specifying
     * case-sensitivity.
     *
     * @param suffixes        the suffixes to allow, must not be null
     * @param caseSensitivity how to handle case sensitivity, null means
     *                        case-sensitive
     */
    public SuffixFileFilter(final IOCase caseSensitivity, final String... suffixes) {
        if (suffixes == null) {
            throw new IllegalArgumentException("The array of suffixes must not be null");
        }
        this.suffixes = new ArrayList<>(Arrays.asList(suffixes));
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Checks to see if the file name ends with the suffix.
     *
     * @param fileInfo the File to check
     *
     * @return true if the file name ends with one of our suffixes
     */
    @Override
    public boolean accept(final FileSelectInfo fileInfo) {
        final String name = fileInfo.getFile().getName().getBaseName();
        for (final String suffix : this.suffixes) {
            if (caseSensitivity.checkEndsWith(name, suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Provides a String representation of this file filter.
     *
     * @return a String representation
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(super.toString());
        buffer.append("(");
        if (suffixes != null) {
            for (int i = 0; i < suffixes.size(); i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(suffixes.get(i));
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

}
