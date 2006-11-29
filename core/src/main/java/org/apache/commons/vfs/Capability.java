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
 * An enumerated type representing the capabilities of files and file systems.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public final class Capability
{
    /**
     * File content can be read.
     */
    public static final Capability READ_CONTENT = new Capability("READ_CONTENT");

    /**
     * File content can be written.
     */
    public static final Capability WRITE_CONTENT = new Capability("WRITE_CONTENT");

    /**
     * File content can be read in random mode.<br>
     */
    public static final Capability RANDOM_ACCESS_READ = new Capability("RANDOM_ACCESS_READ");

    /**
     * File content can be written in random mode.<br>
     */
    public static final Capability RANDOM_ACCESS_WRITE = new Capability("RANDOM_ACCESS_WRITE");

    /**
     * File content can be appended.
     */
    public static final Capability APPEND_CONTENT = new Capability("APPEND_CONTENT");

    /**
     * File attributes are supported.
     */
    public static final Capability ATTRIBUTES = new Capability("ATTRIBUTES");

    /**
     * File last-modified time is supported.
     */
    public static final Capability LAST_MODIFIED = new Capability("LAST_MODIFIED");

    /**
     * File get last-modified time is supported.
     */
    public static final Capability GET_LAST_MODIFIED = new Capability("GET_LAST_MODIFIED");

    /**
     * File set last-modified time is supported.
     */
    public static final Capability SET_LAST_MODIFIED_FILE = new Capability("SET_LAST_MODIFIED_FILE");

    /**
     * folder set last-modified time is supported.
     */
    public static final Capability SET_LAST_MODIFIED_FOLDER = new Capability("SET_LAST_MODIFIED_FOLDER");

    /**
     * File content signing is supported.
     */
    public static final Capability SIGNING = new Capability("SIGNING");

    /**
     * Files can be created.
     */
    public static final Capability CREATE = new Capability("CREATE");

    /**
     * Files can be deleted.
     */
    public static final Capability DELETE = new Capability("DELETE");

    /**
     * Files can be renamed.
     */
    public static final Capability RENAME = new Capability("RENAME");

    /**
     * The file type can be determined.
     */
    public static final Capability GET_TYPE = new Capability("GET_TYPE");

    /**
     * Children of files can be listed.
     */
    public static final Capability LIST_CHILDREN = new Capability("LIST_CHILDREN");

    /**
     * URI are supported.  Files without this capability use URI that do not
     * globally and uniquely identify the file.
     */
    public static final Capability URI = new Capability("URI");

    /**
     * File system attributes are supported.
     */
    public static final Capability FS_ATTRIBUTES = new Capability("FS_ATTRIBUTE");

    /**
     * Junctions are supported.
     */
    public static final Capability JUNCTIONS = new Capability("JUNCTIONS");

    /**
     * The set of attributes defined by the Jar manifest specification are
     * supported.  The attributes aren't necessarily stored in a manifest file.
     */
    public static final Capability MANIFEST_ATTRIBUTES = new Capability("MANIFEST_ATTRIBUTES");

    /**
     * The provider itself do not provide a filesystem. It simply resolves a full name
     * and dispatches the request back to the filesystemmanager.<br>
     * A provider with this capability cant tell much about the capabilities about the
     * finally used filesystem in advance.
     */
    public static final Capability DISPATCHER = new Capability("DISPATCHER");

    /**
     * A compressed filesystem is a filesystem which use compression.
     */
    public static final Capability COMPRESS = new Capability("COMPRESS");

    /**
     * A virtual filesystem can be an archive like tar or zip.
     */
    public static final Capability VIRTUAL = new Capability("VIRTUAL");

    private final String name;

    private Capability(final String name)
    {
        this.name = name;
    }

    public String toString()
    {
        return name;
    }
}
