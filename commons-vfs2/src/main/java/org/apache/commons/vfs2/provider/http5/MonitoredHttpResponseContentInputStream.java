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
package org.apache.commons.vfs2.provider.http5;

import java.io.IOException;

import org.apache.commons.vfs2.util.MonitorInputStream;
import org.apache.hc.core5.http.ClassicHttpResponse;

/**
 * An InputStream that cleans up the {@code org.apache.hc.core5.http.ClassicHttpResponse} on close.
 */
class MonitoredHttpResponseContentInputStream extends MonitorInputStream {

    private final ClassicHttpResponse httpResponse;

    public MonitoredHttpResponseContentInputStream(final ClassicHttpResponse httpResponse) throws IOException {
        super(httpResponse.getEntity().getContent());
        this.httpResponse = httpResponse;
    }

    public MonitoredHttpResponseContentInputStream(final ClassicHttpResponse httpResponse, final int bufferSize) throws IOException {
        super(httpResponse.getEntity().getContent(), bufferSize);
        this.httpResponse = httpResponse;
    }

    @Override
    protected void onClose() throws IOException {
        httpResponse.close();
    }

}
