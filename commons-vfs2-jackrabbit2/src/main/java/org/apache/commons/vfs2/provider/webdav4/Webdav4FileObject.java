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
package org.apache.commons.vfs2.provider.webdav4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileContentInfoFactory;
import org.apache.commons.vfs2.FileNotFolderException;
import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.DefaultFileContent;
import org.apache.commons.vfs2.provider.GenericURLFileName;
import org.apache.commons.vfs2.provider.http4.Http4FileObject;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.commons.vfs2.util.MonitorOutputStream;
import org.apache.commons.vfs2.util.URIUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.BaseDavRequest;
import org.apache.jackrabbit.webdav.client.methods.HttpCheckin;
import org.apache.jackrabbit.webdav.client.methods.HttpCheckout;
import org.apache.jackrabbit.webdav.client.methods.HttpDelete;
import org.apache.jackrabbit.webdav.client.methods.HttpMkcol;
import org.apache.jackrabbit.webdav.client.methods.HttpMove;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.client.methods.HttpProppatch;
import org.apache.jackrabbit.webdav.client.methods.HttpVersionControl;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
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
 * @since 2.5.0
 */
public class Webdav4FileObject extends Http4FileObject<Webdav4FileSystem> {
    /**
     * An OutputStream that writes to a Webdav resource.
     * <p>
     * TODO - Use piped stream to avoid temporary file.
     */
    private class WebdavOutputStream extends MonitorOutputStream {
        private final Webdav4FileObject file;

        public WebdavOutputStream(final Webdav4FileObject file) {
            super(new ByteArrayOutputStream());
            this.file = file;
        }

        private boolean createVersion(final String urlStr) {
            try {
                final HttpVersionControl request = new HttpVersionControl(urlStr);
                setupRequest(request);
                executeRequest(request);
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
            final HttpEntity entity = new ByteArrayEntity(((ByteArrayOutputStream) out).toByteArray());
            final GenericURLFileName fileName = (GenericURLFileName) getName();
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
                        final HttpCheckout request = new HttpCheckout(urlStr);
                        setupRequest(request);
                        executeRequest(request);
                        isCheckedIn = false;
                    } catch (final FileSystemException ex) {
                        // Ignore the exception checking out.
                    }
                }

                try {
                    final HttpPut request = new HttpPut(urlStr);
                    request.setEntity(entity);
                    setupRequest(request);
                    executeRequest(request);
                    setUserName(fileName, urlStr);
                } catch (final FileSystemException ex) {
                    if (!isCheckedIn) {
                        try {
                            final HttpCheckin request = new HttpCheckin(urlStr);
                            setupRequest(request);
                            executeRequest(request);
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
                    final HttpCheckin request = new HttpCheckin(urlStr);
                    setupRequest(request);
                    executeRequest(request);
                }
            } else {
                final HttpPut request = new HttpPut(urlStr);
                request.setEntity(entity);
                setupRequest(request);
                executeRequest(request);
                try {
                    setUserName(fileName, urlStr);
                } catch (final IOException e) {
                    // Ignore the exception if unable to set the user name.
                }
            }
            ((DefaultFileContent) this.file.getContent()).resetAttributes();
        }

        private void setUserName(final GenericURLFileName fileName, final String urlStr) throws IOException {
            final DavPropertySet setProperties = new DavPropertySet();
            final DavPropertyNameSet removeProperties = new DavPropertyNameSet();
            String name = builder.getCreatorName(getFileSystem().getFileSystemOptions());
            final String userName = fileName.getUserName();
            if (name == null) {
                name = userName;
            } else {
                if (userName != null) {
                    final String comment = "Modified by user " + userName;
                    setProperties.add(new DefaultDavProperty(DeltaVConstants.COMMENT, comment));
                }
            }
            setProperties.add(new DefaultDavProperty(DeltaVConstants.CREATOR_DISPLAYNAME, name));
            final HttpProppatch request = new HttpProppatch(urlStr, setProperties, removeProperties);
            setupRequest(request);
            executeRequest(request);
        }
    }

