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

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.VFS;

/**
 * A default file name implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public abstract class AbstractFileName
    implements FileName
{
    
    private final String scheme;
    private final String absPath;
    private FileType type;

    // Cached stuff
    private String uri;
    private String baseName;
    private String rootUri;
    private String extension;
    private String decodedAbsPath;

    public AbstractFileName(final String scheme,
                            final String absPath, FileType type)
    {
        this.rootUri = null;
        this.scheme = scheme;
        this.type = type;
        if (absPath != null && absPath.length() > 0)
        {
            if (absPath.length() > 1 && absPath.endsWith("/"))
            {
                this.absPath = absPath.substring(0, absPath.length() - 1);
            }
            else
            {
                this.absPath = absPath;
            }
        }
        else
        {
            this.absPath = ROOT_PATH;
        }
    }

    /**
     * Returns the hashcode for this name.
     */
    public int hashCode()
    {
        return (getRootURI().hashCode() ^ getPath().hashCode());
    }

    /**
     * Determines if this object is equal to another.
     */
    public boolean equals(final Object obj)
    {
        final AbstractFileName name = (AbstractFileName) obj;
        return (getRootURI().equals(name.getRootURI()) && getPath().equals(name.getPath()));
    }

    /**
     * Implement Comparable
     *
     * @param obj another abstractfilename
     */
    public int compareTo(Object obj)
    {
        final AbstractFileName name = (AbstractFileName) obj;
        int ret = getRootURI().compareTo(name.getRootURI());
        if (ret != 0)
        {
            return ret;
        }

        // return absPath.compareTo(name.absPath);
        try
        {
            return getPathDecoded().compareTo(name.getPathDecoded());
        }
        catch (FileSystemException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Returns the URI of the file.
     */
    public String toString()
    {
        return getURI();
    }

    /**
     * Factory method for creating name instances.
     */
    public abstract FileName createName(String absPath, FileType type);

    /**
     * Builds the root URI for this file name.  Note that the root URI must not
     * end with a separator character.
     */
    protected abstract void appendRootUri(StringBuffer buffer, boolean addPassword);

    /**
     * Returns the base name of the file.
     */
    public String getBaseName()
    {
        if (baseName == null)
        {
            final int idx = getPath().lastIndexOf(SEPARATOR_CHAR);
            if (idx == -1)
            {
                baseName = getPath();
            }
            else
            {
                baseName = getPath().substring(idx + 1);
            }
        }

        return baseName;
    }

    /**
     * Returns the absolute path of the file, relative to the root of the
     * file system that the file belongs to.
     */
    public String getPath()
    {
        if (VFS.isUriStyle())
        {
            return absPath + getUriTrailer();
        }
        return absPath;
    }

    protected String getUriTrailer()
    {
        return getType().hasChildren() ? "/" : "";
    }

    public String getPathDecoded() throws FileSystemException
    {
        if (decodedAbsPath == null)
        {
            decodedAbsPath = UriParser.decode(getPath());
        }

        return decodedAbsPath;
    }

    /**
     * Returns the name of the parent of the file.
     */
    public FileName getParent()
    {
        final String parentPath;
        final int idx = getPath().lastIndexOf(SEPARATOR_CHAR);
        if (idx == -1 || idx == getPath().length() - 1)
        {
            // No parent
            return null;
        }
        else if (idx == 0)
        {
            // Root is the parent
            parentPath = SEPARATOR;
        }
        else
        {
            parentPath = getPath().substring(0, idx);
        }
        return createName(parentPath, FileType.FOLDER);
    }

    /**
     * find the root of the filesystem
     */
    public FileName getRoot()
    {
        FileName root = this;
        while (root.getParent() != null)
        {
            root = root.getParent();
        }

        return root;
    }

    /**
     * Returns the URI scheme of this file.
     */
    public String getScheme()
    {
        return scheme;
    }

    /**
     * Returns the absolute URI of the file.
     */
    public String getURI()
    {
        if (uri == null)
        {
            uri = createURI();
        }
        return uri;
    }

    protected String createURI()
    {
        final StringBuffer buffer = new StringBuffer();
        appendRootUri(buffer, true);
        buffer.append(getPath());
        return buffer.toString();
    }

    /**
     * Converts a file name to a relative name, relative to this file name.
     */
    public String getRelativeName(final FileName name) throws FileSystemException
    {
        final String path = name.getPath();

        // Calculate the common prefix
        final int basePathLen = getPath().length();
        final int pathLen = path.length();

        // Deal with root
        if (basePathLen == 1 && pathLen == 1)
        {
            return ".";
        }
        else if (basePathLen == 1)
        {
            return path.substring(1);
        }

        final int maxlen = Math.min(basePathLen, pathLen);
        int pos = 0;
        for (; pos < maxlen && getPath().charAt(pos) == path.charAt(pos); pos++)
        {
        }

        if (pos == basePathLen && pos == pathLen)
        {
            // Same names
            return ".";
        }
        else if (pos == basePathLen && pos < pathLen && path.charAt(pos) == SEPARATOR_CHAR)
        {
            // A descendent of the base path
            return path.substring(pos + 1);
        }

        // Strip the common prefix off the path
        final StringBuffer buffer = new StringBuffer();
        if (pathLen > 1 && (pos < pathLen || getPath().charAt(pos) != SEPARATOR_CHAR))
        {
            // Not a direct ancestor, need to back up
            pos = getPath().lastIndexOf(SEPARATOR_CHAR, pos);
            buffer.append(path.substring(pos));
        }

        // Prepend a '../' for each element in the base path past the common
        // prefix
        buffer.insert(0, "..");
        pos = getPath().indexOf(SEPARATOR_CHAR, pos + 1);
        while (pos != -1)
        {
            buffer.insert(0, "../");
            pos = getPath().indexOf(SEPARATOR_CHAR, pos + 1);
        }

        return buffer.toString();
    }

    /**
     * Returns the root URI of the file system this file belongs to.
     */
    public String getRootURI()
    {
        if (rootUri == null)
        {
            final StringBuffer buffer = new StringBuffer();
            appendRootUri(buffer, true);
            buffer.append(SEPARATOR_CHAR);
            rootUri = buffer.toString().intern();
        }
        return rootUri;
    }

    /**
     * Returns the depth of this file name, within its file system.
     */
    public int getDepth()
    {
        final int len = getPath().length();
        if (len == 0 || (len == 1 && getPath().charAt(0) == SEPARATOR_CHAR))
        {
            return 0;
        }
        int depth = 1;
        for (int pos = 0; pos > -1 && pos < len; depth++)
        {
            pos = getPath().indexOf(SEPARATOR_CHAR, pos + 1);
        }
        return depth;
    }

    /**
     * Returns the extension of this file name.
     */
    public String getExtension()
    {
        if (extension == null)
        {
            getBaseName();
            final int pos = baseName.lastIndexOf('.');
            // if ((pos == -1) || (pos == baseName.length() - 1))
            // imario@ops.co.at: Review of patch from adagoubard@chello.nl
            // do not treat filenames like
            // .bashrc c:\windows\.java c:\windows\.javaws c:\windows\.jedit c:\windows\.appletviewer
            // as extension
            if ((pos < 1) || (pos == baseName.length() - 1))
            {
                // No extension
                extension = "";
            }
            else
            {
                extension = baseName.substring(pos + 1).intern();
            }
        }
        return extension;
    }

    /**
     * Determines if another file name is an ancestor of this file name.
     */
    public boolean isAncestor(final FileName ancestor)
    {
        if (!ancestor.getRootURI().equals(getRootURI()))
        {
            return false;
        }
        return checkName(ancestor.getPath(), getPath(), NameScope.DESCENDENT);
    }

    /**
     * Determines if another file name is a descendent of this file name.
     */
    public boolean isDescendent(final FileName descendent)
    {
        return isDescendent(descendent, NameScope.DESCENDENT);
    }

    /**
     * Determines if another file name is a descendent of this file name.
     */
    public boolean isDescendent(final FileName descendent,
                                final NameScope scope)
    {
        if (!descendent.getRootURI().equals(getRootURI()))
        {
            return false;
        }
        return checkName(getPath(), descendent.getPath(), scope);
    }

    /**
     * Returns the requested or current type of this name. <br />
     * <p/>
     * The "requested" type is the one determined during resolving the name. <br/>
     * In this case the name is a {@link FileType#FOLDER} if it ends with an "/" else
     * it will be a {@link FileType#FILE}<br/>
     * </p>
     * <p/>
     * Once attached it will be changed to reflect the real type of this resource.
     * </p>
     *
     * @return {@link FileType#FOLDER} or {@link FileType#FILE}
     */
    public FileType getType()
    {
        return type;
    }

    /**
     * sets the type of this file e.g. when it will be attached.
     *
     * @param type {@link FileType#FOLDER} or {@link FileType#FILE}
     */
    void setType(FileType type) throws FileSystemException
    {
        if (type != FileType.FOLDER && type != FileType.FILE && type != FileType.FILE_OR_FOLDER)
        {
            throw new FileSystemException("vfs.provider/filename-type.error");
        }

        this.type = type;
    }

    /**
     * Checks whether a path fits in a particular scope of another path.
     *
     * @param basePath An absolute, normalised path.
     * @param path     An absolute, normalised path.
     */
    public static boolean checkName(final String basePath,
                                    final String path,
                                    final NameScope scope)
    {
        if (scope == NameScope.FILE_SYSTEM)
        {
            // All good
            return true;
        }

        if (!path.startsWith(basePath))
        {
            return false;
        }

        int baseLen = basePath.length();
        if (VFS.isUriStyle())
        {
            // strip the trailing "/"
            baseLen--;
        }

        if (scope == NameScope.CHILD)
        {
            if (path.length() == baseLen
                || (baseLen > 1 && path.charAt(baseLen) != SEPARATOR_CHAR)
                || path.indexOf(SEPARATOR_CHAR, baseLen + 1) != -1)
            {
                return false;
            }
        }
        else if (scope == NameScope.DESCENDENT)
        {
            if (path.length() == baseLen
                || (baseLen > 1 && path.charAt(baseLen) != SEPARATOR_CHAR))
            {
                return false;
            }
        }
        else if (scope == NameScope.DESCENDENT_OR_SELF)
        {
            if (baseLen > 1
                && path.length() > baseLen
                && path.charAt(baseLen) != SEPARATOR_CHAR)
            {
                return false;
            }
        }
        else if (scope != NameScope.FILE_SYSTEM)
        {
            throw new IllegalArgumentException();
        }

        return true;
    }

    /**
     * returns a "friendly path", this is a path without a password.
     */
    public String getFriendlyURI()
    {
        final StringBuffer buffer = new StringBuffer();
        appendRootUri(buffer, false);
        buffer.append(getPath());
        return buffer.toString();
    }
}
