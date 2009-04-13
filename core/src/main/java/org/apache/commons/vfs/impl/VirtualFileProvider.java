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
package org.apache.commons.vfs.impl;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractVfsContainer;


/**
 * A virtual filesystem provider.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class VirtualFileProvider
    extends AbstractVfsContainer
{
    /**
     * Creates a virtual file system, with the supplied file as its root.
     * @param rootFile The root of the file system.
     * @return A FileObject in the FileSystem.
     * @throws FileSystemException if an error occurs.
     */
    public FileObject createFileSystem(final FileObject rootFile)
        throws FileSystemException
    {
        final FileName rootName =
            getContext().getFileSystemManager().resolveName(rootFile.getName(), FileName.ROOT_PATH);
        // final FileName rootName =
        //    new BasicFileName(rootFile.getName(), FileName.ROOT_PATH);
        final VirtualFileSystem fs = new VirtualFileSystem(rootName, rootFile.getFileSystem().getFileSystemOptions());
        addComponent(fs);
        fs.addJunction(FileName.ROOT_PATH, rootFile);
        return fs.getRoot();
    }

    /**
     * Creates an empty virtual file system.
     * @param rootUri The root of the file system.
     * @return A FileObject in the FileSystem.
     * @throws FileSystemException if an error occurs.
     */
    public FileObject createFileSystem(final String rootUri) throws FileSystemException
    {
        final FileName rootName =
            new VirtualFileName(rootUri, FileName.ROOT_PATH, FileType.FOLDER);
        // final FileName rootName =
        //    new BasicFileName(rootUri, FileName.ROOT_PATH);
        final VirtualFileSystem fs = new VirtualFileSystem(rootName, null);
        addComponent(fs);
        return fs.getRoot();
    }
}
