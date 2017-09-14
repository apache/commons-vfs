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
package org.apache.commons.vfs2.provider.webdav;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.vfs2.FileContentInfoFactory;
import org.apache.commons.vfs2.FileNotFolderException;
import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.DefaultFileContent;
import org.apache.commons.vfs2.provider.URLFileName;
import org.apache.commons.vfs2.provider.http.HttpFileObject;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.commons.vfs2.util.MonitorOutputStream;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.CheckinMethod;
import org.apache.jackrabbit.webdav.client.methods.CheckoutMethod;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.client.methods.UncheckoutMethod;
import org.apache.jackrabbit.webdav.client.methods.VersionControlMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;
import org.apache.jackrabbit.webdav.version.VersionControlledResource;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Node;

/**
 * A WebDAV file.
 *
 * @since 2.0
 */
public class WebdavFileObject extends HttpFileObject<WebdavFileSystem> {
    /**
     * An OutputStream that writes to a Webdav resource.
     * <p>
     * TODO - Use piped stream to avoid temporary file.
     */
    private class WebdavOutputStream extends MonitorOutputStream {
        private final WebdavFileObject file;

        public WebdavOutputStream(final WebdavFileObject file) {
            super(new ByteArrayOutputStream());
            this.file = file;
        }

        private boolean createVersion(final String urlStr) {
            try {
                final VersionControlMethod method = new VersionControlMethod(urlStr);
                setupMethod(method);
                execute(method);
                return true;
            } catch (final Exception ex) {
                return false;
            }
        }

        /**
         * Called after this stream is closed.
         */
        @Override
        protected void onClose() throws IOException {
            final RequestEntity entity = new ByteArrayRequestEntity(((ByteArrayOutputStream) out).toByteArray());
            final URLFileName fileName = (URLFileName) getName();
            final String urlStr = toUrlString(fileName);
            if (builder.isVersioning(getFileSystem().getFileSystemOptions())) {
                DavPropertySet set = null;
                boolean fileExists = true;
                boolean isCheckedIn = true;
                try {
                    set = getPropertyNames(fileName);
                } catch (final FileNotFoundException fnfe) {
                    fileExists = false;
                }
                if (fileExists && set != null) {
                    if (set.contains(VersionControlledResource.CHECKED_OUT)) {
                        isCheckedIn = false;
                    } else if (!set.contains(VersionControlledResource.CHECKED_IN)) {
                        DavProperty prop = set.get(VersionControlledResource.AUTO_VERSION);
                        if (prop != null) {
                            prop = getProperty(fileName, VersionControlledResource.AUTO_VERSION);
                            if (DeltaVConstants.XML_CHECKOUT_CHECKIN.equals(prop.getValue())) {
                                createVersion(urlStr);
                            }
                        }
                    }
                }
                if (fileExists && isCheckedIn) {
                    try {
                        final CheckoutMethod checkout = new CheckoutMethod(urlStr);
                        setupMethod(checkout);
                        execute(checkout);
                        isCheckedIn = false;
                    } catch (final FileSystemException ex) {
                        // Ignore the exception checking out.
                    }
                }

                try {
                    final PutMethod method = new PutMethod(urlStr);
                    method.setRequestEntity(entity);
                    setupMethod(method);
                    execute(method);
                    setUserName(fileName, urlStr);
                } catch (final FileSystemException ex) {
                    if (!isCheckedIn) {
                        try {
                            final UncheckoutMethod method = new UncheckoutMethod(urlStr);
                            setupMethod(method);
                            execute(method);
                            isCheckedIn = true;
                        } catch (final Exception e) {
                            // Ignore the exception. Going to throw original.
                        }
                        throw ex;
                    }
                }
                if (!fileExists) {
                    createVersion(urlStr);
                    try {
                        final DavPropertySet props = getPropertyNames(fileName);
                        isCheckedIn = !props.contains(VersionControlledResource.CHECKED_OUT);
                    } catch (final FileNotFoundException fnfe) {
                        // Ignore the error
                    }
                }
                if (!isCheckedIn) {
                    final CheckinMethod checkin = new CheckinMethod(urlStr);
                    setupMethod(checkin);
                    execute(checkin);
                }
            } else {
                final PutMethod method = new PutMethod(urlStr);
                method.setRequestEntity(entity);
                setupMethod(method);
                execute(method);
                try {
                    setUserName(fileName, urlStr);
                } catch (final IOException e) {
                    // Ignore the exception if unable to set the user name.
                }
            }
            ((DefaultFileContent) this.file.getContent()).resetAttributes();
        }

