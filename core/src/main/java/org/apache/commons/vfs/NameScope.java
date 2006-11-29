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
package org.apache.commons.vfs;

/**
 * An enumerated type for file name scope, used when resolving a name relative
 * to a file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public final class NameScope
{
    /**
     * Resolve against the children of the base file.  The name is resolved
     * as described by {@link #FILE_SYSTEM}.  However, an exception is
     * thrown if the resolved file is not a direct child of the base file.
     */
    public static final NameScope CHILD = new NameScope("child");

    /**
     * Resolve against the descendents of the base file.  The name is resolved
     * as described by {@link #FILE_SYSTEM}.  However, an exception is thrown
     * if the resolved file is not a descendent of the base file.
     */
    public static final NameScope DESCENDENT = new NameScope("descendent");

    /**
     * Resolve against the descendents of the base file.  The name is resolved
     * as described by {@link #FILE_SYSTEM}.  However, an exception is thrown
     * if the resolved file is not a descendent of the base file, or the base
     * files itself.
     */
    public static final NameScope DESCENDENT_OR_SELF =
        new NameScope("descendent_or_self");

    /**
     * Resolve against files in the same file system as the base file.
     * <p/>
     * <p>If the supplied name is an absolute path, then it is resolved
     * relative to the root of the file system that the base file belongs to.
     * If a relative name is supplied, then it is resolved relative to the base
     * file.
     * <p/>
     * <p>The path may use any mix of <code>/</code>, <code>\</code>, or file
     * system specific separators to separate elements in the path.  It may
     * also contain <code>.</code> and <code>..</code> elements.
     * <p/>
     * <p>A path is considered absolute if it starts with a separator character,
     * and relative if it does not.
     */
    public static final NameScope FILE_SYSTEM = new NameScope("filesystem");

    private final String name;

    private NameScope(final String name)
    {
        this.name = name;
    }

    /**
     * Returns the name of the scope.
     */
    public String toString()
    {
        return name;
    }

    /**
     * Returns the name of the scope.
     */
    public String getName()
    {
        return name;
    }
}
