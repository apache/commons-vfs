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
public class DecoratedFileObject implements FileObject {
    private final FileObject decoratedFileObject;

    public DecoratedFileObject(final FileObject decoratedFileObject) {
        super();
        this.decoratedFileObject = decoratedFileObject;
    }

    @Override
    public boolean canRenameTo(final FileObject newfile) {
        return decoratedFileObject.canRenameTo(newfile);
    }

    @Override
    public void close() throws FileSystemException {
        decoratedFileObject.close();
    }

    @Override
    public int compareTo(final FileObject fo) {
        return decoratedFileObject.compareTo(fo);
    }

    @Override
    public void copyFrom(final FileObject srcFile, final FileSelector selector) throws FileSystemException {
        decoratedFileObject.copyFrom(srcFile, selector);
    }

    @Override
    public void createFile() throws FileSystemException {
        decoratedFileObject.createFile();
    }

    @Override
    public void createFolder() throws FileSystemException {
        decoratedFileObject.createFolder();
    }

    @Override
    public boolean delete() throws FileSystemException {
        return decoratedFileObject.delete();
    }

    @Override
    public int delete(final FileSelector selector) throws FileSystemException {
        return decoratedFileObject.delete(selector);
    }

    @Override
    public int deleteAll() throws FileSystemException {
        return decoratedFileObject.deleteAll();
    }

    @Override
    public boolean exists() throws FileSystemException {
        return decoratedFileObject.exists();
    }

    @Override
    public FileObject[] findFiles(final FileSelector selector) throws FileSystemException {
        return decoratedFileObject.findFiles(selector);
    }

    @Override
    public void findFiles(final FileSelector selector, final boolean depthwise, final List<FileObject> selected)
            throws FileSystemException {
        decoratedFileObject.findFiles(selector, depthwise, selected);
    }

    @Override
    public FileObject getChild(final String name) throws FileSystemException {
        return decoratedFileObject.getChild(name);
    }

    @Override
    public FileObject[] getChildren() throws FileSystemException {
        return decoratedFileObject.getChildren();
    }

    @Override
    public FileContent getContent() throws FileSystemException {
        return decoratedFileObject.getContent();
    }

    public FileObject getDecoratedFileObject() {
        return decoratedFileObject;
    }

    @Override
    public FileOperations getFileOperations() throws FileSystemException {
        return decoratedFileObject.getFileOperations();
    }

    @Override
    public FileSystem getFileSystem() {
        return decoratedFileObject.getFileSystem();
    }

    @Override
    public String getPublicURIString() {
        return decoratedFileObject.getPublicURIString();
    }

    @Override
    public FileName getName() {
        return decoratedFileObject.getName();
    }

    @Override
    public FileObject getParent() throws FileSystemException {
        return decoratedFileObject.getParent();
    }

    @Override
    public FileType getType() throws FileSystemException {
        return decoratedFileObject.getType();
    }

    @Override
    public URL getURL() throws FileSystemException {
        return decoratedFileObject.getURL();
    }

    @Override
    public boolean isAttached() {
        return decoratedFileObject.isAttached();
    }

    @Override
    public boolean isContentOpen() {
        return decoratedFileObject.isContentOpen();
    }

    @Override
    public boolean isExecutable() throws FileSystemException {
        return decoratedFileObject.isExecutable();
    }

    @Override
    public boolean isFile() throws FileSystemException {
        return decoratedFileObject.isFile();
    }

    @Override
    public boolean isFolder() throws FileSystemException {
        return decoratedFileObject.isFolder();
    }

    @Override
    public boolean isHidden() throws FileSystemException {
        return decoratedFileObject.isHidden();
    }

    @Override
    public boolean isReadable() throws FileSystemException {
        return decoratedFileObject.isReadable();
    }

    @Override
    public boolean isWriteable() throws FileSystemException {
        return decoratedFileObject.isWriteable();
    }

    @Override
    public Iterator<FileObject> iterator() {
        return decoratedFileObject.iterator();
    }

    @Override
    public void moveTo(final FileObject destFile) throws FileSystemException {
        decoratedFileObject.moveTo(destFile);
    }

    @Override
    public void refresh() throws FileSystemException {
        decoratedFileObject.refresh();
    }

    @Override
    public FileObject resolveFile(final String path) throws FileSystemException {
        return decoratedFileObject.resolveFile(path);
    }

    @Override
    public FileObject resolveFile(final String name, final NameScope scope) throws FileSystemException {
        return decoratedFileObject.resolveFile(name, scope);
    }

    @Override
    public boolean setExecutable(final boolean executable, final boolean ownerOnly) throws FileSystemException {
        return decoratedFileObject.setExecutable(executable, ownerOnly);
    }

    @Override
    public boolean setReadable(final boolean readable, final boolean ownerOnly) throws FileSystemException {
        return decoratedFileObject.setReadable(readable, ownerOnly);
    }

    @Override
    public boolean setWritable(final boolean writable, final boolean ownerOnly) throws FileSystemException {
        return decoratedFileObject.setWritable(writable, ownerOnly);
    }

    @Override
    public String toString() {
        return decoratedFileObject.toString();
    }

}
