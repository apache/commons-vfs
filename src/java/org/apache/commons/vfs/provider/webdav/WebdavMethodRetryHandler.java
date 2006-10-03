package org.apache.commons.vfs.provider.webdav;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.MethodRetryHandler;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpRecoverableException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.UserAuthenticator;
import org.apache.commons.vfs.UserAuthenticationData;
import org.apache.commons.vfs.util.UserAuthenticatorUtils;
import org.apache.webdav.lib.WebdavResource;

import java.io.IOException;

/**
 * A retry handler which will retry a failed webdav method one time.<br />
 * Now that webdavlib didnt support adding a MethodRetryHandler only a few operations are restartable yet.
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class WebdavMethodRetryHandler implements MethodRetryHandler
{
    private final static WebdavMethodRetryHandler INSTANCE = new WebdavMethodRetryHandler();

    private WebdavMethodRetryHandler()
    {
    }

    public static WebdavMethodRetryHandler getInstance()
    {
        return INSTANCE;
    }

    public boolean retryMethod(HttpMethod method, HttpConnection connection, HttpRecoverableException recoverableException, int executionCount, boolean requestSent)
    {
        return executionCount < 2;
    }
}
