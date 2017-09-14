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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.DelegateFileObject;

/**
 * A logical file system, made up of set of junctions, or links, to files from other file systems.
 * <p>
 * TODO - Handle nested junctions.
 */
public class VirtualFileSystem extends AbstractFileSystem {
    private final Map<FileName, FileObject> junctions = new HashMap<>();

    public VirtualFileSystem(final AbstractFileName rootName, final FileSystemOptions fileSystemOptions) {
        super(rootName, null, fileSystemOptions);
    }

    /**
     * Adds the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps) {
        // TODO - this isn't really true
        caps.add(Capability.ATTRIBUTES);
        caps.add(Capability.CREATE);
        caps.add(Capability.DELETE);
        caps.add(Capability.GET_TYPE);
        caps.add(Capability.JUNCTIONS);
        caps.add(Capability.GET_LAST_MODIFIED);
        caps.add(Capability.SET_LAST_MODIFIED_FILE);
        caps.add(Capability.SET_LAST_MODIFIED_FOLDER);
        caps.add(Capability.LIST_CHILDREN);
        caps.add(Capability.READ_CONTENT);
        caps.add(Capability.SIGNING);
        caps.add(Capability.WRITE_CONTENT);
        caps.add(Capability.APPEND_CONTENT);
    }

    /**
     * Creates a file object. This method is called only if the requested file is not cached.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws Exception {
        // Find the file that the name points to
        final FileName junctionPoint = getJunctionForFile(name);
        final FileObject file;
        if (junctionPoint != null) {
            // Resolve the real file
            final FileObject junctionFile = junctions.get(junctionPoint);
            final String relName = junctionPoint.getRelativeName(name);
            file = junctionFile.resolveFile(relName, NameScope.DESCENDENT_OR_SELF);
        } else {
            file = null;
        }

        // Return a wrapper around the file
        return new DelegateFileObject(name, this, file);
    }

    /**
     * Adds a junction to this file system.
     *
     * @param junctionPoint The location of the junction.
     * @param targetFile The target file to base the junction on.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public void addJunction(final String junctionPoint, final FileObject targetFile) throws FileSystemException {
        final FileName junctionName = getFileSystemManager().resolveName(getRootName(), junctionPoint);

        // Check for nested junction - these are not supported yet
        if (getJunctionForFile(junctionName) != null) {
            throw new FileSystemException("vfs.impl/nested-junction.error", junctionName);
        }

        try {
            // Add to junction table
            junctions.put(junctionName, targetFile);

            // Attach to file
            final DelegateFileObject junctionFile = (DelegateFileObject) getFileFromCache(junctionName);
            if (junctionFile != null) {
                junctionFile.setFile(targetFile);
            }

            // Create ancestors of junction point
            FileName childName = junctionName;
            boolean done = false;
            for (AbstractFileName parentName = (AbstractFileName) childName.getParent(); !done
                    && parentName != null; childName = parentName, parentName = (AbstractFileName) parentName
                            .getParent()) {
                DelegateFileObject file = (DelegateFileObject) getFileFromCache(parentName);
                if (file == null) {
                    file = new DelegateFileObject(parentName, this, null);
                    putFileToCache(file);
                } else {
                    done = file.exists();
                }

                // As this is the parent of our junction it has to be a folder
                file.attachChild(childName, FileType.FOLDER);
            }

            // TODO - attach all cached children of the junction point to their real file
        } catch (final Exception e) {
            throw new FileSystemException("vfs.impl/create-junction.error", junctionName, e);
        }
    }

    /**
     * Removes a junction from this file system.
     *
     * @param junctionPoint The junction to remove.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public void removeJunction(final String junctionPoint) throws FileSystemException {
        final FileName junctionName = getFileSystemManager().resolveName(getRootName(), junctionPoint);
        junctions.remove(junctionName);

        // TODO - remove from parents of junction point
        // TODO - detach all cached children of the junction point from their real file
    }

    /**
     * Locates the junction point for the junction containing the given file.
     *
     * @param name The FileName.
     * @return the FileName where the junction occurs.
     */
    private FileName getJunctionForFile(final FileName name) {
        if (junctions.containsKey(name)) {
            // The name points to the junction point directly
            return name;
        }

        // Find matching junction
        for (final FileName junctionPoint : junctions.keySet()) {
            if (junctionPoint.isDescendent(name)) {
                return junctionPoint;
            }
        }

        // None
        return null;
    }

    @Override
    public void close() {
        super.close();
        junctions.clear();
    }
}
