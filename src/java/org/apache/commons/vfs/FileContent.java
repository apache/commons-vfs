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

import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.Map;

/**
 * Represents the data content of a file.
 *
 * <p>To read from a file, use the <code>InputStream</code> returned by
 * {@link #getInputStream}.
 *
 * <p>To write to a file, use the <code>OutputStream</code> returned by
 * {@link #getOutputStream} method.  This will create the file, and the parent
 * folder, if necessary.
 *
 * <p>To prevent concurrency problems, a file may not have an OutputStream and
 * an InputStream open at the same time.  A file may have multiple InputStreams
 * open at the sametime.
 *
 * @see FileObject#getContent
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/04/07 02:27:55 $
 */
public interface FileContent
{
    /**
     * Returns the file which this is the content of.
     */
    FileObject getFile();

    /**
     * Determines the size of the file, in bytes.
     *
     * @return
     *      The size of the file, in bytes.
     *
     * @throws FileSystemException
     *      If the file does not exist, or is being written to, or on error
     *      determining the size.
     */
    long getSize() throws FileSystemException;

    /**
     * Determines the last-modified timestamp of the file.
     *
     * @return
     *      The last-modified timestamp.
     *
     * @throws FileSystemException
     *      If the file does not exist, or is being written to, or on error
     *      determining the last-modified timestamp.
     */
    long getLastModifiedTime() throws FileSystemException;

    /**
     * Sets the last-modified timestamp of the file.  Creates the file if
     * it does not exist.
     *
     * @param modTime
     *      The time to set the last-modified timestamp to.
     *
     * @throws FileSystemException
     *      If the file is read-only, or is being written to, or on error
     *      setting the last-modified timestamp.
     */
    void setLastModifiedTime( long modTime ) throws FileSystemException;

    /**
     * Returns a read-only map of this file's attributes.
     *
     * @throws FileSystemException
     *      If the file does not exist, or does not support attributes.
     */
    Map getAttributes() throws FileSystemException;

    /**
     * Lists the attributes of the file's content.
     *
     * @return
     *      The names of the attributes.  Never returns null;
     *
     * @throws FileSystemException
     *      If the file does not exist, or does not support attributes.
     */
    String[] getAttributeNames() throws FileSystemException;

    /**
     * Gets the value of an attribute of the file's content.
     *
     * @param attrName
     *      The name of the attribute.  Attribute names are case insensitive.
     *
     * @return
     *      The value of the attribute, or null if the attribute value is
     *      unknown.
     *
     * @throws FileSystemException
     *      If the file does not exist, or does not support attributes.
     */
    Object getAttribute( String attrName ) throws FileSystemException;

    /**
     * Sets the value of an attribute of the file's content.  Creates the
     * file if it does not exist.
     *
     * @param attrName
     *      The name of the attribute.
     *
     * @param value
     *      The value of the attribute.
     *
     * @throws FileSystemException
     *      If the file does not exist, or is read-only, or does not support
     *      attributes, or on error setting the attribute.
     */
    void setAttribute( String attrName, Object value )
        throws FileSystemException;

    /**
     * Retrieves the certificates if any used to sign this file or folder.
     *
     * @return
     *      The certificates, or an empty array if there are no certificates or
     *      the file does not support signing.
     *
     * @throws FileSystemException
     *      If the file does not exist, or is being written.
     */
    Certificate[] getCertificates() throws FileSystemException;

    /**
     * Returns an input stream for reading the file's content.
     *
     * <p>There may only be a single input or output stream open for the
     * file at any time.
     *
     * @return
     *      An input stream to read the file's content from.  The input
     *      stream is buffered, so there is no need to wrap it in a
     *      <code>BufferedInputStream</code>.
     *
     * @throws FileSystemException
     *      If the file does not exist, or is being read, or is being written,
     *      or on error opening the stream.
     */
    InputStream getInputStream() throws FileSystemException;

    /**
     * Returns an output stream for writing the file's content.
     *
     * If the file does not exist, this method creates it, and the parent
     * folder, if necessary.  If the file does exist, it is replaced with
     * whatever is written to the output stream.
     *
     * <p>There may only be a single input or output stream open for the
     * file at any time.
     *
     * @return
     *      An output stream to write the file's content to.  The stream is
     *      buffered, so there is no need to wrap it in a
     *      <code>BufferedOutputStream</code>.
     *
     * @throws FileSystemException
     *      If the file is read-only, or is being read, or is being written,
     *      or on error opening the stream.
     */
    OutputStream getOutputStream() throws FileSystemException;

    /**
     * Closes all resources used by the content, including any open stream.
     * Commits pending changes to the file.
     *
     * <p>This method is a hint to the implementation that it can release
     * resources.  This object can continue to be used after calling this
     * method.
     */
    void close() throws FileSystemException;
}
