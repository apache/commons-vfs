package org.apache.commons.vfs.provider;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;

public class URLFileName extends GenericFileName
{
    private final String queryString;

    public URLFileName(final String rootUri,
                       final String scheme,
                       final String hostName,
                       final int port,
                       final int defaultPort,
                       final String userName,
                       final String password,
                       final String path,
                       final String queryString)
    {
        super(rootUri, scheme, hostName, port, defaultPort, userName, password, path);
        this.queryString = queryString;
    }

    /**
     * get the query string
     * @return
     */
    public String getQueryString()
    {
        return queryString;
    }

    /**
     * get the path and query string e.g. /path/servlet?param1=true
     * @return
     */
    public String getPathQuery()
    {
        StringBuffer sb = new StringBuffer(250);
        sb.append(getPath());
        sb.append("?");
        sb.append(getQueryString());

        return sb.toString();
    }

    /**
     * get the path encoded suitable for url like filesystem e.g. (http, webdav)
     */
    public String getPathQueryEncoded() throws URIException, FileSystemException
    {
        if (getQueryString() == null)
        {
            return URIUtil.encodePath(getPathDecoded());
        }

        StringBuffer sb = new StringBuffer(250);
        sb.append(URIUtil.encodePath(getPathDecoded()));
        sb.append("?");
        sb.append(getQueryString());
        return sb.toString();
    }

    public FileName createName(final String rootURI, final String absPath)
    {
        return new URLFileName(getRootURI(),
            getScheme(),
            getHostName(),
            getPort(),
            getDefaultPort(),
            getUserName(),
            getPassword(),
            absPath,
            getQueryString());
    }

    /**
     * append query string to the uri
     * @return
     */
    protected String createURI()
    {
        if (getQueryString() != null)
        {
            StringBuffer sb = new StringBuffer(250);
            sb.append(super.createURI());
            sb.append("?");
            sb.append(getQueryString());

            return sb.toString();
        }

        return super.createURI();
    }
}
