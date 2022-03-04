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
package org.apache.commons.vfs2.provider;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.VFS;

/**
 * A default file name implementation.
 */
public abstract class AbstractFileName implements FileName {

    // URI Characters that are possible in local file names, but must be escaped
    // for proper URI handling.
    //
    // How reserved URI chars were selected:
    //
    // URIs can contain :, /, ?, #, @
    // See https://docs.oracle.com/javase/8/docs/api/java/net/URI.html
    // http://tools.ietf.org/html/rfc3986#section-2.2
    //
    // Since : and / occur before the path, only chars after path are escaped (i.e., # and ?)
    // ? is a reserved filesystem character for Windows and Unix, so can't be part of a file name.
    // Therefore only # is a reserved char in a URI as part of the path that can be in the file name.
    private static final char[] RESERVED_URI_CHARS = {'#', ' '};

    private final String scheme;
    private final String absPath;
    private FileType type;

    // Cached attributes
    private String uriString;
    private String baseName;
    private String rootUri;
    private String extension;
    private String decodedAbsPath;

    private String key;

    /**
     * Constructs a new instance.
     *
     * @param scheme The scheme.
     * @param absolutePath the absolute path, maybe empty or null.
     * @param type the file type.
     */
    public AbstractFileName(final String scheme, final String absolutePath, final FileType type) {
        this.rootUri = null;
        this.scheme = scheme;
        this.type = type;
        if (StringUtils.isEmpty(absolutePath)) {
            this.absPath = ROOT_PATH;
        } else if (absolutePath.length() > 1 && absolutePath.endsWith("/")) {
            this.absPath = absolutePath.substring(0, absolutePath.length() - 1);
        } else {
            this.absPath = absolutePath;
        }
    }

