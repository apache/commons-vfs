/*
 * Copyright 2002-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.commons.logging.Log;

import java.io.File;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;

/**
 * A FileSystemManager manages a set of file systems.  This interface is
 * used to locate a {@link FileObject} by name from one of those file systems.
 * <p/>
 * <p>To locate a {@link FileObject}, use one of the <code>resolveFile()</code>
 * methods.</p>
 * <p/>
 * <h4><a name="naming">File Naming</a></h4>
 * <p/>
 * <p>A file system manager can recognise several types of file names:
 * <p/>
 * <ul>
 * <p/>
 * <li><p>Absolute URI.  These must start with a scheme, such as
 * <code>file:</code> or <code>ftp:</code>, followed by a scheme dependent
 * file name.  Some examples:</p>
 * <pre>
 * file:/c:/somefile
 * ftp://somewhere.org/somefile
 * </pre>
 * <p/>
 * <li><p>Absolute local file name.  For example,
 * <code>/home/someuser/a-file</code> or <code>c:\dir\somefile.html</code>.
 * Elements in the name can be separated using any of the following
 * characters: <code>/</code>, <code>\</code>, or the native file separator
 * character. For example, the following file names are the same:</p>
 * <pre>
 * c:\somedir\somefile.xml
 * c:/somedir/somefile.xml
 * </pre>
 * <p/>
 * <li><p>Relative path.  For example: <code>../somefile</code> or
 * <code>somedir/file.txt</code>.   The file system manager resolves relative
 * paths against its <i>base file</i>.  Elements in the relative path can be
 * separated using <code>/</code>, <code>\</code>, or file system specific
 * separator characters.  Relative paths may also contain <code>..</code> and
 * <code>.</code> elements.  See {@link FileObject#resolveFile} for more
 * details.</p>
 * <p/>
 * </ul>
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public interface FileSystemManager
{
    /**
     * Returns the base file used to resolve relative paths.
     */
    FileObject getBaseFile() throws FileSystemException;

    /**
     * Locates a file by name.  Equivalent to calling
     * <code>resolveFile(uri, getBaseName())</code>.
     *
     * @param name The name of the file.
     * @return The file.  Never returns null.
     * @throws FileSystemException On error parsing the file name.
     */
    FileObject resolveFile(String name)
        throws FileSystemException;

    /**
     * Locates a file by name.  Equivalent to calling
     * <code>resolveFile(uri, getBaseName())</code>.
     *
     * @param name              The name of the file.
     * @param fileSystemOptions The FileSystemOptions used for FileSystem creation
     * @return The file.  Never returns null.
     * @throws FileSystemException On error parsing the file name.
     */
    FileObject resolveFile(String name, FileSystemOptions fileSystemOptions)
        throws FileSystemException;

    /**
     * Locates a file by name.  The name is resolved as described
     * <a href="#naming">above</a>.  That is, the name can be either
     * an absolute URI, an absolute file name, or a relative path to
     * be resolved against <code>baseFile</code>.
     * <p/>
     * <p>Note that the file does not have to exist when this method is called.
     *
     * @param name     The name of the file.
     * @param baseFile The base file to use to resolve relative paths.
     *                 May be null.
     * @return The file.  Never returns null.
     * @throws FileSystemException On error parsing the file name.
     */
    FileObject resolveFile(FileObject baseFile, String name)
        throws FileSystemException;

    /**
     * Locates a file by name.  See {@link #resolveFile(FileObject, String)}
     * for details.
     *
     * @param baseFile The base file to use to resolve relative paths.
     *                 May be null.
     * @param name     The name of the file.
     * @return The file.  Never returns null.
     * @throws FileSystemException On error parsing the file name.
     */
    FileObject resolveFile(File baseFile, String name)
        throws FileSystemException;

    /**
     * Resolves a name, relative to this file name.  Equivalent to calling
     * <code>resolveName( path, NameScope.FILE_SYSTEM )</code>.
     *
     * @param root the base filename
     * @param name The name to resolve.
     * @return A {@link FileName} object representing the resolved file name.
     * @throws FileSystemException If the name is invalid.
     */
    FileName resolveName(final FileName root, final String name) throws FileSystemException;

    /**
     * Resolves a name, relative to the "root" file name.  Refer to {@link NameScope}
     * for a description of how names are resolved.
     *
     * @param root the base filename
     * @param name  The name to resolve.
     * @param scope The {@link NameScope} to use when resolving the name.
     * @return A {@link FileName} object representing the resolved file name.
     * @throws FileSystemException If the name is invalid.
     */
    FileName resolveName(final FileName root, String name, NameScope scope)
        throws FileSystemException;

    /**
     * Converts a local file into a {@link FileObject}.
     *
     * @param file The file to convert.
     * @return The {@link FileObject} that represents the local file.  Never
     *         returns null.
     * @throws FileSystemException On error converting the file.
     */
    FileObject toFileObject(File file)
        throws FileSystemException;

    /**
     * Creates a layered file system.  A layered file system is a file system
     * that is created from the contents of a file, such as a zip or tar file.
     *
     * @param provider The name of the file system provider to use.  This name
     *                 is the same as the scheme used in URI to identify the provider.
     * @param file     The file to use to create the file system.
     * @return The root file of the new file system.
     * @throws FileSystemException On error creating the file system.
     */
    FileObject createFileSystem(String provider, FileObject file)
        throws FileSystemException;

    /**
     * Creates a layered file system.  A layered file system is a file system
     * that is created from the contents of a file, such as a zip or tar file.
     *
     * @param file The file to use to create the file system.
     * @return The root file of the new file system.
     * @throws FileSystemException On error creating the file system.
     */
    FileObject createFileSystem(FileObject file)
        throws FileSystemException;

    /**
     * Creates an empty virtual file system.  Can be populated by adding
     * junctions to it.
     *
     * @param rootUri The root URI to use for the new file system.  Can be null.
     * @return The root file of the new file system.
     */
    FileObject createVirtualFileSystem(String rootUri)
        throws FileSystemException;

    /**
     * Creates a virtual file system.  The file system will contain a junction
     * at the fs root to the supplied root file.
     *
     * @param rootFile The root file to backs the file system.
     * @return The root of the new file system.
     */
    FileObject createVirtualFileSystem(FileObject rootFile)
        throws FileSystemException;

    /**
     * Returns a streamhandler factory to enable URL lookup using this
     * FileSystemManager.
     */
    URLStreamHandlerFactory getURLStreamHandlerFactory();

    /**
     * Determines if a layered file system can be created for a given file.
     *
     * @param file The file to check for.
     */
    boolean canCreateFileSystem(FileObject file) throws FileSystemException;

    /**
     * Get the cache used to cache fileobjects.
     */
    FilesCache getFilesCache();

    /**
     * The class to use to determine the content-type (mime-type)
     */
    FileContentInfoFactory getFileContentInfoFactory();

    /**
     * Get the schemes currently available.
     */
    public String[] getSchemes();

    /**
     * Get the capabilities for a given scheme.
     *
     * @throws FileSystemException if the given scheme is not konwn
     */
    public Collection getProviderCapabilities(final String scheme) throws FileSystemException;

    /**
     * Sets the logger to use.
     */
    public void setLogger(final Log log);

    /**
     * Get the configuration builder for the given scheme
     *
     * @throws FileSystemException if the given scheme is not konwn
     */
    public FileSystemConfigBuilder getFileSystemConfigBuilder(final String scheme) throws FileSystemException;

    /**
     * Resolve the uri to a filename
     *
     * @throws FileSystemException if this is not possible
     */
    public FileName resolveURI(String uri) throws FileSystemException;
}
