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

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.vfs.provider.URLFileName;
import org.apache.commons.vfs.provider.DefaultFileContent;
import org.apache.commons.vfs.provider.http.HttpFileObject;
import org.apache.commons.vfs.util.FileObjectUtils;
import org.apache.commons.vfs.util.MonitorOutputStream;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.FileNotFolderException;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileNotFoundException;
import org.apache.commons.vfs.FileContentInfoFactory;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.client.methods.CheckoutMethod;
import org.apache.jackrabbit.webdav.client.methods.CheckinMethod;
import org.apache.jackrabbit.webdav.client.methods.UncheckoutMethod;
import org.apache.jackrabbit.webdav.client.methods.VersionControlMethod;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;
import org.apache.jackrabbit.webdav.version.VersionControlledResource;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Iterator;

/**
 * A WebDAV file.
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @version $Revision$
 */
public class WebdavFileObject
    extends HttpFileObject
    implements FileObject
{
    private final WebdavFileSystem fileSystem;
    private final String urlCharset;
    public static final DavPropertyName RESPONSE_CHARSET = DavPropertyName.create("response-charset");
    public final WebdavFileSystemConfigBuilder builder;

    protected WebdavFileObject(final FileName name,
                               final WebdavFileSystem fileSystem)
    {
        super(name, fileSystem);
        this.fileSystem = fileSystem;
        builder =
            (WebdavFileSystemConfigBuilder)WebdavFileSystemConfigBuilder.getInstance();
        this.urlCharset = builder.getUrlCharset(getFileSystem().getFileSystemOptions());
    }

    protected void configureMethod(HttpMethodBase httpMethod)
    {
        httpMethod.setMethodRetryHandler(WebdavMethodRetryHandler.getInstance());
    }

    /**
     * Determines the type of this file.  Must not return null.  The return
     * value of this method is cached, so the implementation can be expensive.
     */
    protected FileType doGetType()
        throws Exception
    {
        try
        {
            return isDirectory((URLFileName)getName()) ? FileType.FOLDER : FileType.FILE;
        }
        catch (FileNotFolderException fnfe)
        {
            return FileType.IMAGINARY;
        }
        catch (FileNotFoundException fnfe)
        {
            return FileType.IMAGINARY;
        }

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
        PropFindMethod method = null;
        try
        {
            URLFileName name = (URLFileName)getName();
            if ( isDirectory( name ) )
            {
                DavPropertyNameSet nameSet = new DavPropertyNameSet();
                nameSet.add( DavPropertyName.create( DavConstants.PROPERTY_DISPLAYNAME ) );

                method = new PropFindMethod( urlString(name), nameSet,
                        DavConstants.DEPTH_1 );

                execute( method );
                List vfs = new ArrayList();
                if ( method.succeeded() )
                {
                    MultiStatusResponse[] responses =
                            method.getResponseBodyAsMultiStatus().getResponses();

                    for (int i=0; i < responses.length; ++i)
                    {
                        MultiStatusResponse response = responses[i];
                        if (isCurrentFile(response.getHref(), name))
                        {
                            continue;
                        }
                        String resourceName = resourceName(response.getHref());
                        if (resourceName != null && resourceName.length() > 0)
                        {
                            WebdavFileObject fo = (WebdavFileObject)FileObjectUtils.
                                getAbstractFileObject(getFileSystem().resolveFile(
                                    getFileSystem().getFileSystemManager().
                                        resolveName(getName(), resourceName, NameScope.CHILD)));
                            vfs.add(fo);
                        }
                    }
                }
                return (WebdavFileObject[]) vfs.toArray(new WebdavFileObject[vfs.size()]);
            }
            throw new FileNotFolderException(getName());
        }
        catch ( FileNotFolderException fnfe)
        {
            throw fnfe;
        }
        catch ( DavException e )
        {
            throw new FileSystemException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new FileSystemException( e.getMessage(), e );
        }
        finally
        {
            if ( method != null )
                method.releaseConnection();
        }
    }

    /**
     * Creates this file as a folder.
     */
    protected void doCreateFolder() throws Exception
    {
        DavMethod method = new MkColMethod(urlString((URLFileName)getName()));
        setupMethod(method);
        try
        {
            execute(method);
        }
        catch (FileSystemException fse)
        {
            throw new FileSystemException("vfs.provider.webdav/create-collection.error", getName(),
                                          fse);
        }
    }

    /**
     * Deletes the file.
     */
    protected void doDelete() throws Exception
    {
        DavMethod method = new DeleteMethod(urlString((URLFileName)getName()));
        setupMethod(method);
        execute(method);
    }

    /**
     * Rename the file.
     */
    protected void doRename(FileObject newfile) throws Exception
    {
        String url = encodePath(urlString((URLFileName)getName()));
        String dest = urlString((URLFileName)newfile.getName(), false);
        DavMethod method = new MoveMethod(url, dest, false);
        setupMethod(method);
        execute(method);
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize() throws Exception
    {
        DavProperty property = getProperty((URLFileName)getName(),
                DavConstants.PROPERTY_GETCONTENTLENGTH);
        if ( property != null )
        {
            String value = (String) property.getValue();
            return Long.parseLong(value);
        }
        return 0;
    }

    /**
     * Returns the last modified time of this file.  Is only called if
     * {@link #doGetType} does not return {@link FileType#IMAGINARY}.
     */
    protected long doGetLastModifiedTime() throws Exception
    {
        DavProperty property = getProperty((URLFileName)getName(),
                DavConstants.PROPERTY_GETLASTMODIFIED);
        if ( property != null )
        {
            String value = (String) property.getValue();
            return DateUtil.parseDate(value).getTime();
        }
        return 0;
    }

    /**
     * Returns the properties of the Webdav resource.
     */
    protected Map doGetAttributes() throws Exception
    {
        final Map attributes = new HashMap();
        try
        {
            URLFileName fileName = (URLFileName)getName();
            DavPropertySet properties = getProperties(fileName, PropFindMethod.PROPFIND_ALL_PROP,
                    new DavPropertyNameSet(), false);
            Iterator iter = properties.iterator();
            while (iter.hasNext())
            {
                DavProperty property = (DavProperty)iter.next();
                attributes.put(property.getName().toString(), property.getValue());
            }
            properties = getPropertyNames(fileName);
            iter = properties.iterator();
            while (iter.hasNext())
            {
                DavProperty property = (DavProperty)iter.next();
                if (!attributes.containsKey(property.getName()))
                {
                    property = getProperty(fileName, property.getName());
                    attributes.put(property.getName().toString(), property.getValue());
                }               
            }
            return attributes;
        }
        catch (Exception e)
        {
            throw new FileSystemException("vfs.provider.webdav/propfind.error", getName(), e);
        }
    }

    protected OutputStream doGetOutputStream(boolean bAppend) throws Exception
    {
        return new WebdavOutputStream(this);
    }

    protected FileContentInfoFactory getFileContentInfoFactory()
    {
        return new WebdavFileContentInfoFactory();
    }

    /**
     * Prepares a Method object.
     * @param method the HttpMethod.
     * @throws FileSystemException if an error occurs encoding the uri.
     * @throws URIException if the URI is in error.
     */
    protected void setupMethod(final HttpMethod method) throws FileSystemException, URIException
    {
        String pathEncoded = ((URLFileName) getName()).getPathQueryEncoded(urlCharset);
        method.setPath(pathEncoded);
        // All the WebDav methods are EntityEnclosingMethods and are not allowed to redirect.
        method.setFollowRedirects(false);
        method.setRequestHeader("User-Agent", "Jakarta-Commons-VFS");
        method.addRequestHeader( "Cache-control", "no-cache" );
        method.addRequestHeader( "Cache-store", "no-store" );
        method.addRequestHeader( "Pragma", "no-cache" );
        method.addRequestHeader( "Expires", "0" );
    }

    /**
     * Execute a 'Workspace' operation.
     *
     * @param method The DavMethod to invoke.
     * @throws FileSystemException If an error occurs.
     */
    private void execute(DavMethod method) throws FileSystemException
    {
        try
        {
            int status = fileSystem.getClient().executeMethod(method);
            if (status == HttpURLConnection.HTTP_NOT_FOUND
                 || status == HttpURLConnection.HTTP_GONE)
            {
                throw new FileNotFoundException(method.getURI());
            }
            method.checkSuccess();
        }
        catch (FileSystemException fse)
        {
            throw fse;
        }
        catch (IOException e)
        {
            throw new FileSystemException(e);
        }
        catch (DavException e)
        {
            throw ExceptionConverter.generate(e);
        }
        finally
        {
            if (method != null)
            {
                method.releaseConnection();
            }
        }
    }

    private boolean isDirectory(URLFileName name) throws IOException, DavException
    {
        try
        {
            DavProperty property = getProperty(name, DavConstants.PROPERTY_RESOURCETYPE);
            Node node;
            if ( property != null && (node = (Node)property.getValue()) != null)
            {
                return node.getLocalName().equals( DavConstants.XML_COLLECTION );
            }
            else
            {
                return false;
            }
        }
        catch (FileNotFoundException fse)
        {
            throw new FileNotFolderException(name);
        }
    }

    DavProperty getProperty(URLFileName fileName, String property)
            throws FileSystemException
    {
        return getProperty(fileName, DavPropertyName.create(property));
    }

    DavProperty getProperty(URLFileName fileName, DavPropertyName name)
            throws FileSystemException
    {
        DavPropertyNameSet nameSet = new DavPropertyNameSet();
        nameSet.add(name);
        DavPropertySet propertySet = getProperties(fileName, nameSet, false);
        return propertySet.get(name);
    }

    DavPropertySet getProperties(URLFileName name, DavPropertyNameSet nameSet, boolean addEncoding)
            throws FileSystemException
    {
        return getProperties(name, PropFindMethod.PROPFIND_BY_PROPERTY, nameSet, addEncoding);
    }

    DavPropertySet getProperties(URLFileName name) throws FileSystemException
    {
        return getProperties(name, PropFindMethod.PROPFIND_ALL_PROP, new DavPropertyNameSet(), false);
    }


    DavPropertySet getPropertyNames(URLFileName name) throws FileSystemException
    {
        return getProperties(name, PropFindMethod.PROPFIND_PROPERTY_NAMES,
                new DavPropertyNameSet(), false);
    }

    DavPropertySet getProperties(URLFileName name, int type, DavPropertyNameSet nameSet,
                                 boolean addEncoding)
            throws FileSystemException
    {
        try
        {
            String urlStr = urlString(name);
            PropFindMethod method = new PropFindMethod(urlStr, type, nameSet, DavConstants.DEPTH_0);
            setupMethod(method);
            execute( method );
            if ( method.succeeded() )
            {
                MultiStatus multiStatus = method.getResponseBodyAsMultiStatus();
                MultiStatusResponse response = multiStatus.getResponses()[0];
                DavPropertySet props = response.getProperties( HttpStatus.SC_OK );
                if (addEncoding)
                {
                    DavProperty prop = new DefaultDavProperty(RESPONSE_CHARSET,
                            method.getResponseCharSet());
                    props.add(prop);
                }
                return props;
            }
            return new DavPropertySet();
        }
        catch (FileSystemException fse)
        {
            throw fse;
        }
        catch (Exception e)
        {
            throw new FileSystemException("vfs.provider.webdav/propfind.error", getName(), e);
        }
    }

    /**
     * Returns the resource name from the path.
     * @param path the path to the file.
     * @return The resource name
     */
    private String resourceName( String path )
    {
        if (path.endsWith("/"))
        {
            path = path.substring(0, path.length()-1);
        }
        final int i = path.lastIndexOf( "/" );
        return ( ( i >= 0 ) ? path.substring( i + 1 ) : path );
    }

    private String urlString(URLFileName name)
    {
        return urlString(name, true);
    }

    /**
     * Convert the FileName to an encoded url String.
     * @param name The FileName.
     * @return The encoded URL String.
     */
    private String urlString(URLFileName name, boolean includeUserInfo)
    {
        String user = null;
        String password = null;
        if (includeUserInfo)
        {
            user = name.getUserName();
            password = name.getPassword();
        }
        URLFileName newFile = new URLFileName("http", name.getHostName(), name.getPort(),
                name.getDefaultPort(), user, password,
                name.getPath(), name.getType(), name.getQueryString());
        try
        {
            return newFile.getURIEncoded(urlCharset);
        }
        catch (Exception e)
        {
            return name.getURI();
        }
    }

    private boolean isCurrentFile(String href, URLFileName fileName)
    {
        String name = hrefString(fileName);
        if (href.endsWith("/") && !name.endsWith("/"))
        {
            name += "/";
        }
        return href.equals(name);
    }

    /**
     * Convert the FileName to an encoded url String.
     * @param name The FileName.
     * @return The encoded URL String.
     */
    private String hrefString(URLFileName name)
    {
        URLFileName newFile = new URLFileName("http", name.getHostName(), name.getPort(),
                name.getDefaultPort(), null, null,
                name.getPath(), name.getType(), name.getQueryString());
        try
        {
            return newFile.getURIEncoded(urlCharset);
        }
        catch (Exception e)
        {
            return name.getURI();
        }
    }

    /**
     * An OutputStream that writes to a Webdav resource.
     *
     * @todo Use piped stream to avoid temporary file
     */
    private class WebdavOutputStream extends MonitorOutputStream
    {
        private ByteArrayOutputStream stream;
        private WebdavFileObject file;

        public WebdavOutputStream(WebdavFileObject file) throws FileSystemException
        {
            super(new ByteArrayOutputStream());
            this.file = file;
        }

        /**
         * Called after this stream is closed.
         */
        protected void onClose() throws IOException
        {
            RequestEntity entity = new StringRequestEntity(out.toString());
            URLFileName fileName = (URLFileName)getName();
            String urlStr = urlString(fileName);
            if (builder.isVersioning(getFileSystem().getFileSystemOptions()))
            {
                DavPropertySet set = null;
                boolean fileExists = true;
                boolean isCheckedIn = true;
                try
                {
                    set = getPropertyNames(fileName);
                }
                catch (FileNotFoundException fnfe)
                {
                    fileExists = false;
                }
                if (fileExists && set != null)
                {
                    if (set.contains(VersionControlledResource.CHECKED_OUT))
                    {
                        isCheckedIn = false;
                    }
                    else if (!set.contains(VersionControlledResource.CHECKED_IN))
                    {
                        DavProperty prop = set.get(VersionControlledResource.AUTO_VERSION);
                        if (prop != null)
                        {
                            prop = getProperty(fileName, VersionControlledResource.AUTO_VERSION);
                            if (DeltaVConstants.XML_CHECKOUT_CHECKIN.equals(prop.getValue()))
                            {
                                createVersion(urlStr);
                            }
                        }
                    }
                }
                if (fileExists && isCheckedIn)
                {
                    try
                    {
                        CheckoutMethod checkout = new CheckoutMethod(urlStr);
                        setupMethod(checkout);
                        execute(checkout);
                        isCheckedIn = false;
                    }
                    catch (FileSystemException ex)
                    {
                        //
                    }
                }

                try
                {
                    PutMethod method = new PutMethod(urlStr);
                    method.setRequestEntity(entity);
                    setupMethod(method);
                    execute(method);
                    setUserName(fileName, urlStr);
                }
                catch (FileSystemException ex)
                {
                    if (!isCheckedIn)
                    {
                        try
                        {
                            UncheckoutMethod method = new UncheckoutMethod(urlStr);
                            setupMethod(method);
                            execute(method);
                            isCheckedIn = true;
                        }
                        catch (Exception e)
                        {
                            // Ignore the exception. Going to throw original.
                        }
                        throw ex;
                    }
                }
                if (fileExists)
                {
                    if (!isCheckedIn)
                    {
                        CheckinMethod checkin = new CheckinMethod(urlStr);
                        setupMethod(checkin);
                        execute(checkin);
                    }
                }
                else
                {
                    createVersion(urlStr);       
                }
            }
            else
            {
                PutMethod method = new PutMethod(urlStr);
                method.setRequestEntity(entity);
                setupMethod(method);
                execute(method);
                try
                {
                    setUserName(fileName, urlStr);
                }
                catch (IOException e)
                {

                }
            }
            ((DefaultFileContent)this.file.getContent()).resetAttributes();
        }

        private void setUserName(URLFileName fileName, String urlStr)
            throws IOException
        {
            List list = new ArrayList();
            String name = builder.getCreatorName(getFileSystem().getFileSystemOptions());
            String userName = fileName.getUserName();
            if (name == null)
            {
                name = userName;
            }
            else
            {
                if (userName != null)
                {
                    String comment = "Modified by user " + userName;
                    list.add(new DefaultDavProperty(DeltaVConstants.COMMENT, comment));
                }
            }
            list.add(new DefaultDavProperty(DeltaVConstants.CREATOR_DISPLAYNAME, name));
            PropPatchMethod method = new PropPatchMethod(urlStr, list);
            setupMethod(method);
            execute(method);
        }

        private boolean createVersion(String urlStr)
        {
            try
            {
                VersionControlMethod method = new VersionControlMethod(urlStr);
                setupMethod(method);
                execute(method);
                return true;
            }
            catch (Exception ex)
            {
                return false;
            }
        }
    }
}