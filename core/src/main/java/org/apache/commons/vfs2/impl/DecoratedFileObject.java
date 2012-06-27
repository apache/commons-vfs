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
package org.apache.commons.vfs2.impl;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.operations.FileOperations;

/**
 * Base class to build a fileObject decoration.
 */
public class DecoratedFileObject implements FileObject
{
    private final FileObject decoratedFileObject;

    public DecoratedFileObject(FileObject decoratedFileObject)
    {
        super();
        this.decoratedFileObject = decoratedFileObject;
    }

    public boolean canRenameTo(FileObject newfile)
    {
        return decoratedFileObject.canRenameTo(newfile);
    }

    public void close() throws FileSystemException
    {
        decoratedFileObject.close();
    }

    public int compareTo(FileObject fo)
    {
        return decoratedFileObject.compareTo(fo);
    }

    public void copyFrom(FileObject srcFile, FileSelector selector) throws FileSystemException
    {
        decoratedFileObject.copyFrom(srcFile, selector);
    }

    public void createFile() throws FileSystemException
    {
        decoratedFileObject.createFile();
    }

    public void createFolder() throws FileSystemException
    {
        decoratedFileObject.createFolder();
    }

    public boolean delete() throws FileSystemException
    {
        return decoratedFileObject.delete();
    }

    public int delete(FileSelector selector) throws FileSystemException
    {
        return decoratedFileObject.delete(selector);
    }

    public int deleteAll() throws FileSystemException
    {
        return decoratedFileObject.deleteAll();
    }

    public boolean exists() throws FileSystemException
    {
        return decoratedFileObject.exists();
    }

    public void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected)
        throws FileSystemException
    {
        decoratedFileObject.findFiles(selector, depthwise, selected);
    }

    public FileObject[] findFiles(FileSelector selector) throws FileSystemException
    {
        return decoratedFileObject.findFiles(selector);
    }

    public FileObject getChild(String name) throws FileSystemException
    {
        return decoratedFileObject.getChild(name);
    }

    public FileObject[] getChildren() throws FileSystemException
    {
        return decoratedFileObject.getChildren();
    }

    public FileContent getContent() throws FileSystemException
    {
        return decoratedFileObject.getContent();
    }

    public FileSystem getFileSystem()
    {
        return decoratedFileObject.getFileSystem();
    }

    public FileName getName()
    {
        return decoratedFileObject.getName();
    }

    public FileObject getParent() throws FileSystemException
    {
        return decoratedFileObject.getParent();
    }

    public FileType getType() throws FileSystemException
    {
        return decoratedFileObject.getType();
    }

    public URL getURL() throws FileSystemException
    {
        return decoratedFileObject.getURL();
    }

    public boolean isExecutable() throws FileSystemException
    {
        return decoratedFileObject.isExecutable();
    }

    public boolean isHidden() throws FileSystemException
    {
        return decoratedFileObject.isHidden();
    }

    public boolean isReadable() throws FileSystemException
    {
        return decoratedFileObject.isReadable();
    }

    public boolean isWriteable() throws FileSystemException
    {
        return decoratedFileObject.isWriteable();
    }

    public Iterator<FileObject> iterator()
    {
        return decoratedFileObject.iterator();
    }

    public void moveTo(FileObject destFile) throws FileSystemException
    {
        decoratedFileObject.moveTo(destFile);
    }

    public FileObject resolveFile(String name, NameScope scope) throws FileSystemException
    {
        return decoratedFileObject.resolveFile(name, scope);
    }

    public FileObject resolveFile(String path) throws FileSystemException
    {
        return decoratedFileObject.resolveFile(path);
    }

    public void refresh() throws FileSystemException
    {
        decoratedFileObject.refresh();
    }

    public FileObject getDecoratedFileObject()
    {
        return decoratedFileObject;
    }

    public boolean isAttached()
    {
        return decoratedFileObject.isAttached();
    }

    public boolean isContentOpen()
    {
        return decoratedFileObject.isContentOpen();
    }

    public boolean isFile() throws FileSystemException
    {
        return decoratedFileObject.isFile();
    }

    public boolean isFolder() throws FileSystemException
    {
        return decoratedFileObject.isFolder();
    }

    @Override
    public String toString()
    {
        return decoratedFileObject.toString();
    }

    public FileOperations getFileOperations() throws FileSystemException
    {
        return decoratedFileObject.getFileOperations();
    }


}
