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

/**
 * An enumerated type for file name scope, used when resolving a name relative to a file.
 */
public enum NameScope {
    /**
     * Resolve against the children of the base file. The name is resolved as described by {@link #FILE_SYSTEM}.
     * However, an exception is thrown if the resolved file is not a direct child of the base file.
     */
    CHILD("child"),

    /**
     * Resolve against the descendants of the base file. The name is resolved as described by {@link #FILE_SYSTEM}.
     * However, an exception is thrown if the resolved file is not a descendent of the base file.
     */
    DESCENDENT("descendent"),

    /**
     * Resolve against the descendants of the base file. The name is resolved as described by {@link #FILE_SYSTEM}.
     * However, an exception is thrown if the resolved file is not a descendent of the base file, or the base files
     * itself.
     */
    DESCENDENT_OR_SELF("descendent_or_self"),

    /**
     * Resolve against files in the same file system as the base file.
     * <p>
     * If the supplied name is an absolute path, then it is resolved relative to the root of the file system that the
     * base file belongs to. If a relative name is supplied, then it is resolved relative to the base file.
     * <p>
     * The path may use any mix of {@code /}, {@code \}, or file system specific separators to separate elements in the
     * path. It may also contain {@code .} and {@code ..} elements.
     * <p>
     * A path is considered absolute if it starts with a separator character, and relative if it does not.
     */
    FILE_SYSTEM("filesystem");

    /** The name */
    private final String realName;

    private NameScope(final String name) {
        this.realName = name;
    }

    /**
     * Returns the name of the scope.
     *
     * @return The name of the scope.
     */
    @Override
    public String toString() {
        return realName;
    }

    /**
     * Returns the name of the scope.
     *
     * @return The name of the scope.
     */
    public String getName() {
        return realName;
    }
}
