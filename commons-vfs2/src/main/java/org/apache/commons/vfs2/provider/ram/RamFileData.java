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
package org.apache.commons.vfs2.provider.ram;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

/**
 * RAM File Object Data.
 */
final class RamFileData implements Serializable {

    /**
     * serialVersionUID format is YYYYMMDD for the date of the last binary change.
     */
    private static final long serialVersionUID = 20101208L;

    /**
     * File Name.
     */
    private FileName name;

    /**
     * File Type.
     */
    private FileType type;

    /**
     * Bytes.
     */
    private byte[] content;

    /**
     * Last modified time
     */
    private long lastModifiedMillis;

    /**
     * Children
     */
    private final Collection<RamFileData> children;

    /**
     * Constructs a new instance.
     *
     * @param name The file name.
     */
    RamFileData(final FileName name) {
        children = Collections.synchronizedCollection(new ArrayList<>());
        clear();
        if (name == null) {
            throw new IllegalArgumentException("name can not be null");
        }
        this.name = name;
    }

    /**
     * Add a child.
     *
     * @param data The file data.
     * @throws FileSystemException if an error occurs.
     */
    void addChild(final RamFileData data) throws FileSystemException {
        if (!getType().hasChildren()) {
            throw new FileSystemException("A child can only be added in a folder");
        }

        FileSystemException.requireNonNull(data, "No child can be null");

        if (children.contains(data)) {
            throw new FileSystemException("Child already exists. " + data);
        }

        children.add(data);
        updateLastModified();
    }

    /**
     */
    void clear() {
        content = ArrayUtils.EMPTY_BYTE_ARRAY;
        updateLastModified();
        type = FileType.IMAGINARY;
        children.clear();
        name = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RamFileData)) {
            return false;
        }
        final RamFileData data = (RamFileData) o;
        return getName().equals(data.getName());
    }

    /**
     * @return Returns the children.
     */
    Collection<RamFileData> getChildren() {
        if (name == null) {
            throw new IllegalStateException("Data is clear");
        }
        return children;
    }

    /**
     * @return Returns the buffer.
     */
    byte[] getContent() {
        return content;
    }

    /**
     * @return Returns the lastModified.
     */
    long getLastModified() {
        return lastModifiedMillis;
    }

    /**
     * @return Returns the name.
     */
    FileName getName() {
        return name;
    }

    /**
     * @return Returns the type.
     */
    FileType getType() {
        return type;
    }

    boolean hasChildren(final RamFileData data) {
        return children.contains(data);
    }

    /*
     * (non-Javadoc)
     *
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Remove a child.
     *
     * @param data The file data.
     * @throws FileSystemException if an error occurs.
     */
    void removeChild(final RamFileData data) throws FileSystemException {
        if (!getType().hasChildren()) {
            throw new FileSystemException("A child can only be removed from a folder");
        }
        if (!children.contains(data)) {
            throw new FileSystemException("Child not found. " + data);
        }
        children.remove(data);
        updateLastModified();
    }

    /**
     * Resize the buffer
     *
     * @param newSize The new buffer size.
     */
    void resize(final long newSize) {
        // A future implementation may allow longs/multiple buffer/and so on
        if (newSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    String.format("newSize(%d) > Integer.MAX_VALUE(%d)", newSize, Integer.MAX_VALUE));
        }
        final int resize = (int) newSize;
        final int size = size();
        final byte[] newBuf = new byte[resize];
        System.arraycopy(content, 0, newBuf, 0, Math.min(resize, size));
        content = newBuf;
        updateLastModified();
    }

    /**
     * @param content The buffer.
     */
    void setContent(final byte[] content) {
        updateLastModified();
        this.content = content;
    }

    /**
     * @param lastModifiedMillis The lastModified to set.
     */
    void setLastModified(final long lastModifiedMillis) {
        this.lastModifiedMillis = lastModifiedMillis;
    }

    /**
     * @param type The type to set.
     */
    void setType(final FileType type) {
        this.type = type;
    }

    /**
     * @return Returns the size of the buffer
     */
    int size() {
        return content.length;
    }

    /*
     * (non-Javadoc)
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return name.toString();
    }

    void updateLastModified() {
        lastModifiedMillis = System.currentTimeMillis();
    }

}
