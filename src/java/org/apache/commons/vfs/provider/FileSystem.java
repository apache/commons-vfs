/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

/**
 * A file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/04/07 02:27:56 $
 */
public interface FileSystem
{
    /**
     * Returns the root of this file system.
     */
    FileObject getRoot() throws FileSystemException;

    /**
     * Returns the parent layerer if this is a layered file system.
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
     * @throws FileSystemException
     *      If the file does not exist, or is being written, or if the
     *      attribute is unknown.
     */
    Object getAttribute( String attrName ) throws FileSystemException;

    /**
     * Sets the value of an attribute of the file's content.  Creates the
     * file if it does not exist.
     *
     * @see org.apache.commons.vfs.FileContent#setAttribute
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
    void setAttribute( String attrName, Object value ) throws FileSystemException;

    /**
     * Finds a file in this file system.
     *
     * @param name
     *          The name of the file.
     */
    FileObject findFile( FileName name ) throws FileSystemException;

    /**
     * Finds a file in this file system.
     *
     * @param name
     *          The name of the file.  This must be an absolute path.
     */
    FileObject findFile( String name ) throws FileSystemException;

    /**
     * Closes this file system.
     */
    void close();
}
