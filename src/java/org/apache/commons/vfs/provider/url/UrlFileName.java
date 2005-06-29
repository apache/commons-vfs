package org.apache.commons.vfs.provider.url;

import org.apache.commons.vfs.provider.URLFileName;
import org.apache.commons.vfs.provider.UriParser;

/**
 * Created by IntelliJ IDEA.
 * User: im
 * Date: 28.06.2005
 * Time: 16:00:19
 * To change this template use File | Settings | File Templates.
 */
public class UrlFileName extends URLFileName
{
    public UrlFileName(final String scheme, final String hostName, final int port, final int defaultPort, final String userName, final String password, final String path, final String queryString)
    {
        super(scheme, hostName, port, defaultPort, userName, password, path, queryString);
    }

    protected void appendRootUri(final StringBuffer buffer)
    {
        if (getHostName() != null && !"".equals(getHostName()))
        {
            super.appendRootUri(buffer);
            return;
        }

        buffer.append(getScheme());
        buffer.append(":");
    }
}
