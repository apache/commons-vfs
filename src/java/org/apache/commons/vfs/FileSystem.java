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
 * A file system.
 *
 * <p>A file system can also implement {@link org.apache.commons.vfs.provider.VfsComponent}.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.5 $ $Date: 2002/10/25 03:59:09 $
 */
public interface FileSystem
{
    /**
     * Returns the root file of this file system.
     */
    FileObject getRoot() throws FileSystemException;

    /**
     * Returns the parent layer if this is a layered file system.
     * This returns null if this is not a layered file system.
     */
    FileObject getParentLayer() throws FileSystemException;

    /**
     * Gets the value of an attribute of the file system.
     *
     * <p>TODO - change to <code>Map getAttributes()</code> instead?
     *
     * <p>TODO - define the standard attribute names, and define which attrs
     * are guaranteed to be present.
     *
     * @see org.apache.commons.vfs.FileContent#getAttribute
     *
     * @param attrName
     *      The name of the attribute.
     *
     * @return
     *      The value of the attribute.
     *
     * @throws org.apache.commons.vfs.FileSystemException
     *      If the file does not exist, or is being written, or if the
     *      attribute is unknown.
     */
    Object getAttribute( String attrName ) throws FileSystemException;

    /**
     * Sets the value of an attribute of the file's content.  Creates the
     * file if it does not exist.
     *
     * @see FileContent#setAttribute
     *
     * @param attrName
     *      The name of the attribute.
     *
     * @param value
     *      The value of the attribute.
     *
     * @throws FileSystemException
     *      If the file is read-only, or is being read, or if the attribute
     *      is not supported, or on error setting the attribute.
     */
    void setAttribute( String attrName, Object value )
        throws FileSystemException;

    /**
     * Finds a file in this file system.
     *
     * @param name
     *          The name of the file.
     *
     * @return The file.  Never returns null.
     */
    FileObject resolveFile( FileName name ) throws FileSystemException;

    /**
     * Finds a file in this file system.
     *
     * @param name
     *          The name of the file.  This must be an absolute path.
     *
     * @return The file.  Never returns null.
     */
    FileObject resolveFile( String name ) throws FileSystemException;

    /**
     * Adds a listener on a file in this file system.
     *
     * @param file The file to attach the listener to.
     * @param listener The listener to add.
     */
    void addListener( FileObject file, FileListener listener );

    /**
     * Removes a listener from a file in this file system.
     *
     * @param file The file to remove the listener from.
     * @param listener The listener to remove.
     */
    void removeListener( FileObject file, FileListener listener );
}
