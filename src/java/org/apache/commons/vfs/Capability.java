/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002, 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.vfs;

/**
 * An enumerated type representing the capabilities of files and file systems.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2003/10/13 08:45:23 $
 */
public final class Capability
{
    /** File content can be read. */
    public static final Capability READ_CONTENT = new Capability( "READ_CONTENT" );

    /** File content can be written. */
    public static final Capability WRITE_CONTENT = new Capability( "WRITE_CONTENT" );

    /** File attributes are supported. */
    public static final Capability ATTRIBUTES = new Capability( "ATTRIBUTES" );

    /** File last-modified time is supported.
     * @deprecated use GET_LAST_MODIFIED and SET_LAST_MODIFIED */
    public static final Capability LAST_MODIFIED = new Capability( "LAST_MODIFIED" );

    /** File get last-modified time is supported. */
    public static final Capability GET_LAST_MODIFIED = new Capability( "GET_LAST_MODIFIED" );

    /** File set last-modified time is supported. */
    public static final Capability SET_LAST_MODIFIED = new Capability( "SET_LAST_MODIFIED" );

    /** File content signing is supported. */
    public static final Capability SIGNING = new Capability( "SIGNING" );

    /** Files can be created. */
    public static final Capability CREATE = new Capability( "CREATE" );

    /** Files can be deleted. */
    public static final Capability DELETE = new Capability( "DELETE" );

    /** The file type can be determined. */
    public static final Capability GET_TYPE = new Capability( "GET_TYPE" );

    /** Children of files can be listed. */
    public static final Capability LIST_CHILDREN = new Capability( "LIST_CHILDREN" );

    /**
     * URI are supported.  Files without this capability use URI that do not
     * globally and uniquely identify the file.
     */
    public static final Capability URI = new Capability( "URI" );

    /** File system attributes are supported. */
    public static final Capability FS_ATTRIBUTES = new Capability( "FS_ATTRIBUTE" );

    /** Junctions are supported. */
    public static final Capability JUNCTIONS = new Capability( "JUNCTIONS" );

    /**
     * The set of attributes defined by the Jar manifest specification are
     * supported.  The attributes aren't necessarily stored in a manifest file.
     */
    public static final Capability MANIFEST_ATTRIBUTES = new Capability( "MANIFEST_ATTRIBUTES" );

    private final String name;

    private Capability( final String name )
    {
        this.name = name;
    }

    public String toString()
    {
        return name;
    }
}
