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

import java.io.File;
import java.net.URLStreamHandlerFactory;

/**
 * A FileSystemManager manages a set of file systems.  This interface is
 * used to locate a {@link FileObject} by name from one of those file systems.
 *
 * <p>To locate a {@link FileObject}, use one of the <code>resolveFile()</code>
 * methods.</p>
 *
 * <h4><a name="naming">File Naming</a></h4>
 *
 * <p>A file system manager can recognise several types of file names:
 *
 * <ul>
 *
 * <li><p>Absolute URI.  These must start with a scheme, such as
 * <code>file:</code> or <code>ftp:</code>, followed by a scheme dependent
 * file name.  Some examples:</p>
 * <pre>
 * file:/c:/somefile
 * ftp://somewhere.org/somefile
 * </pre>
 *
 * <li><p>Absolute local file name.  For example,
 * <code>/home/someuser/a-file</code> or <code>c:\dir\somefile.html</code>.
 * Elements in the name can be separated using any of the following
 * characters: <code>/</code>, <code>\</code>, or the native file separator
 * character. For example, the following file names are the same:</p>
 * <pre>
 * c:\somedir\somefile.xml
 * c:/somedir/somefile.xml
 * </pre>
 *
 * <li><p>Relative path.  For example: <code>../somefile</code> or
 * <code>somedir/file.txt</code>.   The file system manager resolves relative
 * paths against its <i>base file</i>.  Elements in the relative path can be
 * separated using <code>/</code>, <code>\</code>, or file system specific
 * separator characters.  Relative paths may also contain <code>..</code> and
 * <code>.</code> elements.  See {@link FileObject#resolveFile} for more
 * details.</p>
 *
 * </ul>
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.9 $ $Date: 2002/06/17 00:06:16 $
 */
public interface FileSystemManager
{
    /**
     * Returns the base file used to resolve relative paths.
     */
    FileObject getBaseFile();

    /**
     * Locates a file by name.  Equivalent to calling
     * <code>resolveFile(uri, getBaseName())</code>.
     *
     * @param name
     *          The name of the file.
     *
     * @return The file.  Never returns null.
     *
     * @throws FileSystemException
     *          On error parsing the file name.
     */
    FileObject resolveFile( String name )
        throws FileSystemException;

    /**
     * Locates a file by name.  The name is resolved as described
     * <a href="#naming">above</a>.  That is, the name can be either
     * an absolute URI, an absolute file name, or a relative path to
     * be resolved against <code>baseFile</code>.
     *
     * <p>Note that the file does not have to exist when this method is called.
     *
     * @param name
     *          The name of the file.
     *
     * @param baseFile
     *          The base file to use to resolve relative paths.  May be null.
     *
     * @return The file.  Never returns null.
     *
     * @throws FileSystemException
     *          On error parsing the file name.
     */
    FileObject resolveFile( FileObject baseFile, String name )
        throws FileSystemException;

    /**
     * Locates a file by name.  See {@link #resolveFile(FileObject, String)}
     * for details.
     *
     * @param baseFile
     *          The base file to use to resolve relative paths.  May be null.
     *
     * @param name
     *          The name of the file.
     *
     * @return The file.  Never returns null.
     *
     * @throws FileSystemException
     *          On error parsing the file name.
     *
     */
    FileObject resolveFile( File baseFile, String name )
        throws FileSystemException;

    /**
     * Converts a local file into a {@link FileObject}.
     *
     * @param file
     *          The file to convert.
     *
     * @return
     *          The {@link FileObject} that represents the local file.  Never
     *          returns null.
     *
     * @throws FileSystemException
     *          On error converting the file.
     */
    FileObject toFileObject( File file )
        throws FileSystemException;

    /**
     * Creates a layered file system.  A layered file system is a file system
     * that is created from the contents of another file, such as a zip
     * or tar file.
     *
     * @param provider
     *          The name of the file system provider to use.  This name is
     *          the same as the scheme used in URI to identify the provider.
     *
     * @param file
     *          The file to use to create the file system.
     *
     * @return
     *          The root file of the new file system.
     *
     * @throws FileSystemException
     *          On error creating the file system.
     */
    FileObject createFileSystem( String provider, FileObject file )
        throws FileSystemException;

    /**
     * Creates an empty virtual file system.  Can be populated by adding
     * junctions to it.
     *
     * @param rootUri
     *          The root URI to use for the new file system.  Can be null.
     *
     * @return
     *          The root file of the new file system.
     */
    FileObject createFileSystem( String rootUri )
        throws FileSystemException;

    /**
     * Creates a virtual file system.  The file system will contain a junction
     * at the fs root to the supplied root file.
     *
     * @param rootFile The root file to backs the file system.
     *
     * @return
     *          The root of the new file system.
     */
    FileObject createFileSystem( FileObject rootFile )
        throws FileSystemException;

    /**
     * Returns a streamhandler factory to enable URL lookup using this
     * FileSystemManager.
     */
    URLStreamHandlerFactory getURLStreamHandlerFactory();
}
