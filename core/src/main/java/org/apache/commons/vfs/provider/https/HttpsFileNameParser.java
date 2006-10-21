package org.apache.commons.vfs.provider.https;

import org.apache.commons.vfs.provider.URLFileNameParser;
import org.apache.commons.vfs.provider.FileNameParser;
import org.apache.commons.vfs.provider.http.HttpFileNameParser;

/**
 * Implementation for http. set default port to 80
 */
public class HttpsFileNameParser extends URLFileNameParser
{
    private final static HttpsFileNameParser INSTANCE = new HttpsFileNameParser();

    public HttpsFileNameParser()
    {
        super(443);
    }

    public static FileNameParser getInstance()
    {
        return INSTANCE;
    }
}