        private void setUserName(final URLFileName fileName, final String urlStr) throws IOException {
            final List<DefaultDavProperty> list = new ArrayList<>();
            String name = builder.getCreatorName(getFileSystem().getFileSystemOptions());
            final String userName = fileName.getUserName();
            if (name == null) {
                name = userName;
            } else {
                if (userName != null) {
                    final String comment = "Modified by user " + userName;
                    list.add(new DefaultDavProperty(DeltaVConstants.COMMENT, comment));
                }
            }
            list.add(new DefaultDavProperty(DeltaVConstants.CREATOR_DISPLAYNAME, name));
            final PropPatchMethod method = new PropPatchMethod(urlStr, list);
            setupMethod(method);
            execute(method);
        }
    }

    /** The character set property name. */
    public static final DavPropertyName RESPONSE_CHARSET = DavPropertyName.create("response-charset");

    /** The FileSystemConfigBuilder */
    private final WebdavFileSystemConfigBuilder builder;

    private final WebdavFileSystem fileSystem;

    protected WebdavFileObject(final AbstractFileName name, final WebdavFileSystem fileSystem) {
        super(name, fileSystem, WebdavFileSystemConfigBuilder.getInstance());
        this.fileSystem = fileSystem;
        builder = (WebdavFileSystemConfigBuilder) WebdavFileSystemConfigBuilder.getInstance();
    }

