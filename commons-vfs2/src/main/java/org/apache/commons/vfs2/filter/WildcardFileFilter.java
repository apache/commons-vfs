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
import java.util.Stack;

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileSelectInfo;

/**
 * Filters files using the supplied wildcards.
 * <p>
 * This filter selects files and directories based on one or more wildcards.
 * Testing is case-sensitive by default, but this can be configured.
 * </p>
 * <p>
 * The wildcard matcher uses the characters '?' and '*' to represent a single or
 * multiple wildcard characters. This is the same as often found on Dos/Unix
 * command lines.
 * </p>
 * <p>
 * For example, to retrieve and print all java files that have the expression
 * test in the name in the current directory:
 * </p>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject dir = fsManager.toFileObject(new File(&quot;.&quot;));
 * FileObject[] files;
 * files = dir.findFiles(new FileFilterSelector(new WildcardFileFilter(&quot;*test*.java&quot;)));
 * for (int i = 0; i &lt; files.length; i++) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @author This code was originally ported from Apache Commons IO File Filter
 * @see "http://commons.apache.org/proper/commons-io/"
 * @since 2.4
 */
public class WildcardFileFilter implements FileFilter, Serializable {

    private static final long serialVersionUID = 1L;

    /** Whether the comparison is case sensitive. */
    private final IOCase caseSensitivity;

    /** The wildcards that will be used to match file names. */
    private final List<String> wildcards;

    /**
     * Construct a new case-sensitive wildcard filter for a list of wildcards.
     *
     * @param wildcards the list of wildcards to match, not null
     */
    public WildcardFileFilter(final List<String> wildcards) {
        this((IOCase) null, wildcards);
    }

