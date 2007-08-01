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
