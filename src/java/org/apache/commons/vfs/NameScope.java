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
 * An enumerated type for file name scope, used when resolving a name relative
 * to a file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2002/04/07 02:27:55 $
 */
public final class NameScope
{
    /**
     * Resolve against the children of the base file.  The name is resolved
     * as described by {@link #FILE_SYSTEM}.  However, an exception is
     * thrown if the resolved file is not a direct child of the base file.
     */
    public static final NameScope CHILD = new NameScope( "child" );

    /**
     * Resolve against the descendents of the base file.  The name is resolved
     * as described by {@link #FILE_SYSTEM}.  However, an exception is thrown
     * if the resolved file is not a descendent of the base file.
     */
    public static final NameScope DESCENDENT = new NameScope( "descendent" );

    /**
     * Resolve against the descendents of the base file.  The name is resolved
     * as described by {@link #FILE_SYSTEM}.  However, an exception is thrown
     * if the resolved file is not a descendent of the base file, or the base
     * files itself.
     */
    public static final NameScope DESCENDENT_OR_SELF =
        new NameScope( "descendent_or_self" );

    /**
     * Resolve against files in the same file system as the base file.
     *
     * <p>If the supplied name is an absolute path, then it is resolved
     * relative to the root of the file system that the base file belongs to.
     * If a relative name is supplied, then it is resolved relative to the base
     * file.
     *
     * <p>The path may use any mix of <code>/</code>, <code>\</code>, or file
     * system specific separators to separate elements in the path.  It may
     * also contain <code>.</code> and <code>..</code> elements.
     *
     * <p>A path is considered absolute if it starts with a separator character,
     * and relative if it does not.
     */
    public static final NameScope FILE_SYSTEM = new NameScope( "filesystem" );

    private final String name;

    private NameScope( final String name )
    {
        this.name = name;
    }

    /** Returns the name of the scope. */
    public String toString()
    {
        return name;
    }

    /** Returns the name of the scope. */
    public String getName()
    {
        return name;
    }
}
