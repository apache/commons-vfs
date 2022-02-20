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
package org.apache.commons.vfs2.provider.ftps;

import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.ftpserver.ssl.ClientAuth;
import org.apache.ftpserver.ssl.SslConfiguration;

/**
 * see https://issues.apache.org/jira/browse/FTPSERVER-491
 */
public class NoProtocolSslConfigurationProxy implements SslConfiguration {

    @Override
    public String getEnabledProtocol() {
        return null;
    }

    private final SslConfiguration sslConfiguration;

    public NoProtocolSslConfigurationProxy(SslConfiguration sslConfiguration) {
        this.sslConfiguration = sslConfiguration;
    }

    @Override
    public SSLSocketFactory getSocketFactory() throws GeneralSecurityException {
        return this.sslConfiguration.getSocketFactory();
    }

    @Override
    public SSLContext getSSLContext() throws GeneralSecurityException {
        return this.sslConfiguration.getSSLContext();
    }

    @Override
    public SSLContext getSSLContext(String protocol) throws GeneralSecurityException {
        return this.sslConfiguration.getSSLContext(protocol);
    }

    @Override
    public String[] getEnabledCipherSuites() {
        return this.sslConfiguration.getEnabledCipherSuites();
    }

    @Override
    public ClientAuth getClientAuth() {
        return this.sslConfiguration.getClientAuth();
    }

}
