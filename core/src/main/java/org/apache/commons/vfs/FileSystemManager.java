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

import org.apache.commons.logging.Log;

import java.io.File;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;
import java.lang.reflect.Constructor;

import org.apache.commons.vfs.operations.FileOperationProvider;

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
     * @return The base FileObject.
     * @throws FileSystemException if an error occurs.
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
    FileObject resolveFile(String name) throws FileSystemException;

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

    /**                               §
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
    FileObject resolveFile(FileObject baseFile, String name) throws FileSystemException;

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
    FileObject resolveFile(File baseFile, String name) throws FileSystemException;

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
    FileObject toFileObject(File file) throws FileSystemException;

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
     * Closes the given filesystem.<br />
     * If you use VFS as singleton it is VERY dangerous to call this method.
     * @param filesystem The FileSystem to close.
     */
    void closeFileSystem(FileSystem filesystem);

    /**
     * Creates a layered file system.  A layered file system is a file system
     * that is created from the contents of a file, such as a zip or tar file.
     *
     * @param file The file to use to create the file system.
     * @return The root file of the new file system.
     * @throws FileSystemException On error creating the file system.
     */
    FileObject createFileSystem(FileObject file) throws FileSystemException;

    /**
     * Creates an empty virtual file system.  Can be populated by adding
     * junctions to it.
     *
     * @param rootUri The root URI to use for the new file system.  Can be null.
     * @return The root file of the new file system.
     * @throws FileSystemException if an error occurs creating the VirtualFileSystem.
     */
    FileObject createVirtualFileSystem(String rootUri) throws FileSystemException;

    /**
     * Creates a virtual file system.  The file system will contain a junction
     * at the fs root to the supplied root file.
     *
     * @param rootFile The root file to backs the file system.
     * @return The root of the new file system.
     * @throws FileSystemException if an error occurs creating the VirtualFileSystem.
     */
    FileObject createVirtualFileSystem(FileObject rootFile) throws FileSystemException;

    /**
     * Returns a streamhandler factory to enable URL lookup using this
     * FileSystemManager.
     * @return the URLStreamHandlerFactory.
     */
    URLStreamHandlerFactory getURLStreamHandlerFactory();

    /**
     * Determines if a layered file system can be created for a given file.
     *
     * @param file The file to check for.
     * @return true if the FileSystem can be created.
     * @throws FileSystemException if an error occurs.
     */
    boolean canCreateFileSystem(FileObject file) throws FileSystemException;

    /**
     * Get the cache used to cache fileobjects.
     * @return The FilesCache.
     */
    FilesCache getFilesCache();

    /**
     * Get the cache strategy used.
     * @return the CacheStrategy.
     */
    CacheStrategy getCacheStrategy();

    /**
     * Get the file object decorator used.
     * @return the file object decorator Class.
     */
    Class getFileObjectDecorator();

    /**
     * The constructor associated to the fileObjectDecorator.
     * We cache it here for performance reasons.
     * @return the Constructor associated with the FileObjectDecorator.
     */
    Constructor getFileObjectDecoratorConst();

    /**
     * The class to use to determine the content-type (mime-type).
     * @return the FileContentInfoFactory.
     */
    FileContentInfoFactory getFileContentInfoFactory();

    /**
     * Returns true if this manager has a provider for a particular scheme.
     * @param scheme The scheme for which a provider should be checked.
     * @return true if a provider for the scheme is available.
     */
    boolean hasProvider(final String scheme);

    /**
     * Get the schemes currently available.
     * @return An array of available scheme names that are supported.
     */
    String[] getSchemes();

    /**
     * Get the capabilities for a given scheme.
     *
     * @param scheme The scheme to use to locate the provider's capabilities.
     * @return A Collection of the various capabilities.
     * @throws FileSystemException if the given scheme is not konwn.
     */
    Collection getProviderCapabilities(final String scheme) throws FileSystemException;

    /**
     * Sets the logger to use.
     * @param log The logger to use.
     */
    void setLogger(final Log log);

    /**
     * Get the configuration builder for the given scheme.
     *
     * @param scheme The schem to use to obtain the FileSystemConfigBuidler.
     * @return A FileSystemConfigBuilder appropriate for the given scheme.
     * @throws FileSystemException if the given scheme is not konwn.
     */
    FileSystemConfigBuilder getFileSystemConfigBuilder(final String scheme) throws FileSystemException;

    /**
     * Resolve the uri to a filename.
     *
     * @param uri The uri to resolve.
     * @return A FileName that matches the uri.
     * @throws FileSystemException if this is not possible.
     */
    FileName resolveURI(String uri) throws FileSystemException;

    // -- OPERATIONS --
    /**
     * Adds the specified FileOperationProvider for the specified scheme.
     * Several FileOperationProvider's might be registered for the same scheme.
     * For example, for "file" scheme we can register SvnWsOperationProvider and
     * CvsOperationProvider.
     *
     * @param scheme The scheme assoicated with this provider.
     * @param operationProvider The FileOperationProvider to add.
     * @throws FileSystemException if an error occurs.
     */
    void addOperationProvider(final String scheme, final FileOperationProvider operationProvider)
        throws FileSystemException;

    /**
     * @see FileSystemManager#addOperationProvider(String, org.apache.commons.vfs.operations.FileOperationProvider).
     *
     * @param schemes The schemes that will be associated with the provider.
     * @param operationProvider The FileOperationProvider to add.
     * @throws FileSystemException if an error occurs.
     */
    void addOperationProvider(final String[] schemes, final FileOperationProvider operationProvider)
        throws FileSystemException;


    /**
     * @param scheme the scheme for wich we want to get the list af registered providers.
     *
     * @return the registered FileOperationProviders for the specified scheme.
     * If there were no providers registered for the scheme, it returns null.
     *
     * @throws FileSystemException if an error occurs.
     */
    FileOperationProvider[] getOperationProviders(final String scheme) throws FileSystemException;
}
