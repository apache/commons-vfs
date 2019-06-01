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
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileSelectInfo;

/**
 * Filters files using supplied regular expression(s).
 * <p>
 * See java.util.regex.Pattern for regex matching rules.
 * </p>
 *
 * <p>
 * For example, to retrieve and print all java files where the name matched the
 * regular expression in the current directory:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * FileObject[] files = dir.findFiles(new FileFilterSelector(new RegexFileFilter(&quot;Ë†.*[tT]est(-\\d+)?\\.java$&quot;)));
 * for (int i = 0; i &lt; files.length; i++) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @author This code was originally ported from Apache Commons IO File Filter
 * @see "http://commons.apache.org/proper/commons-io/"
 * @since 2.4
 */
public class RegexFileFilter implements FileFilter, Serializable {

    private static final long serialVersionUID = 1L;

    /** Exception message when no pattern is given in the constructor. */
    public static final String PATTERN_IS_MISSING = "Pattern is missing";

    /** The regular expression pattern that will be used to match file names. */
    private final Pattern pattern;

    /**
     * Construct a new regular expression filter for a compiled regular expression.
     *
     * @param pattern regular expression to match - Cannot be null
     */
    public RegexFileFilter(final Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException(PATTERN_IS_MISSING);
        }

        this.pattern = pattern;
    }

    /**
     * Construct a new regular expression filter.
     *
     * @param pattern regular string expression to match - Cannot be null
     */
    public RegexFileFilter(final String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException(PATTERN_IS_MISSING);
        }

        this.pattern = Pattern.compile(pattern);
    }

    /**
     * Construct a new regular expression filter with the specified flags.
     *
     * @param pattern regular string expression to match
     * @param flags   pattern flags - e.g. {@link Pattern#CASE_INSENSITIVE}
     */
    public RegexFileFilter(final String pattern, final int flags) {
        if (pattern == null) {
            throw new IllegalArgumentException(PATTERN_IS_MISSING);
        }
        this.pattern = Pattern.compile(pattern, flags);
    }

    /**
     * Construct a new regular expression filter with the specified flags case
     * sensitivity.
     *
     * @param pattern         regular string expression to match - Cannot be null
     * @param caseSensitivity how to handle case sensitivity, null means
     *                        case-sensitive
     */
    public RegexFileFilter(final String pattern, final IOCase caseSensitivity) {
        if (pattern == null) {
            throw new IllegalArgumentException(PATTERN_IS_MISSING);
        }
        int flags = 0;
        if (caseSensitivity != null && !caseSensitivity.isCaseSensitive()) {
            flags = Pattern.CASE_INSENSITIVE;
        }
        this.pattern = Pattern.compile(pattern, flags);
    }

    /**
     * Checks to see if the file name matches one of the regular expressions.
     *
     * @param fileInfo the File to check
     *
     * @return true if the file matches one of the regular expressions
     */
    @Override
    public boolean accept(final FileSelectInfo fileInfo) {
        final String name = fileInfo.getFile().getName().getBaseName();
        return pattern.matcher(name).matches();
    }

}