    /**
     * Checks whether a path fits in a particular scope of another path.
     *
     * @param basePath An absolute, normalised path.
     * @param path An absolute, normalised path.
     * @param scope The NameScope.
     * @return true if the path fits in the scope, false otherwise.
     */
    public static boolean checkName(final String basePath, final String path, final NameScope scope) {
        if (scope == NameScope.FILE_SYSTEM) {
            // All good
            return true;
        }

        if (!path.startsWith(basePath)) {
            return false;
        }

        int baseLen = basePath.length();
        if (VFS.isUriStyle()) {
            // strip the trailing "/"
            baseLen--;
        }

        if (scope == NameScope.CHILD) {
            return path.length() != baseLen && (baseLen <= 1 || path.charAt(baseLen) == SEPARATOR_CHAR)
                    && path.indexOf(SEPARATOR_CHAR, baseLen + 1) == -1;
        }
        if (scope == NameScope.DESCENDENT) {
            return path.length() != baseLen && (baseLen <= 1 || path.charAt(baseLen) == SEPARATOR_CHAR);
        }
        if (scope == NameScope.DESCENDENT_OR_SELF) {
            return baseLen <= 1 || path.length() <= baseLen || path.charAt(baseLen) == SEPARATOR_CHAR;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Builds the root URI for this file name. Note that the root URI must not end with a separator character.
     *
     * @param buffer A StringBuilder to use to construct the URI.
     * @param addPassword true if the password should be added, false otherwise.
     */
    protected abstract void appendRootUri(StringBuilder buffer, boolean addPassword);

    /**
     * Implement Comparable.
     *
     * @param obj another abstract file name
     * @return negative number if less than, 0 if equal, positive if greater than.
     */
    @Override
    public int compareTo(final FileName obj) {
        final AbstractFileName name = (AbstractFileName) obj;
        return getKey().compareTo(name.getKey());
    }

    /**
     * Factory method for creating name instances.
     *
     * @param absolutePath The absolute path.
     * @param fileType The FileType.
     * @return The FileName.
     */
    public abstract FileName createName(String absolutePath, FileType fileType);

    protected String createURI() {
        return createURI(false, true);
    }

    private String createURI(final boolean useAbsolutePath, final boolean usePassword) {
        final StringBuilder buffer = new StringBuilder();
        appendRootUri(buffer, usePassword);
        buffer.append(handleURISpecialCharacters(useAbsolutePath ? absPath : getPath()));
        return buffer.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AbstractFileName that = (AbstractFileName) o;

        return getKey().equals(that.getKey());
    }

    /**
     * Returns the base name of the file.
     *
     * @return The base name of the file.
     */
    @Override
    public String getBaseName() {
        if (baseName == null) {
            final int idx = getPath().lastIndexOf(SEPARATOR_CHAR);
            if (idx == -1) {
                baseName = getPath();
            } else {
                baseName = getPath().substring(idx + 1);
            }
        }

        return baseName;
    }

    /**
     * Returns the depth of this file name, within its file system.
     *
     * @return The depth of the file name.
     */
    @Override
    public int getDepth() {
        final int len = getPath().length();
        if (len == 0 || len == 1 && getPath().charAt(0) == SEPARATOR_CHAR) {
            return 0;
        }
        int depth = 1;
        for (int pos = 0; pos > -1 && pos < len; depth++) {
            pos = getPath().indexOf(SEPARATOR_CHAR, pos + 1);
        }
        return depth;
    }

    /**
     * Returns the extension of this file name.
     *
     * @return The file extension.
     */
    @Override
    public String getExtension() {
        if (extension == null) {
            getBaseName();
            final int pos = baseName.lastIndexOf('.');
            // if ((pos == -1) || (pos == baseName.length() - 1))
            // imario@ops.co.at: Review of patch from adagoubard@chello.nl
            // do not treat file names like
            // .bashrc c:\windows\.java c:\windows\.javaws c:\windows\.jedit c:\windows\.appletviewer
            // as extension
            if (pos < 1 || pos == baseName.length() - 1) {
                // No extension
                extension = "";
            } else {
                extension = baseName.substring(pos + 1).intern();
            }
        }
        return extension;
    }

    /**
     * Returns the URI without a password.
     *
     * @return Returns the URI without a password.
     */
    @Override
    public String getFriendlyURI() {
        return createURI(false, false);
    }

    /**
     * Create a path that does not use the FileType since that field is not immutable.
     *
     * @return The key.
     */
    private String getKey() {
        if (key == null) {
            key = getURI();
        }
        return key;
    }

    /**
     * Returns the name of the parent of the file.
     *
     * @return the FileName of the parent.
     */
    @Override
    public FileName getParent() {
        final String parentPath;
        final int idx = getPath().lastIndexOf(SEPARATOR_CHAR);
        if (idx == -1 || idx == getPath().length() - 1) {
            // No parent
            return null;
        }
        if (idx == 0) {
            // Root is the parent
            parentPath = SEPARATOR;
        } else {
            parentPath = getPath().substring(0, idx);
        }
        return createName(parentPath, FileType.FOLDER);
    }

    /**
     * Returns the absolute path of the file, relative to the root of the file system that the file belongs to.
     *
     * @return The path String.
     */
    @Override
    public String getPath() {
        if (VFS.isUriStyle()) {
            return absPath + getUriTrailer();
        }
        return absPath;
    }

    /**
     * Returns the decoded path.
     *
     * @return The decoded path String.
     * @throws FileSystemException If an error occurs.
     */
    @Override
    public String getPathDecoded() throws FileSystemException {
        if (decodedAbsPath == null) {
            decodedAbsPath = UriParser.decode(getPath());
        }

        return decodedAbsPath;
    }

    /**
     * Converts a file name to a relative name, relative to this file name.
     *
     * @param name The FileName.
     * @return The relative path to the file.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public String getRelativeName(final FileName name) throws FileSystemException {
        final String path = name.getPath();

        // Calculate the common prefix
        final int basePathLen = getPath().length();
        final int pathLen = path.length();

        // Deal with root
        if (basePathLen == 1 && pathLen == 1) {
            return ".";
        }
        if (basePathLen == 1) {
            return path.substring(1);
        }

        final int maxlen = Math.min(basePathLen, pathLen);
        int pos = 0;
        for (; pos < maxlen && getPath().charAt(pos) == path.charAt(pos); pos++) {
            // empty
        }

        if (pos == basePathLen && pos == pathLen) {
            // Same names
            return ".";
        }
        if (pos == basePathLen && pos < pathLen && path.charAt(pos) == SEPARATOR_CHAR) {
            // A descendent of the base path
            return path.substring(pos + 1);
        }

        // Strip the common prefix off the path
        final StringBuilder buffer = new StringBuilder();
        if (pathLen > 1 && (pos < pathLen || getPath().charAt(pos) != SEPARATOR_CHAR)) {
            // Not a direct ancestor, need to back up
            pos = getPath().lastIndexOf(SEPARATOR_CHAR, pos);
            buffer.append(path.substring(pos));
        }

        // Prepend a '../' for each element in the base path past the common
        // prefix
        buffer.insert(0, "..");
        pos = getPath().indexOf(SEPARATOR_CHAR, pos + 1);
        while (pos != -1) {
            buffer.insert(0, "../");
            pos = getPath().indexOf(SEPARATOR_CHAR, pos + 1);
        }

        return buffer.toString();
    }

    /**
     * find the root of the file system.
     *
     * @return The root FileName.
     */
    @Override
    public FileName getRoot() {
        FileName root = this;
        while (root.getParent() != null) {
            root = root.getParent();
        }

        return root;
    }

    /**
     * Returns the root URI of the file system this file belongs to.
     *
     * @return The URI of the root.
     */
    @Override
    public String getRootURI() {
        if (rootUri == null) {
            final StringBuilder buffer = new StringBuilder();
            appendRootUri(buffer, true);
            buffer.append(SEPARATOR_CHAR);
            rootUri = buffer.toString().intern();
        }
        return rootUri;
    }

    /**
     * Returns the URI scheme of this file.
     *
     * @return The protocol used to access the file.
     */
    @Override
    public String getScheme() {
        return scheme;
    }

    /**
     * Returns the requested or current type of this name.
     * <p>
     * The "requested" type is the one determined during resolving the name. n this case the name is a
     * {@link FileType#FOLDER} if it ends with an "/" else it will be a {@link FileType#FILE}.
     * </p>
     * <p>
     * Once attached it will be changed to reflect the real type of this resource.
     * </p>
     *
     * @return {@link FileType#FOLDER} or {@link FileType#FILE}
     */
    @Override
    public FileType getType() {
        return type;
    }

    /**
     * Returns the absolute URI of the file.
     *
     * @return The absolute URI of the file.
     */
    @Override
    public String getURI() {
        if (uriString == null) {
            uriString = createURI();
        }
        return uriString;
    }

    protected String getUriTrailer() {
        return getType().hasChildren() ? "/" : "";
    }

    private String handleURISpecialCharacters(String uri) {
        if (!StringUtils.isEmpty(uri)) {
            try {
                // VFS-325: Handle URI special characters in file name
                // Decode the base URI and re-encode with URI special characters
                uri = UriParser.decode(uri);

                return UriParser.encode(uri, RESERVED_URI_CHARS);
            } catch (final FileSystemException e) {
                // Default to base URI value?
                return uri;
            }
        }

        return uri;
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    /**
     * Determines if another file name is an ancestor of this file name.
     *
     * @param ancestor The FileName to check.
     * @return true if the FileName is an ancestor, false otherwise.
     */
    @Override
    public boolean isAncestor(final FileName ancestor) {
        if (!ancestor.getRootURI().equals(getRootURI())) {
            return false;
        }
        return checkName(ancestor.getPath(), getPath(), NameScope.DESCENDENT);
    }

    /**
     * Determines if another file name is a descendent of this file name.
     *
     * @param descendent The FileName to check.
     * @return true if the FileName is a descendent, false otherwise.
     */
    @Override
    public boolean isDescendent(final FileName descendent) {
        return isDescendent(descendent, NameScope.DESCENDENT);
    }

    /**
     * Determines if another file name is a descendent of this file name.
     *
     * @param descendent The FileName to check.
     * @param scope The NameScope.
     * @return true if the FileName is a descendent, false otherwise.
     */
    @Override
    public boolean isDescendent(final FileName descendent, final NameScope scope) {
        if (!descendent.getRootURI().equals(getRootURI())) {
            return false;
        }
        return checkName(getPath(), descendent.getPath(), scope);
    }

    /**
     * Checks if this file name is a name for a regular file by using its type.
     *
     * @return true if this file is a regular file.
     * @throws FileSystemException may be thrown by subclasses.
     * @see #getType()
     * @see FileType#FILE
     */
    @Override
    public boolean isFile() throws FileSystemException {
        // Use equals instead of == to avoid any class loader worries.
        return FileType.FILE.equals(this.getType());
    }

    /**
     * Sets the type of this file e.g. when it will be attached.
     *
     * @param type {@link FileType#FOLDER} or {@link FileType#FILE}
     * @throws FileSystemException if an error occurs.
     */
    void setType(final FileType type) throws FileSystemException {
        if (type != FileType.FOLDER && type != FileType.FILE && type != FileType.FILE_OR_FOLDER) {
            throw new FileSystemException("vfs.provider/filename-type.error");
        }

        this.type = type;
    }

    /**
     * Returns the URI of the file.
     *
     * @return the FileName as a URI.
     */
    @Override
    public String toString() {
        return getURI();
    }
}
