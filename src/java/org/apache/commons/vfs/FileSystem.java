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

import java.io.File;

/**
 * A file system, made up of a hierarchy of files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.11 $ $Date: 2003/10/13 08:45:23 $
 */
public interface FileSystem
{
    /**
     * Returns the root file of this file system.
     */
    FileObject getRoot() throws FileSystemException;

    /**
     * Determines if this file system has a particular capability.
     *
     * @param capability The capability to check for.
     *
     * @return true if this filesystem has the requested capability.
     *         Note that not all files in the file system may have the
     *         capability.
     *
     * @todo Move this to another interface, so that set of capabilities can be queried.
     */
    boolean hasCapability( Capability capability );

    /**
     * Returns the parent layer if this is a layered file system.
     *
     * @return The parent layer, or null if this is not a layered file system.
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

    /**
     * Adds a junction to this file system.  A junction is a link that attaches
     * the supplied file to a point in this file system, making it look like
     * part of the file system.
     *
     * @param junctionPoint The point in this file system to add the junction.
     * @param targetFile The file to link to.
     *
     * @throws FileSystemException
     *      If this file system does not support junctions, or the junction
     *      point or target file is invalid (the file system may not support
     *      nested junctions, for example).
     */
    void addJunction( String junctionPoint, FileObject targetFile )
        throws FileSystemException;

    /**
     * Removes a junction from this file system.
     *
     * @param junctionPoint The junction to remove.
     *
     * @throws FileSystemException
     *      On error removing the junction.
     */
    void removeJunction( String junctionPoint ) throws FileSystemException;

    /**
     * Creates a temporary local copy of a file and its descendents.  If
     * this file is already a local file, a copy is not made.
     *
     * <p>Note that the local copy may include additonal files, that were
     * not selected by the given selector.
     *
     * @todo Add options to indicate whether the caller is happy to deal with
     *       extra files being present locally (eg if the file has been
     *       replicated previously), or whether the caller expects only
     *       the selected files to be present.
     *
     * @param file The file to replicate.
     * @param selector The selector to use to select the files to replicate.
     * @return The local copy of this file.
     *
     * @throws FileSystemException
     *      If this file does not exist, or on error replicating the file.
     */
    File replicateFile( FileObject file, FileSelector selector )
        throws FileSystemException;

}
