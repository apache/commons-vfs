/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.apache.commons.vfs.NameScope;

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

    // Cached stuff
    private String uri;
    private String baseName;
    private String rootUri;
    private String extension;

    public AbstractFileName(final String scheme,
                            final String absPath)
    {
        this.scheme = scheme;
        if (absPath != null && absPath.length() > 0)
        {
            this.absPath = absPath;
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
        return (getRootURI().hashCode() ^ absPath.hashCode());
    }

    /**
     * Determines if this object is equal to another.
     */
    public boolean equals(final Object obj)
    {
        final AbstractFileName name = (AbstractFileName) obj;
        return (getRootURI().equals(name.getRootURI()) && absPath.equals(name.absPath));
    }

    /**
     * Implement Comparable
     *
     * @param obj another abstractfilename
     * @return
     */
    public int compareTo(Object obj)
    {
        final AbstractFileName name = (AbstractFileName) obj;
        int ret = getRootURI().compareTo(name.getRootURI());
        if (ret != 0)
        {
            return ret;
        }

        return absPath.compareTo(name.absPath);
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
    protected abstract FileName createName(String absPath);

    /**
     * Builds the root URI for this file name.  Note that the root URI must not
     * end with a separator character.
     */
    protected abstract void appendRootUri(StringBuffer buffer);

    /**
     * Returns the base name of the file.
     */
    public String getBaseName()
    {
        if (baseName == null)
        {
            final int idx = absPath.lastIndexOf(SEPARATOR_CHAR);
            if (idx == -1)
            {
                baseName = absPath;
            }
            else
            {
                baseName = absPath.substring(idx + 1);
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
        return absPath;
    }

    /**
     * Resolves a name, relative to this file name.
     */
    public FileName resolveName(final String name,
                                final NameScope scope)
        throws FileSystemException
    {
        final StringBuffer buffer = new StringBuffer(name);

        // Adjust separators
        UriParser.fixSeparators(buffer);

        // Determine whether to prepend the base path
        if (name.length() == 0 || name.charAt(0) != SEPARATOR_CHAR)
        {
            // Supplied path is not absolute
            buffer.insert(0, SEPARATOR_CHAR);
            buffer.insert(0, absPath);
        }

        // Normalise the path
        UriParser.normalisePath(buffer);

        // Check the name is ok
        final String resolvedPath = buffer.toString();
        if (!checkName(absPath, resolvedPath, scope))
        {
            throw new FileSystemException("vfs.provider/invalid-descendent-name.error", name);
        }

        return createName(resolvedPath);
    }

    /**
     * Returns the name of the parent of the file.
     */
    public FileName getParent()
    {
        final String parentPath;
        final int idx = absPath.lastIndexOf(SEPARATOR_CHAR);
        if (idx == -1 || idx == absPath.length() - 1)
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
            parentPath = absPath.substring(0, idx);
        }
        return createName(parentPath);
    }

    /**
     * Resolves a name, relative to the file.  If the supplied name is an
     * absolute path, then it is resolved relative to the root of the
     * file system that the file belongs to.  If a relative name is supplied,
     * then it is resolved relative to this file name.
     */
    public FileName resolveName(final String path) throws FileSystemException
    {
        return resolveName(path, NameScope.FILE_SYSTEM);
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
            final StringBuffer buffer = new StringBuffer();
            appendRootUri(buffer);
            buffer.append(absPath);
            uri = buffer.toString();
        }
        return uri;
    }

    /**
     * Converts a file name to a relative name, relative to this file name.
     */
    public String getRelativeName(final FileName name) throws FileSystemException
    {
        final String path = name.getPath();

        // Calculate the common prefix
        final int basePathLen = absPath.length();
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
        for (; pos < maxlen && absPath.charAt(pos) == path.charAt(pos); pos++)
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
        if (pathLen > 1 && (pos < pathLen || absPath.charAt(pos) != SEPARATOR_CHAR))
        {
            // Not a direct ancestor, need to back up
            pos = absPath.lastIndexOf(SEPARATOR_CHAR, pos);
            buffer.append(path.substring(pos));
        }

        // Prepend a '../' for each element in the base path past the common
        // prefix
        buffer.insert(0, "..");
        pos = absPath.indexOf(SEPARATOR_CHAR, pos + 1);
        while (pos != -1)
        {
            buffer.insert(0, "../");
            pos = absPath.indexOf(SEPARATOR_CHAR, pos + 1);
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
            appendRootUri(buffer);
            buffer.append(SEPARATOR_CHAR);
            rootUri = buffer.toString();
        }
        return rootUri;
    }

    /**
     * Returns the depth of this file name, within its file system.
     */
    public int getDepth()
    {
        final int len = absPath.length();
        if (len == 0 || (len == 1 && absPath.charAt(0) == SEPARATOR_CHAR))
        {
            return 0;
        }
        int depth = 1;
        for (int pos = 0; pos > -1 && pos < len; depth++)
        {
            pos = absPath.indexOf(SEPARATOR_CHAR, pos + 1);
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
                extension = baseName.substring(pos + 1);
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
        return checkName(ancestor.getPath(), absPath, NameScope.DESCENDENT);
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
        return checkName(absPath, descendent.getPath(), scope);
    }

    /**
     * Checks whether a path fits in a particular scope of another path.
     *
     * @param basePath An absolute, normalised path.
     * @param path     An absolute, normalised path.
     */
    private boolean checkName(final String basePath,
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
        final int baseLen = basePath.length();

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
}
