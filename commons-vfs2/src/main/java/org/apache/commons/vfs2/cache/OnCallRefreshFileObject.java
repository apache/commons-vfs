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
package org.apache.commons.vfs2.cache;

import java.util.List;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.impl.DecoratedFileObject;

/**
 * This decorator refreshes the fileObject data on every call.
 */
public class OnCallRefreshFileObject extends DecoratedFileObject {
    public OnCallRefreshFileObject(final FileObject fileObject) {
        super(fileObject);
    }

    @Override
    public void close() throws FileSystemException {
        refresh();
        super.close();
    }

    @Override
    public void copyFrom(final FileObject srcFile, final FileSelector selector) throws FileSystemException {
        refresh();
        super.copyFrom(srcFile, selector);
    }

    @Override
    public void createFile() throws FileSystemException {
        refresh();
        super.createFile();
    }

    @Override
    public void createFolder() throws FileSystemException {
        refresh();
        super.createFolder();
    }

    @Override
    public boolean delete() throws FileSystemException {
        refresh();
        return super.delete();
    }

    @Override
    public int delete(final FileSelector selector) throws FileSystemException {
        refresh();
        return super.delete(selector);
    }

    @Override
    public boolean exists() throws FileSystemException {
        refresh();
        return super.exists();
    }

    @Override
    public void findFiles(final FileSelector selector, final boolean depthwise, final List<FileObject> selected)
            throws FileSystemException {
        refresh();
        super.findFiles(selector, depthwise, selected);
    }

    @Override
    public FileObject[] findFiles(final FileSelector selector) throws FileSystemException {
        refresh();
        return super.findFiles(selector);
    }

    @Override
    public FileObject getChild(final String name) throws FileSystemException {
        refresh();
        return super.getChild(name);
    }

    @Override
    public FileObject[] getChildren() throws FileSystemException {
        refresh();
        return super.getChildren();
    }

    @Override
    public FileContent getContent() throws FileSystemException {
        refresh();
        return super.getContent();
    }

    @Override
    public FileType getType() throws FileSystemException {
        refresh();
        return super.getType();
    }

    @Override
    public boolean isExecutable() throws FileSystemException {
        refresh();
        return super.isExecutable();
    }

    @Override
    public boolean isHidden() throws FileSystemException {
        refresh();
        return super.isHidden();
    }

    @Override
    public boolean isReadable() throws FileSystemException {
        refresh();
        return super.isReadable();
    }

    @Override
    public boolean isWriteable() throws FileSystemException {
        refresh();
        return super.isWriteable();
    }

    @Override
    public boolean setExecutable(final boolean executable, final boolean ownerOnly) throws FileSystemException {
        refresh();
        return super.setExecutable(executable, ownerOnly);
    }

    @Override
    public boolean setReadable(final boolean readable, final boolean ownerOnly) throws FileSystemException {
        refresh();
        return super.setReadable(readable, ownerOnly);
    }

    @Override
    public boolean setWritable(final boolean writable, final boolean ownerOnly) throws FileSystemException {
        refresh();
        return super.setWritable(writable, ownerOnly);
    }

    @Override
    public void moveTo(final FileObject destFile) throws FileSystemException {
        refresh();
        super.moveTo(destFile);
    }

    @Override
    public FileObject resolveFile(final String name, final NameScope scope) throws FileSystemException {
        refresh();
        return super.resolveFile(name, scope);
    }

    @Override
    public FileObject resolveFile(final String path) throws FileSystemException {
        refresh();
        return super.resolveFile(path);
    }
}
