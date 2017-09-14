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
package org.apache.commons.vfs2.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.util.Messages;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * An abstract file synchronization task. Scans a set of source files and folders, and a destination folder, and
 * performs actions on missing and out-of-date files. Specifically, performs actions on the following:
 * <ul>
 * <li>Missing destination file.
 * <li>Missing source file.
 * <li>Out-of-date destination file.
 * <li>Up-to-date destination file.
 * </ul>
 *
 * TODO - Deal with case where dest file maps to a child of one of the source files.<br>
 * TODO - Deal with case where dest file already exists and is incorrect type (not file, not a folder).<br>
 * TODO - Use visitors.<br>
 * TODO - Add default excludes.<br>
 * TOOD - Allow selector, mapper, filters, etc to be specified.<br>
 * TODO - Handle source/dest directories as well.<br>
 * TODO - Allow selector to be specified for choosing which dest files to sync.
 */
public abstract class AbstractSyncTask extends VfsTask {
    private final ArrayList<SourceInfo> srcFiles = new ArrayList<>();
    private String destFileUrl;
    private String destDirUrl;
    private String srcDirUrl;
    private boolean srcDirIsBase;
    private boolean failonerror = true;
    private String filesList;

    /**
     * Sets the destination file.
     *
     * @param destFile The destination file name.
     */
    public void setDestFile(final String destFile) {
        this.destFileUrl = destFile;
    }

    /**
     * Sets the destination directory.
     *
     * @param destDir The destination directory.
     */
    public void setDestDir(final String destDir) {
        this.destDirUrl = destDir;
    }

    /**
     * Sets the source file.
     *
     * @param srcFile The source file name.
     */
    public void setSrc(final String srcFile) {
        final SourceInfo src = new SourceInfo();
        src.setFile(srcFile);
        addConfiguredSrc(src);
    }

    /**
     * Sets the source directory.
     *
     * @param srcDir The source directory.
     */
    public void setSrcDir(final String srcDir) {
        this.srcDirUrl = srcDir;
    }

    /**
     * Sets whether the source directory should be consider as the base directory.
     *
     * @param srcDirIsBase true if the source directory is the base directory.
     */
    public void setSrcDirIsBase(final boolean srcDirIsBase) {
        this.srcDirIsBase = srcDirIsBase;
    }

    /**
     * Sets whether we should fail if there was an error or not.
     *
     * @param failonerror true if the operation should fail if there is an error.
     */
    public void setFailonerror(final boolean failonerror) {
        this.failonerror = failonerror;
    }

    /**
     * Sets whether we should fail if there was an error or not.
     *
     * @return true if the operation should fail if there was an error.
     */
    public boolean isFailonerror() {
        return failonerror;
    }

    /**
     * Sets the files to includes.
     *
     * @param filesList The list of files to include.
     */
    public void setIncludes(final String filesList) {
        this.filesList = filesList;
    }

    /**
     * Adds a nested &lt;src&gt; element.
     *
     * @param srcInfo A nested source element.
     * @throws BuildException if the SourceInfo doesn't reference a file.
     */
    public void addConfiguredSrc(final SourceInfo srcInfo) throws BuildException {
        if (srcInfo.file == null) {
            final String message = Messages.getString("vfs.tasks/sync.no-source-file.error");
            throw new BuildException(message);
        }
        srcFiles.add(srcInfo);
    }

    /**
     * Executes this task.
     *
     * @throws BuildException if an error occurs.
     */
    @Override
    public void execute() throws BuildException {
        // Validate
        if (destFileUrl == null && destDirUrl == null) {
            final String message = Messages.getString("vfs.tasks/sync.no-destination.error");
            logOrDie(message, Project.MSG_WARN);
            return;
        }

        if (destFileUrl != null && destDirUrl != null) {
            final String message = Messages.getString("vfs.tasks/sync.too-many-destinations.error");
            logOrDie(message, Project.MSG_WARN);
            return;
        }

        // Add the files of the includes attribute to the list
        if (srcDirUrl != null && !srcDirUrl.equals(destDirUrl) && filesList != null && filesList.length() > 0) {
            if (!srcDirUrl.endsWith("/")) {
                srcDirUrl += "/";
            }
            final StringTokenizer tok = new StringTokenizer(filesList, ", \t\n\r\f", false);
            while (tok.hasMoreTokens()) {
                String nextFile = tok.nextToken();

                // Basic compatibility with Ant fileset for directories
                if (nextFile.endsWith("/**")) {
                    nextFile = nextFile.substring(0, nextFile.length() - 2);
                }

                final SourceInfo src = new SourceInfo();
                src.setFile(srcDirUrl + nextFile);
                addConfiguredSrc(src);
            }
        }

        if (srcFiles.size() == 0) {
            final String message = Messages.getString("vfs.tasks/sync.no-source-files.warn");
            logOrDie(message, Project.MSG_WARN);
            return;
        }

        // Perform the sync
        try {
            if (destFileUrl != null) {
                handleSingleFile();
            } else {
                handleFiles();
            }
        } catch (final BuildException e) {
            throw e;
        } catch (final Exception e) {
            throw new BuildException(e.getMessage(), e);
        }
    }

    protected void logOrDie(final String message, final int level) {
        if (!isFailonerror()) {
            log(message, level);
            return;
        }
        throw new BuildException(message);
    }

