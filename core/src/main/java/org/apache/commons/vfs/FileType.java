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
 * An enumerated type that represents a file's type.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public final class FileType
{
    /**
     * A folder.  May contain other files, and have attributes, but does not
     * have any data content.
     */
    public static final FileType FOLDER = new FileType("folder", true, false, true);

    /**
     * A regular file.  May have data content and attributes, but cannot
     * contain other files.
     */
    public static final FileType FILE = new FileType("file", false, true, true);

	/**
	 * A file or folder.  May have data content and attributes, and can
	 * contain other files.
	 */
	public static final FileType FILE_OR_FOLDER = new FileType("fileOrFolder", true, true, true);

	/**
     * A file that does not exist.  May not have data content, attributes,
     * or contain other files.
     */
    public static final FileType IMAGINARY = new FileType("imaginary", false, false, false);

    private final String name;
    private final boolean hasChildren;
    private final boolean hasContent;
    private final boolean hasAttrs;

    private FileType(final String name,
                     final boolean hasChildren,
                     final boolean hasContent,
                     final boolean hasAttrs)
    {
        this.name = name;
        this.hasChildren = hasChildren;
        this.hasContent = hasContent;
        this.hasAttrs = hasAttrs;
    }

    /**
     * Returns the name of this type.
     */
    public String toString()
    {
        return name;
    }

    /**
     * Returns the name of this type.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns true if files of this type may contain other files.
     */
    public boolean hasChildren()
    {
        return hasChildren;
    }

    /**
     * Returns true if files of this type may have data content.
     */
    public boolean hasContent()
    {
        return hasContent;
    }

    /**
     * Returns true if files of this type may have attributes.
     */
    public boolean hasAttributes()
    {
        return hasAttrs;
    }
}
