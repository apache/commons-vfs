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
package org.apache.commons.vfs.provider.http;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import java.io.IOException;
import java.io.InputStream;

/**
 * A connection manager that provides access to a single HttpConnection.  This
 * manager makes no attempt to provide exclusive access to the contained
 * HttpConnection.
 * <p/>
 * imario@apache.org: Keep connection in ThreadLocal.
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @author <a href="mailto:becke@u.washington.edu">Michael Becke</a>
 * @author Eric Johnson
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author Laura Werner
 * @since 2.0
 */
public class ThreadLocalHttpConnectionManager implements HttpConnectionManager
{
    /**
     * The thread data.
     */
    protected ThreadLocal<Entry> localHttpConnection = new ThreadLocal<Entry>()
    {
        @Override
        protected Entry initialValue()
        {
            return new Entry();
        }
    };

    /**
     * Collection of parameters associated with this connection manager.
     */
    private HttpConnectionManagerParams params = new HttpConnectionManagerParams();


    public ThreadLocalHttpConnectionManager()
    {
    }

    /**
     * Since the same connection is about to be reused, make sure the
     * previous request was completely processed, and if not
     * consume it now.
     *
     * @param conn The connection
     */
    static void finishLastResponse(HttpConnection conn)
    {
        InputStream lastResponse = conn.getLastResponseInputStream();
        if (lastResponse != null)
        {
            conn.setLastResponseInputStream(null);
            try
            {
                lastResponse.close();
            }
            catch (IOException ioe)
            {
                //FIXME: badness - close to force reconnect.
                conn.close();
            }
        }
    }

    /**
     * release the connection of the current thread.
     */
    public void releaseLocalConnection()
    {
        if (getLocalHttpConnection() != null)
        {
            releaseConnection(getLocalHttpConnection());
        }
    }

    /**
     * A connection entry.
     */
    private static class Entry
    {
        /**
         * The http connection.
         */
        private HttpConnection conn = null;

        /**
         * The time the connection was made idle.
         */
        private long idleStartTime = Long.MAX_VALUE;

        private Entry()
        {
        }
    }

    protected HttpConnection getLocalHttpConnection()
    {
        return localHttpConnection.get().conn;
    }

    protected void setLocalHttpConnection(HttpConnection conn)
    {
        localHttpConnection.get().conn = conn;
    }

    protected long getIdleStartTime()
    {
        return localHttpConnection.get().idleStartTime;
    }

    protected void setIdleStartTime(long idleStartTime)
    {
        localHttpConnection.get().idleStartTime = idleStartTime;
    }

    /**
     * @param hostConfiguration The host configuration.
     * @return the HttpConnection.
     * @see HttpConnectionManager#getConnection(org.apache.commons.httpclient.HostConfiguration)
     */
    public HttpConnection getConnection(HostConfiguration hostConfiguration)
    {
        return getConnectionWithTimeout(hostConfiguration, 0);
    }

    /**
     * Gets the staleCheckingEnabled value to be set on HttpConnections that are created.
     *
     * @return <code>true</code> if stale checking will be enabled on HttpConections
     * @see HttpConnectionManagerParams #isStaleCheckingEnabled()
     */
    public boolean isConnectionStaleCheckingEnabled()
    {
        return this.params.isStaleCheckingEnabled();
    }

    /**
     * Sets the staleCheckingEnabled value to be set on HttpConnections that are created.
     *
     * @param connectionStaleCheckingEnabled <code>true</code> if stale checking will be enabled
     *                                       on HttpConections
     * @see HttpConnectionManagerParams#setStaleCheckingEnabled(boolean)
     */
    public void setConnectionStaleCheckingEnabled(boolean connectionStaleCheckingEnabled)
    {
        this.params.setStaleCheckingEnabled(connectionStaleCheckingEnabled);
    }

    /**
     * @param hostConfiguration The host configuration.
     * @param timeout The timeout value.
     * @return The HttpConnection.
     * @see HttpConnectionManager#getConnectionWithTimeout(HostConfiguration, long)
     * @since 3.0
     */
    public HttpConnection getConnectionWithTimeout(
        HostConfiguration hostConfiguration, long timeout)
    {

        HttpConnection httpConnection = getLocalHttpConnection();
        if (httpConnection == null)
        {
            httpConnection = new HttpConnection(hostConfiguration);
            setLocalHttpConnection(httpConnection);
            httpConnection.setHttpConnectionManager(this);
            httpConnection.getParams().setStaleCheckingEnabled(params.isStaleCheckingEnabled());
        }
        else
        {

            // make sure the host and proxy are correct for this connection
            // close it and set the values if they are not
            if (!hostConfiguration.hostEquals(httpConnection)
                || !hostConfiguration.proxyEquals(httpConnection))
            {

                if (httpConnection.isOpen())
                {
                    httpConnection.close();
                }

                httpConnection.setHost(hostConfiguration.getHost());
                httpConnection.setPort(hostConfiguration.getPort());
                httpConnection.setProtocol(hostConfiguration.getProtocol());
                httpConnection.setLocalAddress(hostConfiguration.getLocalAddress());

                httpConnection.setProxyHost(hostConfiguration.getProxyHost());
                httpConnection.setProxyPort(hostConfiguration.getProxyPort());
            }
            else
            {
                finishLastResponse(httpConnection);
            }
        }

        // remove the connection from the timeout handler
        setIdleStartTime(Long.MAX_VALUE);

        return httpConnection;
    }

    /**
     * @param hostConfiguration The host configuration.
     * @param timeout The timeout value.
     * @return The HttpConnection.
     * @see HttpConnectionManager#getConnection(HostConfiguration, long)
     * @deprecated Use #getConnectionWithTimeout(HostConfiguration, long)
     */
    @Deprecated
    public HttpConnection getConnection(
        HostConfiguration hostConfiguration, long timeout)
    {
        return getConnectionWithTimeout(hostConfiguration, timeout);
    }

    /**
     * @param conn The HttpConnection.
     * @see HttpConnectionManager#releaseConnection(org.apache.commons.httpclient.HttpConnection)
     */
    public void releaseConnection(HttpConnection conn)
    {
        if (conn != getLocalHttpConnection())
        {
            throw new IllegalStateException("Unexpected release of an unknown connection.");
        }

        finishLastResponse(getLocalHttpConnection());

        // track the time the connection was made idle
        setIdleStartTime(System.currentTimeMillis());
    }

    /**
     * @param idleTimeout The timeout value.
     * @since 3.0
     */
    public void closeIdleConnections(long idleTimeout)
    {
        long maxIdleTime = System.currentTimeMillis() - idleTimeout;
        if (getIdleStartTime() <= maxIdleTime)
        {
            getLocalHttpConnection().close();
        }
    }

    public HttpConnectionManagerParams getParams()
    {
        return this.params;
    }

    public void setParams(HttpConnectionManagerParams params)
    {
        if (params == null)
        {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        this.params = params;
    }
}