    /**
     * Construct a new wildcard filter for a list of wildcards specifying
     * case-sensitivity.
     *
     * @param caseSensitivity how to handle case sensitivity, null means
     *                        case-sensitive
     * @param wildcards       the list of wildcards to match, not null
     */
    public WildcardFileFilter(final IOCase caseSensitivity, final List<String> wildcards) {
        if (wildcards == null) {
            throw new IllegalArgumentException("The wildcard list must not be null");
        }
        this.wildcards = new ArrayList<>(wildcards);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Construct a new case-sensitive wildcard filter for an array of wildcards.
     * <p>
     * The array is not cloned, so could be changed after constructing the instance.
     * This would be inadvisable however.
     *
     * @param wildcards the array of wildcards to match
     */
    public WildcardFileFilter(final String... wildcards) {
        this((IOCase) null, wildcards);
    }

    /**
     * Construct a new wildcard filter for an array of wildcards specifying
     * case-sensitivity.
     *
     * @param caseSensitivity how to handle case sensitivity, null means
     *                        case-sensitive
     * @param wildcards       the array of wildcards to match, not null
     */
    public WildcardFileFilter(final IOCase caseSensitivity, final String... wildcards) {
        if (wildcards == null) {
            throw new IllegalArgumentException("The wildcard array must not be null");
        }
        this.wildcards = new ArrayList<>(Arrays.asList(wildcards));
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Checks to see if the file name matches one of the wildcards.
     *
     * @param fileInfo the file to check
     *
     * @return true if the file name matches one of the wildcards
     */
    @Override
    public boolean accept(final FileSelectInfo fileInfo) {
        final String name = fileInfo.getFile().getName().getBaseName();
        for (final String wildcard : wildcards) {
            if (wildcardMatch(name, wildcard, caseSensitivity)) {
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
        if (wildcards != null) {
            for (int i = 0; i < wildcards.size(); i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(wildcards.get(i));
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

    /**
     * Splits a string into a number of tokens. The text is split by '?' and '*'.
     * Where multiple '*' occur consecutively they are collapsed into a single '*'.
     *
     * @param text the text to split
     * @return the array of tokens, never null
     */
    // CHECKSTYLE:OFF Cyclomatic complexity of 12 is OK here
    static String[] splitOnTokens(final String text) {
        // used by wildcardMatch
        // package level so a unit test may run on this

        if (text.indexOf('?') == -1 && text.indexOf('*') == -1) {
            return new String[] { text };
        }

        final char[] array = text.toCharArray();
        final ArrayList<String> list = new ArrayList<>();
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == '?' || array[i] == '*') {
                if (buffer.length() != 0) {
                    list.add(buffer.toString());
                    buffer.setLength(0);
                }
                if (array[i] == '?') {
                    list.add("?");
                } else if (list.isEmpty() || i > 0 && !list.get(list.size() - 1).equals("*")) {
                    list.add("*");
                }
            } else {
                buffer.append(array[i]);
            }
        }
        if (buffer.length() != 0) {
            list.add(buffer.toString());
        }

        return list.toArray(new String[list.size()]);
    }

    // CHECKSTYLE:ON

    /**
     * Checks a file name to see if it matches the specified wildcard matcher
     * allowing control over case-sensitivity.
     * <p>
     * The wildcard matcher uses the characters '?' and '*' to represent a single or
     * multiple (zero or more) wildcard characters. N.B. the sequence "*?" does not
     * work properly at present in match strings.
     * </p>
     *
     * @param fileName        the file name to match on
     * @param wildcardMatcher the wildcard string to match against
     * @param caseSensitivity what case sensitivity rule to use, null means
     *                        case-sensitive
     *
     * @return true if the file name matches the wilcard string
     */
    // CHECKSTYLE:OFF TODO xxx Cyclomatic complexity of 19 should be refactored
    static boolean wildcardMatch(final String fileName, final String wildcardMatcher, IOCase caseSensitivity) {
        if (fileName == null && wildcardMatcher == null) {
            return true;
        }
        if (fileName == null || wildcardMatcher == null) {
            return false;
        }
        if (caseSensitivity == null) {
            caseSensitivity = IOCase.SENSITIVE;
        }
        final String[] wcs = splitOnTokens(wildcardMatcher);
        boolean anyChars = false;
        int textIdx = 0;
        int wcsIdx = 0;
        final Stack<int[]> backtrack = new Stack<>();

        // loop around a backtrack stack, to handle complex * matching
        do {
            if (backtrack.size() > 0) {
                final int[] array = backtrack.pop();
                wcsIdx = array[0];
                textIdx = array[1];
                anyChars = true;
            }

            // loop whilst tokens and text left to process
            while (wcsIdx < wcs.length) {

                if (wcs[wcsIdx].equals("?")) {
                    // ? so move to next text char
                    textIdx++;
                    if (textIdx > fileName.length()) {
                        break;
                    }
                    anyChars = false;

                } else if (wcs[wcsIdx].equals("*")) {
                    // set any chars status
                    anyChars = true;
                    if (wcsIdx == wcs.length - 1) {
                        textIdx = fileName.length();
                    }

                } else {
                    // matching text token
                    if (anyChars) {
                        // any chars then try to locate text token
                        textIdx = caseSensitivity.checkIndexOf(fileName, textIdx, wcs[wcsIdx]);
                        if (textIdx == -1) {
                            // token not found
                            break;
                        }
                        final int repeat = caseSensitivity.checkIndexOf(fileName, textIdx + 1, wcs[wcsIdx]);
                        if (repeat >= 0) {
                            backtrack.push(new int[] { wcsIdx, repeat });
                        }
                    } else {
                        // matching from current position
                        if (!caseSensitivity.checkRegionMatches(fileName, textIdx, wcs[wcsIdx])) {
                            // couldnt match token
                            break;
                        }
                    }

                    // matched text token, move text index to end of matched
                    // token
                    textIdx += wcs[wcsIdx].length();
                    anyChars = false;
                }

                wcsIdx++;
            }

            // full match
            if (wcsIdx == wcs.length && textIdx == fileName.length()) {
                return true;
            }

        } while (backtrack.size() > 0);

        return false;
    }
    // CHECKSTYLE:ON

}