    /**
     * Copies the source files to the destination.
     */
    private void handleFiles() throws Exception {
        // Locate the destination folder, and make sure it exists
        final FileObject destFolder = resolveFile(destDirUrl);
        destFolder.createFolder();

        // Locate the source files, and make sure they exist
        FileName srcDirName = null;
        if (srcDirUrl != null) {
            srcDirName = resolveFile(srcDirUrl).getName();
        }
        final ArrayList<FileObject> srcs = new ArrayList<>();
        for (int i = 0; i < srcFiles.size(); i++) {
            // Locate the source file, and make sure it exists
            final SourceInfo src = srcFiles.get(i);
            final FileObject srcFile = resolveFile(src.file);
            if (!srcFile.exists()) {
                final String message = Messages.getString("vfs.tasks/sync.src-file-no-exist.warn", srcFile);

                logOrDie(message, Project.MSG_WARN);
            } else {
                srcs.add(srcFile);
            }
        }

        // Scan the source files
        final Set<FileObject> destFiles = new HashSet<>();
        for (int i = 0; i < srcs.size(); i++) {
            final FileObject rootFile = srcs.get(i);
            final FileName rootName = rootFile.getName();

            if (rootFile.isFile()) {
                // Build the destination file name
                String relName = null;
                if (srcDirName == null || !srcDirIsBase) {
                    relName = rootName.getBaseName();
                } else {
                    relName = srcDirName.getRelativeName(rootName);
                }
                final FileObject destFile = destFolder.resolveFile(relName, NameScope.DESCENDENT);

                // Do the copy
                handleFile(destFiles, rootFile, destFile);
            } else {
                // Find matching files
                // If srcDirIsBase is true, select also the sub-directories
                final FileObject[] files = rootFile
                        .findFiles(srcDirIsBase ? Selectors.SELECT_ALL : Selectors.SELECT_FILES);

                for (final FileObject srcFile : files) {
                    // Build the destination file name
                    String relName = null;
                    if (srcDirName == null || !srcDirIsBase) {
                        relName = rootName.getRelativeName(srcFile.getName());
                    } else {
                        relName = srcDirName.getRelativeName(srcFile.getName());
                    }

                    final FileObject destFile = destFolder.resolveFile(relName, NameScope.DESCENDENT);

                    // Do the copy
                    handleFile(destFiles, srcFile, destFile);
                }
            }
        }

        // Scan the destination files for files with no source file
        if (detectMissingSourceFiles()) {
            final FileObject[] allDestFiles = destFolder.findFiles(Selectors.SELECT_FILES);
            for (final FileObject destFile : allDestFiles) {
                if (!destFiles.contains(destFile)) {
                    handleMissingSourceFile(destFile);
                }
            }
        }
    }

    /**
     * Handles a single file, checking for collisions where more than one source file maps to the same destination file.
     */
    private void handleFile(final Set<FileObject> destFiles, final FileObject srcFile, final FileObject destFile)
            throws Exception

    {
        // Check for duplicate source files
        if (destFiles.contains(destFile)) {
            final String message = Messages.getString("vfs.tasks/sync.duplicate-source-files.warn", destFile);
            logOrDie(message, Project.MSG_WARN);
        } else {
            destFiles.add(destFile);
        }

        // Handle the file
        handleFile(srcFile, destFile);
    }

    /**
     * Copies a single file.
     */
    private void handleSingleFile() throws Exception {
        // Make sure there is exactly one source file, and that it exists
        // and is a file.
        if (srcFiles.size() > 1) {
            final String message = Messages.getString("vfs.tasks/sync.too-many-source-files.error");
            logOrDie(message, Project.MSG_WARN);
            return;
        }
        final SourceInfo src = srcFiles.get(0);
        final FileObject srcFile = resolveFile(src.file);
        if (!srcFile.isFile()) {
            final String message = Messages.getString("vfs.tasks/sync.source-not-file.error", srcFile);
            logOrDie(message, Project.MSG_WARN);
            return;
        }

        // Locate the destination file
        final FileObject destFile = resolveFile(destFileUrl);

        // Do the copy
        handleFile(srcFile, destFile);
    }

    /**
     * Handles a single source file.
     */
    private void handleFile(final FileObject srcFile, final FileObject destFile) throws Exception {
        if (!destFile.exists()
                || srcFile.getContent().getLastModifiedTime() > destFile.getContent().getLastModifiedTime()) {
            // Destination file is out-of-date
            handleOutOfDateFile(srcFile, destFile);
        } else {
            // Destination file is up-to-date
            handleUpToDateFile(srcFile, destFile);
        }
    }

    /**
     * Handles an out-of-date file.
     * <p>
     * This is a file where the destination file either doesn't exist, or is older than the source file.
     * <p>
     * This implementation does nothing.
     *
     * @param srcFile The source file.
     * @param destFile The destination file.
     * @throws Exception Implementation can throw any Exception.
     */
    protected void handleOutOfDateFile(final FileObject srcFile, final FileObject destFile) throws Exception {
    }

    /**
     * Handles an up-to-date file.
     * <p>
     * This is where the destination file exists and is newer than the source file.
     * <p>
     * This implementation does nothing.
     *
     * @param srcFile The source file.
     * @param destFile The destination file.
     * @throws Exception Implementation can throw any Exception.
     */
    protected void handleUpToDateFile(final FileObject srcFile, final FileObject destFile) throws Exception {
    }

    /**
     * Handles a destination for which there is no corresponding source file.
     * <p>
     * This implementation does nothing.
     *
     * @param destFile The existing destination file.
     * @throws Exception Implementation can throw any Exception.
     */
    protected void handleMissingSourceFile(final FileObject destFile) throws Exception {
    }

    /**
     * Check if this task cares about destination files with a missing source file.
     * <p>
     * This implementation returns false.
     *
     * @return True if missing file is detected.
     */
    protected boolean detectMissingSourceFiles() {
        return false;
    }

    /**
     * Information about a source file.
     */
    public static class SourceInfo {
        private String file;

        public void setFile(final String file) {
            this.file = file;
        }
    }

}
