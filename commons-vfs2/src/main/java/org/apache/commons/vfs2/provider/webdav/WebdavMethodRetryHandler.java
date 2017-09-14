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

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;

/**
 * A retry handler which will retry a failed webdav method one time.
 * <p>
 * Now that webdavlib didnt support adding a MethodRetryHandler only a few operations are restartable yet.
 *
 * @since 2.0
 */
public final class WebdavMethodRetryHandler implements HttpMethodRetryHandler {
    private static final WebdavMethodRetryHandler INSTANCE = new WebdavMethodRetryHandler();

    private WebdavMethodRetryHandler() {
    }

    public static WebdavMethodRetryHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean retryMethod(final HttpMethod method, final IOException exception, final int executionCount) {
        return executionCount < 2;
    }
}
