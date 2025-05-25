/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
 * Base class to build a FileObject decoration.
 */
public class DecoratedFileObject implements FileObject {

    private final FileObject fileObject;

    /**
     * Constructs a new instance to decorate the given FileObject.
     *
     * @param fileObject the FileObject to decorate.
     */
    public DecoratedFileObject(final FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public boolean canRenameTo(final FileObject newfile) {
        return fileObject.canRenameTo(newfile);
    }

    @Override
    public void close() throws FileSystemException {
        fileObject.close();
    }

    @Override
    public int compareTo(final FileObject fo) {
        return fileObject.compareTo(fo);
    }

    @Override
    public void copyFrom(final FileObject srcFile, final FileSelector selector) throws FileSystemException {
        fileObject.copyFrom(srcFile, selector);
    }

    @Override
    public void createFile() throws FileSystemException {
        fileObject.createFile();
    }

    @Override
    public void createFolder() throws FileSystemException {
        fileObject.createFolder();
    }

    @Override
    public boolean delete() throws FileSystemException {
        return fileObject.delete();
    }

    @Override
    public int delete(final FileSelector selector) throws FileSystemException {
        return fileObject.delete(selector);
    }

    @Override
    public int deleteAll() throws FileSystemException {
        return fileObject.deleteAll();
    }

    @Override
    public boolean exists() throws FileSystemException {
        return fileObject.exists();
    }

    @Override
    public FileObject[] findFiles(final FileSelector selector) throws FileSystemException {
        return fileObject.findFiles(selector);
    }

    @Override
    public void findFiles(final FileSelector selector, final boolean depthwise, final List<FileObject> selected)
            throws FileSystemException {
        fileObject.findFiles(selector, depthwise, selected);
    }

    @Override
    public FileObject getChild(final String name) throws FileSystemException {
        return fileObject.getChild(name);
    }

    @Override
    public FileObject[] getChildren() throws FileSystemException {
        return fileObject.getChildren();
    }

    @Override
    public FileContent getContent() throws FileSystemException {
        return fileObject.getContent();
    }

    /**
     * Gets the decorated fileObject.
     *
     * @return the decorated fileObject.
     */
    public FileObject getDecoratedFileObject() {
        return fileObject;
    }

    @Override
    public FileOperations getFileOperations() throws FileSystemException {
        return fileObject.getFileOperations();
    }

    @Override
    public FileSystem getFileSystem() {
        return fileObject.getFileSystem();
    }

    @Override
    public FileName getName() {
        return fileObject.getName();
    }

    @Override
    public FileObject getParent() throws FileSystemException {
        return fileObject.getParent();
    }

    @Override
    public String getPublicURIString() {
        return fileObject.getPublicURIString();
    }

    @Override
    public FileType getType() throws FileSystemException {
        return fileObject.getType();
    }

    @Override
    public URL getURL() throws FileSystemException {
        return fileObject.getURL();
    }

    @Override
    public boolean isAttached() {
        return fileObject.isAttached();
    }

    @Override
    public boolean isContentOpen() {
        return fileObject.isContentOpen();
    }

    @Override
    public boolean isExecutable() throws FileSystemException {
        return fileObject.isExecutable();
    }

    @Override
    public boolean isFile() throws FileSystemException {
        return fileObject.isFile();
    }

    @Override
    public boolean isFolder() throws FileSystemException {
        return fileObject.isFolder();
    }

    @Override
    public boolean isHidden() throws FileSystemException {
        return fileObject.isHidden();
    }

    @Override
    public boolean isReadable() throws FileSystemException {
        return fileObject.isReadable();
    }

    @Override
    public boolean isWriteable() throws FileSystemException {
        return fileObject.isWriteable();
    }

    @Override
    public Iterator<FileObject> iterator() {
        return fileObject.iterator();
    }

    @Override
    public void moveTo(final FileObject destFile) throws FileSystemException {
        fileObject.moveTo(destFile);
    }

    @Override
    public void refresh() throws FileSystemException {
        fileObject.refresh();
    }

    @Override
    public FileObject resolveFile(final String path) throws FileSystemException {
        return fileObject.resolveFile(path);
    }

    @Override
    public FileObject resolveFile(final String name, final NameScope scope) throws FileSystemException {
        return fileObject.resolveFile(name, scope);
    }

    @Override
    public boolean setExecutable(final boolean executable, final boolean ownerOnly) throws FileSystemException {
        return fileObject.setExecutable(executable, ownerOnly);
    }

    @Override
    public boolean setReadable(final boolean readable, final boolean ownerOnly) throws FileSystemException {
        return fileObject.setReadable(readable, ownerOnly);
    }

    @Override
    public boolean setWritable(final boolean writable, final boolean ownerOnly) throws FileSystemException {
        return fileObject.setWritable(writable, ownerOnly);
    }

    @Override
    public String toString() {
        return fileObject.toString();
    }

}