    protected void configureMethod(final HttpMethodBase httpMethod) {
        httpMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, WebdavMethodRetryHandler.getInstance());
    }

    /**
     * Creates this file as a folder.
     */
    @Override
    protected void doCreateFolder() throws Exception {
        final DavMethod method = new MkColMethod(toUrlString((URLFileName) getName()));
        setupMethod(method);
        try {
            execute(method);
        } catch (final FileSystemException fse) {
            throw new FileSystemException("vfs.provider.webdav/create-collection.error", getName(), fse);
        }
    }

    /**
     * Deletes the file.
     */
    @Override
    protected void doDelete() throws Exception {
        final DavMethod method = new DeleteMethod(toUrlString((URLFileName) getName()));
        setupMethod(method);
        execute(method);
    }

    /**
     * Returns the properties of the Webdav resource.
     */
    @Override
    protected Map<String, Object> doGetAttributes() throws Exception {
        final Map<String, Object> attributes = new HashMap<>();
        try {
            final URLFileName fileName = (URLFileName) getName();
            DavPropertySet properties = getProperties(fileName, DavConstants.PROPFIND_ALL_PROP,
                    new DavPropertyNameSet(), false);
            @SuppressWarnings("unchecked") // iterator() is documented to return DavProperty instances
            final Iterator<DavProperty> iter = properties.iterator();
            while (iter.hasNext()) {
                final DavProperty property = iter.next();
                attributes.put(property.getName().toString(), property.getValue());
            }
            properties = getPropertyNames(fileName);
            @SuppressWarnings("unchecked") // iterator() is documented to return DavProperty instances
            final Iterator<DavProperty> iter2 = properties.iterator();
            while (iter2.hasNext()) {
                DavProperty property = iter2.next();
                if (!attributes.containsKey(property.getName().getName())) {
                    property = getProperty(fileName, property.getName());
                    if (property != null) {
                        final Object name = property.getName();
                        final Object value = property.getValue();
                        if (name != null && value != null) {
                            attributes.put(name.toString(), value);
                        }
                    }
                }
            }
            return attributes;
        } catch (final Exception e) {
            throw new FileSystemException("vfs.provider.webdav/get-attributes.error", getName(), e);
        }
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    @Override
    protected long doGetContentSize() throws Exception {
        final DavProperty property = getProperty((URLFileName) getName(), DavConstants.PROPERTY_GETCONTENTLENGTH);
        if (property != null) {
            final String value = (String) property.getValue();
            return Long.parseLong(value);
        }
        return 0;
    }

    /**
     * Returns the last modified time of this file. Is only called if {@link #doGetType} does not return
     * {@link FileType#IMAGINARY}.
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        final DavProperty property = getProperty((URLFileName) getName(), DavConstants.PROPERTY_GETLASTMODIFIED);
        if (property != null) {
            final String value = (String) property.getValue();
            return DateUtil.parseDate(value).getTime();
        }
        return 0;
    }

    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception {
        return new WebdavOutputStream(this);
    }

    /**
     * Determines the type of this file. Must not return null. The return value of this method is cached, so the
     * implementation can be expensive.
     */
    @Override
    protected FileType doGetType() throws Exception {
        try {
            return isDirectory((URLFileName) getName()) ? FileType.FOLDER : FileType.FILE;
        } catch (final FileNotFolderException fnfe) {
            return FileType.IMAGINARY;
        } catch (final FileNotFoundException fnfe) {
            return FileType.IMAGINARY;
        }

    }

    /**
     * Determines if this file can be written to. Is only called if {@link #doGetType} does not return
     * {@link FileType#IMAGINARY}.
     * <p>
     * This implementation always returns true.
     *
     * @return true if the file is writable.
     * @throws Exception if an error occurs.
     */
    @Override
    protected boolean doIsWriteable() throws Exception {
        return true;
    }

    /**
     * Lists the children of the file.
     */
    @Override
    protected String[] doListChildren() throws Exception {
        // use doListChildrenResolved for performance
        return null;
    }

    /**
     * Lists the children of the file.
     */
    @Override
    protected FileObject[] doListChildrenResolved() throws Exception {
        PropFindMethod method = null;
        try {
            final URLFileName name = (URLFileName) getName();
            if (isDirectory(name)) {
                final DavPropertyNameSet nameSet = new DavPropertyNameSet();
                nameSet.add(DavPropertyName.create(DavConstants.PROPERTY_DISPLAYNAME));

                method = new PropFindMethod(toUrlString(name), nameSet, DavConstants.DEPTH_1);

                execute(method);
                final List<WebdavFileObject> vfs = new ArrayList<>();
                if (method.succeeded()) {
                    final MultiStatusResponse[] responses = method.getResponseBodyAsMultiStatus().getResponses();

                    for (final MultiStatusResponse response : responses) {
                        if (isCurrentFile(response.getHref(), name)) {
                            continue;
                        }
                        final String resourceName = resourceName(response.getHref());
                        if (resourceName != null && resourceName.length() > 0) {
                            final WebdavFileObject fo = (WebdavFileObject) FileObjectUtils.getAbstractFileObject(
                                    getFileSystem().resolveFile(getFileSystem().getFileSystemManager()
                                            .resolveName(getName(), resourceName, NameScope.CHILD)));
                            vfs.add(fo);
                        }
                    }
                }
                return vfs.toArray(new WebdavFileObject[vfs.size()]);
            }
            throw new FileNotFolderException(getName());
        } catch (final FileNotFolderException fnfe) {
            throw fnfe;
        } catch (final DavException e) {
            throw new FileSystemException(e.getMessage(), e);
        } catch (final IOException e) {
            throw new FileSystemException(e.getMessage(), e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    /**
     * Rename the file.
     */
    @Override
    protected void doRename(final FileObject newFile) throws Exception {
        final String url = encodePath(toUrlString((URLFileName) getName()));
        final String dest = toUrlString((URLFileName) newFile.getName(), false);
        final DavMethod method = new MoveMethod(url, dest, false);
        setupMethod(method);
        execute(method);
    }

    /**
     * Sets an attribute of this file. Is only called if {@link #doGetType} does not return {@link FileType#IMAGINARY}.
     */
    @Override
    protected void doSetAttribute(final String attrName, final Object value) throws Exception {
        try {
            final URLFileName fileName = (URLFileName) getName();
            final String urlStr = toUrlString(fileName);
            final DavPropertySet properties = new DavPropertySet();
            final DavPropertyNameSet propertyNameSet = new DavPropertyNameSet();
            final DavProperty property = new DefaultDavProperty(attrName, value, Namespace.EMPTY_NAMESPACE);
            if (value != null) {
                properties.add(property);
            } else {
                propertyNameSet.add(property.getName()); // remove property
            }

            final PropPatchMethod method = new PropPatchMethod(urlStr, properties, propertyNameSet);
            setupMethod(method);
            execute(method);
            if (!method.succeeded()) {
                throw new FileSystemException("Property '" + attrName + "' could not be set.");
            }
        } catch (final FileSystemException fse) {
            throw fse;
        } catch (final Exception e) {
            throw new FileSystemException("vfs.provider.webdav/set-attributes", e, getName(), attrName);
        }
    }

    /**
     * Execute a 'Workspace' operation.
     *
     * @param method The DavMethod to invoke.
     * @throws FileSystemException If an error occurs.
     */
    private void execute(final DavMethod method) throws FileSystemException {
        try {
            final int status = fileSystem.getClient().executeMethod(method);
            if (status == HttpURLConnection.HTTP_NOT_FOUND || status == HttpURLConnection.HTTP_GONE) {
                throw new FileNotFoundException(method.getURI());
            }
            method.checkSuccess();
        } catch (final FileSystemException fse) {
            throw fse;
        } catch (final IOException e) {
            throw new FileSystemException(e);
        } catch (final DavException e) {
            throw ExceptionConverter.generate(e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    @Override
    protected FileContentInfoFactory getFileContentInfoFactory() {
        return new WebdavFileContentInfoFactory();
    }

    DavPropertySet getProperties(final URLFileName name) throws FileSystemException {
        return getProperties(name, DavConstants.PROPFIND_ALL_PROP, new DavPropertyNameSet(), false);
    }

    DavPropertySet getProperties(final URLFileName name, final DavPropertyNameSet nameSet, final boolean addEncoding)
            throws FileSystemException {
        return getProperties(name, DavConstants.PROPFIND_BY_PROPERTY, nameSet, addEncoding);
    }

    DavPropertySet getProperties(final URLFileName name, final int type, final DavPropertyNameSet nameSet,
            final boolean addEncoding) throws FileSystemException {
        try {
            final String urlStr = toUrlString(name);
            final PropFindMethod method = new PropFindMethod(urlStr, type, nameSet, DavConstants.DEPTH_0);
            setupMethod(method);
            execute(method);
            if (method.succeeded()) {
                final MultiStatus multiStatus = method.getResponseBodyAsMultiStatus();
                final MultiStatusResponse response = multiStatus.getResponses()[0];
                final DavPropertySet props = response.getProperties(HttpStatus.SC_OK);
                if (addEncoding) {
                    final DavProperty prop = new DefaultDavProperty(RESPONSE_CHARSET, method.getResponseCharSet());
                    props.add(prop);
                }
                return props;
            }
            return new DavPropertySet();
        } catch (final FileSystemException fse) {
            throw fse;
        } catch (final Exception e) {
            throw new FileSystemException("vfs.provider.webdav/get-property.error", e, getName(), name, type,
                    nameSet.getContent(), addEncoding);
        }
    }

    DavProperty getProperty(final URLFileName fileName, final DavPropertyName name) throws FileSystemException {
        final DavPropertyNameSet nameSet = new DavPropertyNameSet();
        nameSet.add(name);
        final DavPropertySet propertySet = getProperties(fileName, nameSet, false);
        return propertySet.get(name);
    }

    DavProperty getProperty(final URLFileName fileName, final String property) throws FileSystemException {
        return getProperty(fileName, DavPropertyName.create(property));
    }

    DavPropertySet getPropertyNames(final URLFileName name) throws FileSystemException {
        return getProperties(name, DavConstants.PROPFIND_PROPERTY_NAMES, new DavPropertyNameSet(), false);
    }

    /**
     * Convert the FileName to an encoded url String.
     *
     * @param name The FileName.
     * @return The encoded URL String.
     */
    private String hrefString(final URLFileName name) {
        final URLFileName newFile = new URLFileName("http", name.getHostName(), name.getPort(), name.getDefaultPort(),
                null, null, name.getPath(), name.getType(), name.getQueryString());
        try {
            return newFile.getURIEncoded(this.getUrlCharset());
        } catch (final Exception e) {
            return name.getURI();
        }
    }

    private boolean isCurrentFile(final String href, final URLFileName fileName) {
        String name = hrefString(fileName);
        if (href.endsWith("/") && !name.endsWith("/")) {
            name += "/";
        }
        return href.equals(name) || href.equals(fileName.getPath());
    }

    private boolean isDirectory(final URLFileName name) throws IOException {
        try {
            final DavProperty property = getProperty(name, DavConstants.PROPERTY_RESOURCETYPE);
            Node node;
            if (property != null && (node = (Node) property.getValue()) != null) {
                return node.getLocalName().equals(DavConstants.XML_COLLECTION);
            }
            return false;
        } catch (final FileNotFoundException fse) {
            throw new FileNotFolderException(name);
        }
    }

    /**
     * Returns the resource name from the path.
     *
     * @param path the path to the file.
     * @return The resource name
     */
    private String resourceName(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        final int i = path.lastIndexOf("/");
        return i >= 0 ? path.substring(i + 1) : path;
    }

    /**
     * Prepares a Method object.
     *
     * @param method the HttpMethod.
     * @throws FileSystemException if an error occurs encoding the uri.
     * @throws URIException if the URI is in error.
     */
    @Override
    protected void setupMethod(final HttpMethod method) throws FileSystemException, URIException {
        final String pathEncoded = ((URLFileName) getName()).getPathQueryEncoded(this.getUrlCharset());
        method.setPath(pathEncoded);
        method.setFollowRedirects(this.getFollowRedirect());
        method.setRequestHeader("User-Agent", "Jakarta-Commons-VFS");
        method.addRequestHeader("Cache-control", "no-cache");
        method.addRequestHeader("Cache-store", "no-store");
        method.addRequestHeader("Pragma", "no-cache");
        method.addRequestHeader("Expires", "0");
    }

    private String toUrlString(final URLFileName name) {
        return toUrlString(name, true);
    }

    /**
     * Converts the given URLFileName to an encoded URL String.
     *
     * @param name The FileName.
     * @param includeUserInfo true if user information should be included.
     * @return The encoded URL String.
     */
    private String toUrlString(final URLFileName name, final boolean includeUserInfo) {
        String user = null;
        String password = null;
        if (includeUserInfo) {
            user = name.getUserName();
            password = name.getPassword();
        }
        final URLFileName newFile = new URLFileName("http", name.getHostName(), name.getPort(), name.getDefaultPort(),
                user, password, name.getPath(), name.getType(), name.getQueryString());
        try {
            return newFile.getURIEncoded(this.getUrlCharset());
        } catch (final Exception e) {
            return name.getURI();
        }
    }
}