    /** The character set property name. */
    public static final DavPropertyName RESPONSE_CHARSET = DavPropertyName.create("response-charset");

    /** The FileSystemConfigBuilder */
    private final Webdav4FileSystemConfigBuilder builder;

    private final Webdav4FileSystem fileSystem;

    protected Webdav4FileObject(final AbstractFileName name, final Webdav4FileSystem fileSystem)
            throws FileSystemException, URISyntaxException {
        this(name, fileSystem, Webdav4FileSystemConfigBuilder.getInstance());
    }

    protected Webdav4FileObject(final AbstractFileName name, final Webdav4FileSystem fileSystem,
            final Webdav4FileSystemConfigBuilder builder) throws FileSystemException, URISyntaxException {
        super(name, fileSystem, builder);
        this.fileSystem = fileSystem;
        this.builder = builder;
    }

    /**
     * Creates this file as a folder.
     */
    @Override
    protected void doCreateFolder() throws Exception {
        final HttpMkcol request = new HttpMkcol(toUrlString((GenericURLFileName) getName()));
        setupRequest(request);
        try {
            executeRequest(request);
        } catch (final FileSystemException fse) {
            throw new FileSystemException("vfs.provider.webdav/create-collection.error", getName(), fse);
        }
    }

    /**
     * Deletes the file.
     */
    @Override
    protected void doDelete() throws Exception {
        final HttpDelete request = new HttpDelete(toUrlString((GenericURLFileName) getName()));
        setupRequest(request);
        executeRequest(request);
    }

