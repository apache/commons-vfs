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
package org.apache.commons.vfs.provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.*;
import org.apache.commons.vfs.cache.OnCallRefreshFileObject;
import org.apache.commons.vfs.events.AbstractFileChangeEvent;
import org.apache.commons.vfs.events.ChangedEvent;
import org.apache.commons.vfs.events.CreateEvent;
import org.apache.commons.vfs.events.DeleteEvent;
import org.apache.commons.vfs.util.Messages;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;

/**
 * A partial {@link org.apache.commons.vfs.FileSystem} implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public abstract class AbstractFileSystem
    extends AbstractVfsComponent
    implements FileSystem
{
    private static final Log LOG = LogFactory.getLog(AbstractFileSystem.class);

    /**
     * The "root" of the file system. This is always "/" so it isn't always the "real"
     * root.
     */
    private final FileName rootName;

    /**
     * The root URI of the file system. The base path specified as a file system option
     * when the file system was created.
     */
    private final String rootURI;

    private FileObject parentLayer;
    // private FileObject root;
    private final Collection caps = new HashSet();

    /**
     * Map from FileName to FileObject.
     */
    // private FilesCache files;

    /**
     * Map from FileName to an ArrayList of listeners for that file.
     */
    private final Map listenerMap = new HashMap();

    /**
     * FileSystemOptions used for configuration
     */
    private final FileSystemOptions fileSystemOptions;

    /**
     * How many fileObjects are handed out
     */
    private long useCount;


    private FileSystemKey cacheKey;

    /**
     * open streams counter for this filesystem
     */
    private int openStreams;

    protected AbstractFileSystem(final FileName rootName,
                                 final FileObject parentLayer,
                                 final FileSystemOptions fileSystemOptions)
    {
        this(rootName, parentLayer, fileSystemOptions, null);
    }

    protected AbstractFileSystem(final FileName rootName,
                                 final FileObject parentLayer,
                                 final FileSystemOptions fileSystemOptions,
                                 final Class<? extends FileSystemOptions> optionsClass)
    {
        // this.parentLayer = parentLayer;
        this.parentLayer = parentLayer;
        this.rootName = rootName;
        if (optionsClass == null)
        {
            this.fileSystemOptions = fileSystemOptions == null ? new PrivateFileSystemOptions() : fileSystemOptions;
        }
        else
        {
            if (fileSystemOptions != null)
            {
                this.fileSystemOptions = FileSystemOptions.makeSpecific(optionsClass, fileSystemOptions);
            }
            else
            {
                try
                {
                    this.fileSystemOptions = optionsClass.newInstance();
                }
                catch (Exception ex)
                {
                    throw new IllegalArgumentException("Invalid optiosn class", ex);
                }
            }
        }
        String uri = ((DefaultFileSystemOptions)this.fileSystemOptions).getRootURI();
        if (uri == null)
        {
            uri = rootName.getURI();
        }
        this.rootURI = uri;
    }

    /**
     * Initialises this component.
     * @throws FileSystemException if an error occurs.
     */
    public void init() throws FileSystemException
    {
        addCapabilities(caps);
    }

    /**
     * Closes this component.
     */
    public void close()
    {
        closeCommunicationLink();

        parentLayer = null;
    }

    /**
     * Close the underlaying link used to access the files.
     */
    public void closeCommunicationLink()
    {
        synchronized (this)
        {
            doCloseCommunicationLink();
        }
    }

    /**
     * Close the underlaying link used to access the files
     */
    protected void doCloseCommunicationLink()
    {
    }

    /**
     * Creates a file object.  This method is called only if the requested
     * file is not cached.
     */
    protected abstract FileObject createFile(final FileName name)
        throws Exception;

    /**
     * Adds the capabilities of this file system.
     */
    protected abstract void addCapabilities(Collection caps);

    /**
     * Returns the name of the root of this file system.
     * @return the root FileName.
     */
    public FileName getRootName()
    {
        return rootName;
    }

    /**
     * Returns the root URI specified for this file System.
     * @return The root URI used in this file system.
     */
    public String getRootURI()
    {
        return rootURI;
    }

    /**
     * Adds a file object to the cache.
     */
    protected void putFileToCache(final FileObject file)
    {
        getCache().putFile(file);
        // files.put(file.getName(), file);
    }

    private FilesCache getCache()
    {
        FilesCache files;
        //if (this.files == null)
        //{
            files = getContext().getFileSystemManager().getFilesCache();
            if (files == null)
            {
                throw new RuntimeException(Messages.getString("vfs.provider/files-cache-missing.error"));
            }
        //}

        return files;
    }

    /**
     * Returns a cached file.
     */
    protected FileObject getFileFromCache(final FileName name)
    {
        return getCache().getFile(this, name);
        // return (FileObject) files.get(name);
    }

    /**
     * remove a cached file.
     */
    protected void removeFileFromCache(final FileName name)
    {
        getCache().removeFile(this, name);
    }

    /**
     * Determines if this file system has a particular capability.
     * @param capability the Capability to check for.
     * @return true if the FileSystem has the Capability, false otherwise.
     */
    public boolean hasCapability(final Capability capability)
    {
        return caps.contains(capability);
    }

    /**
     * Retrieves the attribute with the specified name. The default
     * implementation simply throws an exception.
     * @param attrName The name of the attribute.
     * @return the Object associated with the attribute or null if no object is.
     * @throws FileSystemException if an error occurs.
     */
    public Object getAttribute(final String attrName) throws FileSystemException
    {
        throw new FileSystemException("vfs.provider/get-attribute-not-supported.error");
    }

    /**
     * Sets the attribute with the specified name. The default
     * implementation simply throws an exception.
     * @param attrName the attribute name.
     * @param value The object to associate with the attribute.
     * @throws FileSystemException if an error occurs.
     */
    public void setAttribute(final String attrName, final Object value)
        throws FileSystemException
    {
        throw new FileSystemException("vfs.provider/set-attribute-not-supported.error");
    }

    /**
     * Returns the parent layer if this is a layered file system.
     * @return The FileObject for the parent layer.
     * @throws FileSystemException if an error occurs.
     */
    public FileObject getParentLayer() throws FileSystemException
    {
        return parentLayer;
    }

    /**
     * Returns the root file of this file system.
     * @return The root FileObject of the FileSystem
     * @throws FileSystemException if an error occurs.
     */
    public FileObject getRoot() throws FileSystemException
    {
        return resolveFile(rootName);
        /*
        if (root == null)
        {
            root = resolveFile(rootName);
        }
        return root;
        */
    }

    /**
     * Finds a file in this file system.
     * @param nameStr The name of the file to resolve.
     * @return The located FileObject or null if none could be located.
     * @throws FileSystemException if an error occurs.
     */
    public FileObject resolveFile(final String nameStr) throws FileSystemException
    {
        // Resolve the name, and create the file
        final FileName name = getFileSystemManager().resolveName(rootName, nameStr);
        return resolveFile(name);
    }

    /**
     * Finds a file in this file system.
     * @param name The name of the file to locate.
     * @return The located FileObject or null if none could be located.
     * @throws FileSystemException if an error occurs.
     */
    public synchronized FileObject resolveFile(final FileName name) throws FileSystemException
    {
        return resolveFile(name, true);
    }

    private synchronized FileObject resolveFile(final FileName name, final boolean useCache) throws FileSystemException
    {
        if (!rootName.getRootURI().equals(name.getRootURI()))
        {
            throw new FileSystemException("vfs.provider/mismatched-fs-for-name.error",
                new Object[]{
                    name, rootName, name.getRootURI()});
        }

        // imario@apache.org ==> use getFileFromCache
        FileObject file;
        if (useCache)
        {
            file = getFileFromCache(name);
        }
        else
        {
            file = null;
        }
        // FileObject file = (FileObject) files.get(name);
        if (file == null)
        {
            try
            {
                synchronized (this)
                {
                    file = createFile(name);
                }
            }
            catch (Exception e)
            {
                throw new FileSystemException("vfs.provider/resolve-file.error", name, e);
            }

            file = decorateFileObject(file);

            // imario@apache.org ==> use putFileToCache
            if (useCache)
            {
                putFileToCache(file);
            }
            // files.put(name, file);
        }

        /**
         * resync the file information if requested
         */
        if (getFileSystemManager().getCacheStrategy().equals(CacheStrategy.ON_RESOLVE))
        {
            file.refresh();
        }
        return file;
    }

    protected FileObject decorateFileObject(FileObject file)  throws FileSystemException
    {
        if (getFileSystemManager().getCacheStrategy().equals(CacheStrategy.ON_CALL))
        {
            file = new OnCallRefreshFileObject(file);
        }

        if (getFileSystemManager().getFileObjectDecoratorConst() != null)
        {
            try
            {
                file = (FileObject) getFileSystemManager().getFileObjectDecoratorConst().
                        newInstance(new Object[]{file});
            }
            catch (InstantiationException e)
            {
                throw new FileSystemException("vfs.impl/invalid-decorator.error",
                        getFileSystemManager().getFileObjectDecorator().getName(), e);
            }
            catch (IllegalAccessException e)
            {
                throw new FileSystemException("vfs.impl/invalid-decorator.error",
                        getFileSystemManager().getFileObjectDecorator().getName(), e);
            }
            catch (InvocationTargetException e)
            {
                throw new FileSystemException("vfs.impl/invalid-decorator.error",
                        getFileSystemManager().getFileObjectDecorator().getName(), e);
            }
        }

        return file;
    }

    /**
     * Creates a temporary local copy of a file and its descendents.
     * @param file The FileObject to replicate.
     * @param selector The FileSelector.
     * @return The replicated File.
     * @throws FileSystemException if an error occurs.
     */
    public File replicateFile(final FileObject file,
                              final FileSelector selector)
        throws FileSystemException
    {
        if (!file.exists())
        {
            throw new FileSystemException("vfs.provider/replicate-missing-file.error", file.getName());
        }

        try
        {
            return doReplicateFile(file, selector);
        }
        catch (final Exception e)
        {
            throw new FileSystemException("vfs.provider/replicate-file.error", file.getName(), e);
        }
    }

    /**
     * Return the FileSystemOptions used to instantiate this filesystem.
     * @return the FileSystemOptions.
     */
    public FileSystemOptions getFileSystemOptions()
    {
        return fileSystemOptions;
    }

    protected <T extends FileSystemOptions> T getOptions()
    {
        return (T) fileSystemOptions;
    }

    /**
     * Return the FileSystemManager used to instantiate this filesystem.
     * @return the FileSystemManager.
     */
    public FileSystemManager getFileSystemManager()
    {
        return getContext().getFileSystemManager();
        // return manager;
    }

    /**
     * Returns the accuracy of the last modification time.
     *
     * @return ms 0 perfectly accurate, >0 might be off by this value e.g. sftp 1000ms
     */
    public double getLastModTimeAccuracy()
    {
        return 0;
    }

    /**
     * Creates a temporary local copy of a file and its descendents.
     */
    protected File doReplicateFile(final FileObject file,
                                   final FileSelector selector)
        throws Exception
    {
        return getContext().getReplicator().replicateFile(file, selector);
    }

    /**
     * Adds a junction to this file system.
     * @param junctionPoint The junction point.
     * @param targetFile The target to add.
     * @throws FileSystemException if an error occurs.
     */
    public void addJunction(final String junctionPoint,
                            final FileObject targetFile)
        throws FileSystemException
    {
        throw new FileSystemException("vfs.provider/junctions-not-supported.error", rootName);
    }

    /**
     * Removes a junction from this file system.
     * @param junctionPoint The junction point.
     * @throws FileSystemException if an error occurs
     */
    public void removeJunction(final String junctionPoint) throws FileSystemException
    {
        throw new FileSystemException("vfs.provider/junctions-not-supported.error", rootName);
    }

    /**
     * Adds a listener on a file in this file system.
     * @param file The FileObject to be monitored.
     * @param listener The FileListener
     */
    public void addListener(final FileObject file,
                            final FileListener listener)
    {
        synchronized (listenerMap)
        {
            ArrayList listeners = (ArrayList) listenerMap.get(file.getName());
            if (listeners == null)
            {
                listeners = new ArrayList();
                listenerMap.put(file.getName(), listeners);
            }
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener from a file in this file system.
     * @param file The FileObject to be monitored.
     * @param listener The FileListener
     */
    public void removeListener(final FileObject file,
                               final FileListener listener)
    {
        synchronized (listenerMap)
        {
            final ArrayList listeners = (ArrayList) listenerMap.get(file.getName());
            if (listeners != null)
            {
                listeners.remove(listener);
                if (listeners.isEmpty())
                {
                    listenerMap.remove(file.getName());
                }
            }
        }
    }

    /**
     * Fires a file create event.
     * @param file The FileObject that was created.
     */
    public void fireFileCreated(final FileObject file)
    {
        fireEvent(new CreateEvent(file));
    }

    /**
     * Fires a file delete event.
     * @param file The FileObject that was deleted.
     */
    public void fireFileDeleted(final FileObject file)
    {
        fireEvent(new DeleteEvent(file));
    }

    /**
     * Fires a file changed event. <br />
     * This will only happen if you monitor the file using {@link org.apache.commons.vfs.FileMonitor}.
     * @param file The FileObject that changed.
     */
    public void fireFileChanged(final FileObject file)
    {
        fireEvent(new ChangedEvent(file));
    }

    /**
     * returns true if no file is using this filesystem.
     * @return true of no file is using this FileSystem.
     */
    public boolean isReleaseable()
    {
        return useCount < 1;
    }

    void freeResources()
    {
    }

    /**
     * Fires an event.
     */
    private void fireEvent(final AbstractFileChangeEvent event)
    {
        FileListener[] fileListeners = null;
        final FileObject file = event.getFile();

        synchronized (listenerMap)
        {
            final ArrayList listeners = (ArrayList) listenerMap.get(file.getName());
            if (listeners != null)
            {
                fileListeners = (FileListener[]) listeners.toArray(new FileListener[listeners.size()]);
            }
        }

        if (fileListeners != null)
        {
            for (int i = 0; i < fileListeners.length; i++)
            {
                final FileListener fileListener = fileListeners[i];
                try
                {
                    event.notify(fileListener);
                }
                catch (final Exception e)
                {
                    final String message = Messages.getString("vfs.provider/notify-listener.warn", file);
                    // getLogger().warn(message, e);
                    VfsLog.warn(getLogger(), LOG, message, e);
                }
            }
        }
    }

    /*
    void fileDetached(FileObject fileObject)
    {
        useCount--;
    }

    void fileAttached(FileObject fileObject)
    {
        useCount++;

    }
    */

    void fileObjectHanded(FileObject fileObject)
    {
        useCount++;
    }

    void fileObjectDestroyed(FileObject fileObject)
    {
        useCount--;
    }

    void setCacheKey(FileSystemKey cacheKey)
    {
        this.cacheKey = cacheKey;
    }

    FileSystemKey getCacheKey()
    {
        return this.cacheKey;
    }

    void streamOpened()
    {
        synchronized (this)
        {
            openStreams++;
        }
    }

    void streamClosed()
    {
        synchronized (this)
        {
            if (openStreams > 0)
            {
                openStreams--;
                if (openStreams < 1)
                {
                    notifyAllStreamsClosed();
                }
            }
        }
    }

    /**
     * will be called after all file-objects closed their streams.
     */
    protected void notifyAllStreamsClosed()
    {
    }

    /**
     * check if this filesystem has open streams.
     * @return true if the FileSystem has open streams.
     */
    public boolean isOpen()
    {
        synchronized (this)
        {
            return openStreams > 0;
        }
    }

    private class PrivateFileSystemOptions extends DefaultFileSystemOptions
    {

    }
}
