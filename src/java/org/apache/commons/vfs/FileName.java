/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
 * Represents a file name.  File names are immutable, and work correctly as
 * keys in hash tables.
 *
 * @see FileObject
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/04/07 02:27:55 $
 */
public interface FileName
{
    /**
     * The absolute path of the root of a file system.
     */
    String ROOT_PATH = "/";

    /**
     * Returns the base name of this file.  The base name is the last element
     * of the file name.  For example the base name of
     * <code>/somefolder/somefile</code> is <code>somefile</code>.
     *
     * <p>The root file of a file system has an empty base name.
     *
     * @return The base name.  Never returns null.
     */
    String getBaseName();

    /**
     * Returns the absolute path of this file, within its file system.  This
     * path is normalised, so that <code>.</code> and <code>..</code> elements
     * have been removed.  Also, the path only contains <code>/</code> as its
     * separator character.  The path always starts with <code>/</code>
     *
     * <p>The root of a file system has <code>/</code> as its absolute path.
     *
     * @return The path.  Never returns null.
     */
    String getPath();

    /**
     * Returns the depth of this file name, within its file system.  The depth
     * of the root of a file system is 0.  The depth of any other file is
     * 1 + the depth of its parent.
     */
    int getDepth();

    /**
     * Returns the absolute URI of this file.
     */
    String getURI();

    /**
     * Returns the root URI of the file system this file belongs to.
     */
    String getRootURI();

    /**
     * Returns the file name of the parent of this file.  The root of a
     * file system has no parent.
     *
     * @return
     *      A {@link FileName} object representing the parent name.  Returns
     *      null for the root of a file system.
     */
    FileName getParent();

    /**
     * Resolves a name, relative to this file name.  Equivalent to calling
     * <code>resolveName( path, NameScope.FILE_SYSTEM )</code>.
     *
     * @param name
     *      The name to resolve.
     *
     * @return
     *      A {@link FileName} object representing the resolved file name.
     *
     * @throws FileSystemException
     *      If the name is invalid.
     */
    FileName resolveName( String name ) throws FileSystemException;

    /**
     * Resolves a name, relative to this file name.  Refer to {@link NameScope}
     * for a description of how names are resolved.
     *
     * @param name
     *      The name to resolve.
     *
     * @param scope
     *      The scope to use when resolving the name.
     *
     * @return
     *      A {@link FileName} object representing the resolved file name.
     *
     * @throws FileSystemException
     *      If the name is invalid.
     */
    FileName resolveName( String name, NameScope scope )
        throws FileSystemException;

    /**
     * Converts a file name to a relative name, relative to this file name.
     *
     * @param name
     *      The name to convert to a relative path.
     *
     * @return
     *      The relative name.
     *
     * @throws FileSystemException
     *      On error.
     */
    String getRelativeName( FileName name ) throws FileSystemException;

    /**
     * Determines if another file name is an ancestor of this file name.
     */
    boolean isAncestor( FileName ancestor );

    /**
     * Determines if another file name is a descendent of this file name.
     */
    boolean isDescendent( FileName descendent );

    /**
     * Determines if another file name is a descendent of this file name.
     */
    boolean isDescendent( FileName descendent, NameScope nameScope );
}
