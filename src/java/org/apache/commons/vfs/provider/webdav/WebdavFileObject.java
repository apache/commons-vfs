/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.commons.vfs.provider.webdav;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.commons.vfs.util.MonitorOutputStream;
import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.DepthSupport;
import org.apache.webdav.lib.methods.OptionsMethod;
import org.apache.webdav.lib.methods.PropFindMethod;
import org.apache.webdav.lib.methods.XMLResponseMethodBase;
import org.apache.webdav.lib.properties.ResourceTypeProperty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * A WebDAV file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.15 $ $Date: 2004/05/22 20:32:04 $
 */
public class WebdavFileObject
    extends AbstractFileObject
    implements FileObject
{
    private final WebDavFileSystem fileSystem;
    private WebdavResource resource;
    private HttpURL url;

    private final static Vector PROPS_TYPE = new Vector(Arrays.asList(new String[]
    {
        WebdavResource.RESOURCETYPE
    }));

    public WebdavFileObject(final GenericFileName name,
                            final WebDavFileSystem fileSystem)
    {
        super(name, fileSystem);
        this.fileSystem = fileSystem;
    }

    /**
     * Attaches this file object to its file resource.
     */
    protected void doAttach() throws Exception
    {
        final GenericFileName name = (GenericFileName) getName();
        url = new HttpURL(name.getUserName(), name.getPassword(), name.getHostName(), name.getPort(), name.getPath());
        resource = new WebdavResource(fileSystem.getClient())
        {
        };
        resource.setHttpURL(url, WebdavResource.NOACTION, 1);

        /* @todo: this should not be done in doAttach - will move later */
        /* only needet to fill the properties in resource */
        final OptionsMethod optionsMethod = new OptionsMethod(getName().getPath());
        optionsMethod.setFollowRedirects(true);
        final int status = fileSystem.getClient().executeMethod(optionsMethod);
        if (status < 200 || status > 299)
        {
            injectType(FileType.IMAGINARY);
            return;
        }
        // handle the (maybe) redirected url
        resource.getHttpURL().setPath(optionsMethod.getPath());

        boolean exists = false;
        for (Enumeration enum = optionsMethod.getAllowedMethods(); enum.hasMoreElements();)
        {
            final String method = (String) enum.nextElement();
            if (method.equals("GET"))
            {
                exists = true;
                break;
            }
        }
        if (!exists)
        {
            injectType(FileType.IMAGINARY);
            return;
        }

        try
        {
            resource.setProperties(WebdavResource.DEFAULT, 1);
        }
        catch (IOException e)
        {
            throw new FileSystemException(e);
        }
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     *
     * @todo Shouldn't need 2 trips to the server to determine type.
     */
    protected FileType doGetType() throws Exception
    {
        return doGetType(null);
    }

    private FileType doGetType(final String child) throws Exception
    {
        // do propfind on resource
        final int depth = child == null ? PropFindMethod.DEPTH_0 : PropFindMethod.DEPTH_1;
        final PropFindMethod propfindMethod = new PropFindMethod(getName().getPath(), depth, PROPS_TYPE.elements());
        // propfindMethod.setFollowRedirects(true);
        final int status = fileSystem.getClient().executeMethod(propfindMethod);
        if (status < 200 || status > 299)
        {
            if (child == null && (status == 401 || status == 403))
            {
                // This second pass should only happen if a secured resource was directly resolved.
                // using getChildren() on the parent already inject the type
                WebdavFileObject parent = (WebdavFileObject) getParent();
                if (parent != null)
                {
                    // premission denied
                    // ask the parent to find our type - this is bad
                    return parent.doGetType(getName().getBaseName());
                }
            }

            return FileType.IMAGINARY;
        }

        // handle the (maybe) redirected url
        // resource.getHttpURL().setPath(propfindMethod.getPath());

        // find the ResourceTypeProperty
        String dirChild = null;
        if (child != null)
        {
            dirChild = child + "/";
        }

        Enumeration enum = propfindMethod.getResponses();
        while (enum.hasMoreElements())
        {
            ResponseEntity response = (ResponseEntity) enum.nextElement();
            if (child == null || response.getHref().endsWith(child) || response.getHref().endsWith(dirChild))
            {
                Enumeration properties = response.getProperties();
                while (properties.hasMoreElements())
                {
                    Object property = properties.nextElement();
                    if (property instanceof ResourceTypeProperty)
                    {
                        ResourceTypeProperty resourceType = (ResourceTypeProperty) property;
                        if (resourceType.isCollection())
                        {
                            return FileType.FOLDER;
                        }
                        else
                        {
                            return FileType.FILE;
                        }
                    }
                }
            }
        }

        return FileType.IMAGINARY;

        // Determine whether the resource exists, and whether it is a DAV resource
        /*
        final OptionsMethod optionsMethod = new OptionsMethod(getName().getPath());
        optionsMethod.setFollowRedirects(true);
        final int status = fileSystem.getClient().executeMethod(optionsMethod);
        if (status < 200 || status > 299)
        {
            return FileType.IMAGINARY;
        }
        resource.getHttpURL().setPath(optionsMethod.getPath());

        // Resource exists if we can do a GET on it
        boolean exists = false;
        for (Enumeration enum = optionsMethod.getAllowedMethods(); enum.hasMoreElements();)
        {
            final String method = (String) enum.nextElement();
            if (method.equals("GET"))
            {
                exists = true;
                break;
            }
        }
        if (!exists)
        {
            return FileType.IMAGINARY;
        }

        // Check if the resource is a DAV resource
        final boolean davResource = optionsMethod.getDavCapabilities().hasMoreElements();
        if (!davResource)
        {
            // Assume a folder, and don't get the properties
            return FileType.FOLDER;
        }

        // Get the properties of the resource
        resource.setProperties(WebdavResource.DEFAULT, 1);
        if (resource.isCollection())
        {
            return FileType.FOLDER;
        }
        else
        {
            return FileType.FILE;
        }
        */
    }

    /**
     * Lists the children of the file.
     */
    protected String[] doListChildren() throws Exception
    {
        return null;
        /*
        final String[] children = resource.list();
        if (children == null)
        {
            throw new FileSystemException("vfs.provider.webdav/list-children.error", resource.getStatusMessage());
        }
        return children;
        */
    }

    /**
     * Lists the children of the file.
     */
    protected FileObject[] doListChildrenResolved() throws Exception
    {
        WebdavResource[] children = resource.listWebdavResources();
        if (children == null)
        {
            throw new FileSystemException("vfs.provider.webdav/list-children.error", resource.getStatusMessage());
        }

        WebdavFileObject[] vfs = new WebdavFileObject[children.length];
        for (int i = 0; i < children.length; i++)
        {
            WebdavResource dav = children[i];

            WebdavFileObject fo = (WebdavFileObject) getFileSystem().resolveFile(getName().resolveName(dav.getDisplayName(), NameScope.CHILD));
            fo.injectType(dav.isCollection() ? FileType.FOLDER : FileType.FILE);

            vfs[i] = fo;
        }

        return vfs;
    }

    protected void injectType(FileType fileType)
    {
        super.injectType(fileType);
    }

    /**
     * Creates this file as a folder.
     */
    protected void doCreateFolder() throws Exception
    {
        // Adjust resource path
        resource.getHttpURL().setPath(getName().getPath() + '/');
        final boolean ok = resource.mkcolMethod();
        if (!ok)
        {
            throw new FileSystemException("vfs.provider.webdav/create-collection.error", resource.getStatusMessage());
        }
    }

    /**
     * Deletes the file.
     */
    protected void doDelete() throws Exception
    {
        final boolean ok = resource.deleteMethod(url.getPath());
        if (!ok)
        {
            throw new FileSystemException("vfs.provider.webdav/delete-file.error", resource.getStatusMessage());
        }
    }

    /**
     * Rename the file.
     */
    protected void doRename(FileObject newfile) throws Exception
    {
        final boolean ok = resource.moveMethod(newfile.getName().getPath());
        if (!ok)
        {
            throw new FileSystemException("vfs.provider.webdav/rename-file.error", resource.getStatusMessage());
        }
    }

    /**
     * Creates an input stream to read the file content from.
     */
    protected InputStream doGetInputStream() throws Exception
    {
        return resource.getMethodData();
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream(boolean bAppend) throws Exception
    {
        return new WebdavOutputStream();
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize() throws Exception
    {
        return resource.getGetContentLength();
    }

    /**
     * An OutputStream that writes to a Webdav resource.
     *
     * @todo Don't gather up the body in a ByteArrayOutputStream; need to write directly to connection
     */
    private class WebdavOutputStream
        extends MonitorOutputStream
    {
        public WebdavOutputStream()
        {
            super(new ByteArrayOutputStream());
        }

        /**
         * Called after this stream is closed.
         */
        protected void onClose() throws IOException
        {
            final ByteArrayOutputStream outstr = (ByteArrayOutputStream) out;

            // Adjust the resource path (this file object may have been a folder)
            resource.getHttpURL().setPath(getName().getPath());
            final boolean ok = resource.putMethod(outstr.toByteArray());
            if (!ok)
            {
                throw new FileSystemException("vfs.provider.webdav/write-file.error", resource.getStatusMessage());
            }
        }
    }

    /**
     * Returns the last modified time of this file.  Is only called if
     * {@link #doGetType} does not return {@link FileType#IMAGINARY}.
     */
    protected long doGetLastModifiedTime() throws Exception
    {
        return resource.getGetLastModified();
    }

    /**
     * Returns the properties of the Webdav resource.
     */
    protected Map doGetAttributes() throws Exception
    {
        final Map attributes = new HashMap();
        final Enumeration e = resource.propfindMethod(DepthSupport.DEPTH_0);
        while (e.hasMoreElements())
        {
            final XMLResponseMethodBase.Response response = (XMLResponseMethodBase.Response) e.nextElement();
            final Enumeration properties = response.getProperties();
            while (properties.hasMoreElements())
            {
                final BaseProperty property = (BaseProperty) properties.nextElement();
                attributes.put(property.getLocalName(), property.getPropertyAsString());
            }
        }

        return attributes;
    }
}
