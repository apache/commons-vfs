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
package org.apache.commons.vfs2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link FileSelector} that selects based on regular expressions.
 * <p>
 * The regular expression specified in one of the constructors is
 * {@linkplain Matcher#matches() matched} against {@link FileName#getPath()}
 * of all candidate files. If you want to match only against the base filename,
 * make sure to prefix the pattern with {@code ".*\\/"}.
 *
 * @since 2.1
 */
public class PatternFileSelector implements FileSelector {

    /**
     * The extensions to select.
     */
    private final Pattern pattern;

    /**
     * Creates a new selector for the given pattern.
     * <p>
     * See {@link PatternFileSelector} for a specification how the pattern is matched.
     *
     * @param pattern The regular expressed used by this selector.
     */
    public PatternFileSelector(final Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Creates a new selector for the given pattern.
     * <p>
     * See {@link PatternFileSelector} for a specification how the pattern is matched.
     *
     * @param regex The regular expressed used by this selector.
     *
     * @see Pattern#compile(String, int)
     */
    public PatternFileSelector(final String regex) {
        this(Pattern.compile(regex));
    }

    /**
     * Creates a new selector for the given Pattern and flags.
     * <p>
     * See {@link PatternFileSelector} for a specification how the pattern is matched.
     *
     * @param regex The expression to be compiled
     * @param flags Match flags, a bit mask.
     *
     * @see Pattern#compile(String, int)
     */
    public PatternFileSelector(final String regex, final int flags) {
        this(Pattern.compile(regex, flags));
    }

    /**
     * Determines if a file or folder should be selected.
     * <p>
     * See {@link PatternFileSelector} for a specification how the pattern is matched.
     *
     * @param fileInfo The file selection information.
     * @return true if the file should be selected, false otherwise.
     */
    @Override
    public boolean includeFile(final FileSelectInfo fileInfo) throws Exception {
        return this.pattern.matcher(fileInfo.getFile().getName().getPath()).matches();
    }

    @Override
    public String toString() {
        return this.pattern.toString();
    }

    /**
     * Determines whether a folder should be traversed.
     * <p>
     * This implementation always returns true to make sure all
     * leafs are inspected.
     *
     * @param fileInfo The file selection information.
     * @return true if descendants should be traversed, false otherwise.
     */
    @Override
    public boolean traverseDescendents(final FileSelectInfo fileInfo) throws Exception {
        return true;
    }
}