    /**
     * Returns the properties of the Webdav resource.
     */
    @Override
    protected Map<String, Object> doGetAttributes() throws Exception {
        final Map<String, Object> attributes = new HashMap<>();
        try {
            final GenericURLFileName fileName = (GenericURLFileName) getName();
            DavPropertySet properties = getProperties(fileName, DavConstants.PROPFIND_ALL_PROP,
                    new DavPropertyNameSet(), false);
            final DavPropertyIterator iter = properties.iterator();
            while (iter.hasNext()) {
                final DavProperty property = iter.nextProperty();
                attributes.put(property.getName().toString(), property.getValue());
            }
            properties = getPropertyNames(fileName);
            final DavPropertyIterator iter2 = properties.iterator();
            while (iter2.hasNext()) {
                DavProperty property = iter2.nextProperty();
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
        final DavProperty property = getProperty((GenericURLFileName) getName(), DavConstants.PROPERTY_GETCONTENTLENGTH);
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
        final DavProperty property = getProperty((GenericURLFileName) getName(), DavConstants.PROPERTY_GETLASTMODIFIED);
        if (property != null) {
            final String value = (String) property.getValue();
            return DateUtils.parseDate(value).getTime();
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
            return isDirectory((GenericURLFileName) getName()) ? FileType.FOLDER : FileType.FILE;
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
        HttpPropfind request = null;
        try {
            final GenericURLFileName name = (GenericURLFileName) getName();
            if (isDirectory(name)) {
                final DavPropertyNameSet nameSet = new DavPropertyNameSet();
                nameSet.add(DavPropertyName.create(DavConstants.PROPERTY_DISPLAYNAME));

                request = new HttpPropfind(toUrlString(name), nameSet, DavConstants.DEPTH_1);

                final HttpResponse res = executeRequest(request);
                final List<Webdav4FileObject> vfs = new ArrayList<>();
                if (request.succeeded(res)) {
                    final MultiStatusResponse[] responses = request.getResponseBodyAsMultiStatus(res).getResponses();

                    for (final MultiStatusResponse response : responses) {
                        if (isCurrentFile(response.getHref(), name)) {
                            continue;
                        }
                        final String resourceName = resourceName(response.getHref());
                        if (resourceName != null && resourceName.length() > 0) {
                            final Webdav4FileObject fo = (Webdav4FileObject) FileObjectUtils.getAbstractFileObject(
                                    getFileSystem().resolveFile(getFileSystem().getFileSystemManager()
                                            .resolveName(getName(), resourceName, NameScope.CHILD)));
                            vfs.add(fo);
                        }
                    }
                }
                return vfs.toArray(new Webdav4FileObject[vfs.size()]);
            }
            throw new FileNotFolderException(getName());
        } catch (final FileNotFolderException fnfe) {
            throw fnfe;
        } catch (final DavException e) {
            throw new FileSystemException(e.getMessage(), e);
        } catch (final IOException e) {
            throw new FileSystemException(e.getMessage(), e);
        } finally {
            if (request != null) {
                request.releaseConnection();
            }
        }
    }

    /**
     * Rename the file.
     */
    @Override
    protected void doRename(final FileObject newFile) throws Exception {
        final String url = URIUtils.encodePath(toUrlString((GenericURLFileName) getName()));
        final String dest = toUrlString((GenericURLFileName) newFile.getName(), false);
        final HttpMove request = new HttpMove(url, dest, false);
        setupRequest(request);
        executeRequest(request);
    }

    /**
     * Sets an attribute of this file. Is only called if {@link #doGetType} does not return {@link FileType#IMAGINARY}.
     */
    @Override
    protected void doSetAttribute(final String attrName, final Object value) throws Exception {
        try {
            final GenericURLFileName fileName = (GenericURLFileName) getName();
            final String urlStr = toUrlString(fileName);
            final DavPropertySet properties = new DavPropertySet();
            final DavPropertyNameSet propertyNameSet = new DavPropertyNameSet();
            final DavProperty property = new DefaultDavProperty(attrName, value, Namespace.EMPTY_NAMESPACE);
            if (value != null) {
                properties.add(property);
            } else {
                propertyNameSet.add(property.getName()); // remove property
            }

            final HttpProppatch request = new HttpProppatch(urlStr, properties, propertyNameSet);
            setupRequest(request);
            final HttpResponse response = executeRequest(request);
            if (!request.succeeded(response)) {
                throw new FileSystemException("Property '" + attrName + "' could not be set.");
            }
        } catch (final FileSystemException fse) {
            throw fse;
        } catch (final Exception e) {
            throw new FileSystemException("vfs.provider.webdav/set-attributes", e, getName(), attrName);
        }
    }

    private HttpResponse executeRequest(final HttpUriRequest request) throws FileSystemException {
        HttpResponse response = null;

        try {
            response = executeHttpUriRequest(request);
            final int status = response.getStatusLine().getStatusCode();
            if (status == HttpURLConnection.HTTP_NOT_FOUND || status == HttpURLConnection.HTTP_GONE) {
                throw new FileNotFoundException(request.getURI());
            }

            if (request instanceof BaseDavRequest) {
                ((BaseDavRequest) request).checkSuccess(response);
            }

            return response;
        } catch (final FileSystemException fse) {
            throw fse;
        } catch (final IOException e) {
            throw new FileSystemException(e);
        } catch (final DavException e) {
            throw ExceptionConverter.generate(e);
        } finally {
            if (request instanceof HttpRequestBase) {
                ((HttpRequestBase) request).releaseConnection();
            }
        }
    }

    @Override
    protected FileContentInfoFactory getFileContentInfoFactory() {
        return new Webdav4FileContentInfoFactory();
    }

    DavPropertySet getProperties(final GenericURLFileName name) throws FileSystemException {
        return getProperties(name, DavConstants.PROPFIND_ALL_PROP, new DavPropertyNameSet(), false);
    }

    DavPropertySet getProperties(final GenericURLFileName name, final DavPropertyNameSet nameSet, final boolean addEncoding)
            throws FileSystemException {
        return getProperties(name, DavConstants.PROPFIND_BY_PROPERTY, nameSet, addEncoding);
    }

    DavPropertySet getProperties(final GenericURLFileName name, final int type, final DavPropertyNameSet nameSet,
            final boolean addEncoding) throws FileSystemException {
        try {
            final String urlStr = toUrlString(name);
            final HttpPropfind request = new HttpPropfind(urlStr, type, nameSet, DavConstants.DEPTH_0);
            setupRequest(request);
            final HttpResponse res = executeRequest(request);
            if (request.succeeded(res)) {
                final MultiStatus multiStatus = request.getResponseBodyAsMultiStatus(res);
                final MultiStatusResponse response = multiStatus.getResponses()[0];
                final DavPropertySet props = response.getProperties(HttpStatus.SC_OK);
                if (addEncoding) {
                    final ContentType resContentType = ContentType.getOrDefault(res.getEntity());
                    final DavProperty prop = new DefaultDavProperty(RESPONSE_CHARSET,
                            resContentType.getCharset().name());
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

    DavProperty getProperty(final GenericURLFileName fileName, final DavPropertyName name) throws FileSystemException {
        final DavPropertyNameSet nameSet = new DavPropertyNameSet();
        nameSet.add(name);
        final DavPropertySet propertySet = getProperties(fileName, nameSet, false);
        return propertySet.get(name);
    }

    DavProperty getProperty(final GenericURLFileName fileName, final String property) throws FileSystemException {
        return getProperty(fileName, DavPropertyName.create(property));
    }

    DavPropertySet getPropertyNames(final GenericURLFileName name) throws FileSystemException {
        return getProperties(name, DavConstants.PROPFIND_PROPERTY_NAMES, new DavPropertyNameSet(), false);
    }

    /**
     * Convert the FileName to an encoded url String.
     *
     * @param name The FileName.
     * @return The encoded URL String.
     */
    private String hrefString(final GenericURLFileName name) {
        final GenericURLFileName newFile = new GenericURLFileName("http", name.getHostName(), name.getPort(), name.getDefaultPort(),
                null, null, name.getPath(), name.getType(), name.getQueryString());
        try {
            return newFile.getURIEncoded(this.getUrlCharset());
        } catch (final Exception e) {
            return name.getURI();
        }
    }

    private boolean isCurrentFile(final String href, final GenericURLFileName fileName) {
        String name = hrefString(fileName);
        if (href.endsWith("/") && !name.endsWith("/")) {
            name += "/";
        }
        return href.equals(name) || href.equals(fileName.getPath());
    }

    private boolean isDirectory(final GenericURLFileName name) throws IOException {
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

    private void setupRequest(final HttpUriRequest request) throws FileSystemException {
        // NOTE: *FileSystemConfigBuilder takes care of redirect option and user agent.
        request.addHeader("Cache-control", "no-cache");
        request.addHeader("Cache-store", "no-store");
        request.addHeader("Pragma", "no-cache");
        request.addHeader("Expires", "0");
    }

    private String toUrlString(final GenericURLFileName name) {
        return toUrlString(name, true);
    }

    /**
     * Converts the given URLFileName to an encoded URL String.
     *
     * @param name The FileName.
     * @param includeUserInfo true if user information should be included.
     * @return The encoded URL String.
     */
    private String toUrlString(final GenericURLFileName name, final boolean includeUserInfo) {
        String user = null;
        String password = null;
        if (includeUserInfo) {
            user = name.getUserName();
            password = name.getPassword();
        }
        final GenericURLFileName newFile = new GenericURLFileName("http", name.getHostName(), name.getPort(), name.getDefaultPort(),
                user, password, name.getPath(), name.getType(), name.getQueryString());
        try {
            return newFile.getURIEncoded(this.getUrlCharset());
        } catch (final Exception e) {
            return name.getURI();
        }
    }
}
