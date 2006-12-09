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
package org.apache.commons.vfs.provider.webdav;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.AbstractRandomAccessContent;
import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.commons.vfs.provider.URLFileName;
import org.apache.commons.vfs.provider.AbstractRandomAccessStreamContent;
import org.apache.commons.vfs.util.FileObjectUtils;
import org.apache.commons.vfs.util.MonitorOutputStream;
import org.apache.commons.vfs.util.RandomAccessMode;
import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.DepthSupport;
import org.apache.webdav.lib.methods.OptionsMethod;
import org.apache.webdav.lib.methods.XMLResponseMethodBase;
import org.apache.webdav.lib.properties.ResourceTypeProperty;

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A WebDAV file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class WebdavFileObject
    extends AbstractFileObject
    implements FileObject
{
    private final WebDavFileSystem fileSystem;
    private final String urlCharset;
    private WebdavResource resource;
    private boolean redirectionResolved = false;
    private Set allowedMethods = null;
    // private HttpURL url;

    private static volatile int tmpFileCount = 0;
    private static final Object tmpFileCountSync = new Object();

    protected WebdavFileObject(final GenericFileName name,
                               final WebDavFileSystem fileSystem)
    {
        super(name, fileSystem);
        this.fileSystem = fileSystem;
        this.urlCharset = WebdavFileSystemConfigBuilder.getInstance().getUrlCharset(getFileSystem().getFileSystemOptions());
    }

    /**
     * Attaches this file object to its file resource.
     */
    protected void doAttach() throws Exception
    {
        if (resource == null)
        {
            setDavResource(null);
        }
    }

    protected void doDetach() throws Exception
    {
        if (resource != null)
        {
            // clear cached data
            redirectionResolved = false;
            allowedMethods = null;

            resource.close();
            resource = null;
        }
    }

    /**
     * set the davResource
     *
     * @param resource
     * @throws Exception
     */
    private void setDavResource(WebdavResource resource) throws Exception
    {
        redirectionResolved = false;

        final URLFileName name = (URLFileName) getName();

        if (resource == null)
        {
            // HttpURL url = new HttpURL(name.getHostName(), name.getPort(), name.getPath());
            String pathEncoded = name.getPathQueryEncoded(urlCharset);
            HttpURL url = new HttpURL(name.getUserName(), name.getPassword(), name.getHostName(), name.getPort());
            url.setEscapedPath(pathEncoded);
            resource = new WebdavResource(fileSystem.getClient())
            {
            };
            resource.setHttpURL(url, WebdavResource.NOACTION, 1);
        }

        this.resource = resource;

        // if (bCheckExists)
        {
            /* now fill the dav properties */
            String pathEncoded = name.getPathQueryEncoded(urlCharset);
            final OptionsMethod optionsMethod = new OptionsMethod(pathEncoded);
            configureMethod(optionsMethod);
            try
            {
                optionsMethod.setFollowRedirects(true);
                final int status = fileSystem.getClient().executeMethod(optionsMethod);
                if (status < 200 || status > 299)
                {
                    if (status == 401 || status == 403)
                    {
                        setAllowedMethods(null);

                        // permission denied on this object, but we might get some informations from the parent
                        processParentDavResource();
                        return;
                    }
                    else
                    {
                        injectType(FileType.IMAGINARY);
                    }
                    return;
                }
                // handle the (maybe) redirected url
                redirectionResolved = true;
                resource.getHttpURL().setEscapedPath(optionsMethod.getURI().getPath());

                setAllowedMethods(optionsMethod.getAllowedMethods());
                boolean exists = false;
                for (Enumeration enumeration = optionsMethod.getAllowedMethods(); enumeration.hasMoreElements();)
                {
                    final String method = (String) enumeration.nextElement();
                    // IIS allows GET even if the file is non existend - so changed to COPY
                    // if (method.equals("GET"))
                    if (method.equals("COPY"))
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
            finally
            {
                optionsMethod.releaseConnection();
            }
        }

        ResourceTypeProperty resourceType = resource.getResourceType();
        if (resourceType.isCollection())
        {
            injectType(FileType.FOLDER);
        }
        else
        {
            injectType(FileType.FILE);
        }
    }

    protected void configureMethod(HttpMethodBase httpMethod)
    {
        httpMethod.setMethodRetryHandler(WebdavMethodRetryHandler.getInstance());
    }

    private void setAllowedMethods(Enumeration allowedMethods)
    {
        this.allowedMethods = new TreeSet();

        if (allowedMethods == null)
        {
            return;
        }

        while (allowedMethods.hasMoreElements())
        {
            this.allowedMethods.add(allowedMethods.nextElement());
        }
    }

    private boolean hasAllowedMethods(String method) throws IOException
    {
        if (allowedMethods == null)
        {
            getAllowedMethods();
        }

        return allowedMethods.contains(method);
    }

    private void resolveRedirection() throws IOException, FileSystemException
    {
        if (redirectionResolved)
        {
            return;
        }

        final OptionsMethod optionsMethod = new OptionsMethod(getName().getPath());
        configureMethod(optionsMethod);
        try
        {
            optionsMethod.setFollowRedirects(true);
            final int status = fileSystem.getClient().executeMethod(optionsMethod);
            if (status >= 200 && status <= 299)
            {
                setAllowedMethods(optionsMethod.getAllowedMethods());
                resource.getHttpURL().setEscapedPath(optionsMethod.getPath());
                redirectionResolved = true;
            }
        }
        finally
        {
            optionsMethod.releaseConnection();
        }
    }

    private void processParentDavResource() throws FileSystemException
    {
        WebdavFileObject parent = (WebdavFileObject) FileObjectUtils.getAbstractFileObject(getParent());
        try
        {
            // after this our resource should be reset
            parent.doListChildrenResolved();
        }
        catch (Exception e)
        {
            throw new FileSystemException(e);
        }
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     */
    protected FileType doGetType() throws Exception
    {
        // return doGetType(null);
        throw new IllegalStateException("this should not happen");
    }

    /**
     * Lists the children of the file.
     */
    protected String[] doListChildren() throws Exception
    {
        // use doListChildrenResolved for performance
        return null;
    }

    /**
     * Lists the children of the file.
     */
    protected FileObject[] doListChildrenResolved() throws Exception
    {
        doAttach();

        WebdavResource[] children = new org.apache.webdav.lib.WebdavResource[0];
        try
        {
            children = resource.listWebdavResources();
        }
        catch (HttpException e)
        {
            if (e.getReasonCode() == HttpStatus.SC_MOVED_PERMANENTLY || e.getReasonCode() == HttpStatus.SC_MOVED_TEMPORARILY)
            {
                resolveRedirection();
                children = resource.listWebdavResources();
            }
            else
            {
                throw e;
            }
        }

        if (children == null)
        {
            throw new FileSystemException("vfs.provider.webdav/list-children.error", resource.getStatusMessage());
        }

        List vfs = new ArrayList(children.length);
        // WebdavFileObject[] vfs = new WebdavFileObject[children.length];
        for (int i = 0; i < children.length; i++)
        {
            WebdavResource dav = children[i];

            String davName = dav.getHttpURL().getEscapedName();
            if ("".equals(davName))
            {
                // current file
                continue;
            }

            WebdavFileObject fo = (WebdavFileObject) FileObjectUtils.getAbstractFileObject(getFileSystem().resolveFile(
                getFileSystem().getFileSystemManager().resolveName(
                    getName(),
                    davName,
                    NameScope.CHILD)));
            fo.setDavResource(dav);

            // vfs[i] = fo;
            vfs.add(fo);
        }

        return (WebdavFileObject[]) vfs.toArray(new WebdavFileObject[vfs.size()]);
        // return vfs;
    }

    /**
     * Creates this file as a folder.
     */
    protected void doCreateFolder() throws Exception
    {
        // Adjust resource path
        //// resource.getHttpURL().setEscapedPath(getName().getPath() + '/');
        resource.getHttpURL().setPath(getName().getPathDecoded() + '/');
        final boolean ok = resource.mkcolMethod();
        if (!ok)
        {
            throw new FileSystemException("vfs.provider.webdav/create-collection.error", resource.getStatusMessage());
        }

        // reread allowed methods
        reattach();
    }

    /**
     * Deletes the file.
     */
    protected void doDelete() throws Exception
    {
        resolveRedirection();
        // final boolean ok = resource.deleteMethod(getName().getPathDecoded() /*url.getPath()*/);
        final boolean ok = resource.deleteMethod();
        if (!ok)
        {
            throw new FileSystemException("vfs.provider.webdav/delete-file.error", resource.getStatusMessage());
        }

        // reread allowed methods
        reattach();
    }

    /**
     * Rename the file.
     */
    protected void doRename(FileObject newfile) throws Exception
    {
        // final GenericFileName name = (GenericFileName) newfile.getName();
        // HttpURL url = new HttpURL(name.getUserName(), name.getPassword(), name.getHostName(), name.getPort(), newfile.getName().getPath());
        // String uri = url.getURI();

        final boolean ok = resource.moveMethod(newfile.getName().getPath());
        if (!ok)
        {
            throw new FileSystemException("vfs.provider.webdav/rename-file.error", resource.getStatusMessage());
        }

        // reread allowed methods
        reattach();
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
        int fileCount;
        FileObject webdavTmp;
        synchronized (tmpFileCountSync)
            {
                tmpFileCount++;
                fileCount = tmpFileCount;
            }
        webdavTmp = getFileSystem().getFileSystemManager().resolveFile("tmp://webdav_tmp.c" + fileCount);
        return new WebdavOutputStream(webdavTmp);
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
     * @todo Use piped stream to avoid temporary file
     */
    private class WebdavOutputStream
        extends MonitorOutputStream
    {
        private final FileObject webdavTmp;

        public WebdavOutputStream(FileObject webdavTmp) throws FileSystemException
        {
            super(webdavTmp.getContent().getOutputStream());
            this.webdavTmp = webdavTmp;
        }

        /**
         * Called after this stream is closed.
         */
        protected void onClose() throws IOException
        {
            // final ByteArrayOutputStream outstr = (ByteArrayOutputStream) out;

            // Adjust the resource path (this file object may have been a folder)
            resource.getHttpURL().setPath(getName().getPathDecoded());
            // final boolean ok = resource.putMethod(outstr.toByteArray());
            try
            {
                final boolean ok = resource.putMethod(webdavTmp.getContent().getInputStream());
                if (!ok)
                {
                    throw new FileSystemException("vfs.provider.webdav/write-file.error", resource.getStatusMessage());
                }
            }
            finally
            {
                // close and delete the temporary file
                webdavTmp.close();
                webdavTmp.delete();
            }
        }
    }

    protected void handleCreate(final FileType newType) throws Exception
    {
        // imario@apache.org: this is to reread the webdav internal state
        // Ill treat this as workaround
        reattach();
        super.handleCreate(newType);
    }

    /**
     * refresh the webdav internals
     *
     * @throws FileSystemException
     */
    private void reattach() throws FileSystemException
    {
        try
        {
            doDetach();
            doAttach();
        }
        catch (Exception e)
        {
            throw new FileSystemException(e);
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

    protected boolean doIsReadable() throws Exception
    {
        return hasAllowedMethods("GET");
    }

    protected boolean doIsWriteable() throws Exception
    {
        // Again to be IIS compatible
        // return hasAllowedMethods("POST");
        return hasAllowedMethods("DELETE");
    }

    private void getAllowedMethods() throws IOException
    {
        if (allowedMethods != null)
        {
            return;
        }

        final OptionsMethod optionsMethod = new OptionsMethod(getName().getPath());
        configureMethod(optionsMethod);
        try
        {
            optionsMethod.setFollowRedirects(true);
            final int status = fileSystem.getClient().executeMethod(optionsMethod);
            if (status < 200 || status > 299)
            {
                if (status == 401 || status == 403)
                {
                    setAllowedMethods(null);
                    return;
                }
            }

            setAllowedMethods(optionsMethod.getAllowedMethods());
        }
        finally
        {
            optionsMethod.releaseConnection();
        }

        return;
    }

    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception
    {
        return new WebdavRandomAccesContent(this, mode);
    }

    public static class WebdavRandomAccesContent extends AbstractRandomAccessStreamContent
	{
        private final WebdavFileObject fileObject;

        protected long filePointer = 0;

        private DataInputStream dis = null;

        private InputStream mis = null;

        protected WebdavRandomAccesContent(final WebdavFileObject fileObject, final RandomAccessMode mode)
        {
            super(mode);

            this.fileObject = fileObject;
        }

        public long getFilePointer() throws IOException
        {
            return filePointer;
        }

        public void seek(long pos) throws IOException
        {
            if (pos == filePointer)
            {
                // no change
                return;
            }

            if (pos < 0)
            {
                throw new FileSystemException(
                    "vfs.provider/random-access-invalid-position.error",
                    new Object[]{new Long(pos)});
            }
            if (dis != null)
            {
                close();
            }

            filePointer = pos;
        }

		protected DataInputStream getDataInputStream() throws IOException
        {
            if (dis != null)
            {
                return dis;
            }

            fileObject.resource.addRequestHeader("Range", "bytes="
                + filePointer + "-");
            final InputStream data = fileObject.resource.getMethodData();
            final int status = fileObject.resource.getStatusCode();

            if (status != HttpURLConnection.HTTP_PARTIAL)
            {
                data.close();
                throw new FileSystemException(
                    "vfs.provider.http/get-range.error", new Object[]{
                    fileObject.getName(), new Long(filePointer)});
            }

            mis = data;
            dis = new DataInputStream(new FilterInputStream(mis)
            {
                public int read() throws IOException
                {
                    int ret = super.read();
                    if (ret > -1)
                    {
                        filePointer++;
                    }
                    return ret;
                }

                public int read(byte b[]) throws IOException
                {
                    int ret = super.read(b);
                    if (ret > -1)
                    {
                        filePointer += ret;
                    }
                    return ret;
                }

                public int read(byte b[], int off, int len) throws IOException
                {
                    int ret = super.read(b, off, len);
                    if (ret > -1)
                    {
                        filePointer += ret;
                    }
                    return ret;
                }
            });

			return dis;
		}

        public void close() throws IOException
        {
            if (dis != null)
            {
                dis.close();
                dis = null;
                mis = null;
            }
        }

        public long length() throws IOException
        {
            return fileObject.getContent().getSize();
        }
    }
}