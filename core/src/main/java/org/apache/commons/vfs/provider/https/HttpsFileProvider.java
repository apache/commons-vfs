package org.apache.commons.vfs.provider.https;

import org.apache.commons.vfs.provider.http.HttpFileProvider;

/**
 * An HTTPS provider that uses commons-httpclient.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class HttpsFileProvider
    extends HttpFileProvider
{
	public HttpsFileProvider()
    {
        super();
        setFileNameParser(HttpsFileNameParser.getInstance());
    }
}
