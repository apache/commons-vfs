/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.vfs2.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.nio.DefaultServerIOEventDispatch;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BufferingHttpServiceHandler;
import org.apache.http.nio.protocol.EventListener;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.util.EntityUtils;

/**
 * Adapted from org.apache.http.examples.nio.NHttpServer.
 * 
 * <p>
 * Basic, yet fully functional and spec compliant, HTTP/1.1 server based on the non-blocking I/O model.
 * </p>
 * <p>
 * Please note the purpose of this application is demonstrate the usage of HttpCore APIs. It is NOT intended to demonstrate the most
 * efficient way of building an HTTP server.
 * </p>
 * 
 * @version $Id$
 * @since 2.1
 */
public class NHttpServer
{

    static class EventLogger implements EventListener
    {
        public void connectionClosed(final NHttpConnection conn)
        {
            debug("Connection closed: " + conn);
        }

        public void connectionOpen(final NHttpConnection conn)
        {
            debug("Connection open: " + conn);
        }

        public void connectionTimeout(final NHttpConnection conn)
        {
            debug("Connection timed out: " + conn);
        }

        public void fatalIOException(final IOException ex, final NHttpConnection conn)
        {
            debug("I/O error: " + ex.getMessage());
        }

        public void fatalProtocolException(final HttpException ex, final NHttpConnection conn)
        {
            debug("HTTP error: " + ex.getMessage());
        }
    }

    static class HttpFileHandler implements HttpRequestHandler
    {
        private final String docRoot;

        public HttpFileHandler(final String docRoot)
        {
            super();
            this.docRoot = docRoot;
        }

        public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context)
                throws HttpException, IOException
        {
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST"))
            {
                throw new MethodNotSupportedException(method + " method not supported");
            }

            if (request instanceof HttpEntityEnclosingRequest)
            {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                byte[] entityContent = EntityUtils.toByteArray(entity);
                debug("Incoming entity content (bytes): " + entityContent.length);
            }

            String target = request.getRequestLine().getUri();
            final File file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));
            if (!file.exists())
            {
                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                NStringEntity entity = new NStringEntity("<html><body><h1>File" + file.getPath()
                        + " not found</h1></body></html>", "UTF-8");
                entity.setContentType("text/html; charset=UTF-8");
                response.setEntity(entity);
                debug("File " + file.getPath() + " not found");

            } else if (!file.canRead())
            {
                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                NStringEntity entity = new NStringEntity("<html><body><h1>Access denied</h1></body></html>", "UTF-8");
                entity.setContentType("text/html; charset=UTF-8");
                response.setEntity(entity);
                debug("Cannot read file " + file.getPath());

            } else
            {
                response.setStatusCode(HttpStatus.SC_OK);
                NFileEntity body = new NFileEntity(file, "text/html");
                response.setEntity(body);
                if (!response.containsHeader(HttpHeaders.LAST_MODIFIED))
                {
                    response.addHeader(HttpHeaders.LAST_MODIFIED, DateUtil.formatDate(new Date(file.lastModified())));
                }
                debug("Serving file " + file.getPath());
            }
        }
    }

    static final boolean Debug = false;

    private static void debug(String s)
    {
        if (Debug)
        {
            System.out.println(s);
        }
    }

    public static void main(String[] args) throws Exception
    {
        new NHttpServer().run(0, null, 0);
    }

    public volatile ListeningIOReactor ioReactor;

    public boolean run(final int port, final String docRoot, long waitMillis) throws IOReactorException,
            InterruptedException
    {
        Executors.newSingleThreadExecutor().execute(new Runnable()
        {
            public void run()
            {
                try
                {
                    runBlock(port, docRoot);
                } catch (IOReactorException e)
                {
                    throw new IllegalStateException(e);
                }
            }
        });
        return waitForServerStartup(port, waitMillis);
    }

    private void runBlock(final int port, final String docRoot) throws IOReactorException
    {
        HttpParams params = new SyncBasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

        HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[]
        { new ResponseDate(), new ResponseServer(), new ResponseContent(), new ResponseConnControl() });

        BufferingHttpServiceHandler handler = new BufferingHttpServiceHandler(httpproc,
                new DefaultHttpResponseFactory(), new DefaultConnectionReuseStrategy(), params);

        // Set up request handlers
        HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
        reqistry.register("*", new HttpFileHandler(docRoot));

        handler.setHandlerResolver(reqistry);

        // Provide an event logger
        handler.setEventListener(new EventLogger());

        IOEventDispatch ioEventDispatch = new DefaultServerIOEventDispatch(handler, params);
        ioReactor = new DefaultListeningIOReactor(2, params);
        try
        {
            ioReactor.listen(new InetSocketAddress(port));
            ioReactor.execute(ioEventDispatch);
        } catch (InterruptedIOException ex)
        {
            debug("Interrupted");
        } catch (IOException e)
        {
            debug("I/O error: " + e.getMessage());
        }
        debug("Shutdown");
    }

    public void stop() throws IOException
    {
        if (this.ioReactor != null)
        {
            this.ioReactor.shutdown(2000);
        }

    }

    /**
     * Waits {@code waitMillis} for the server to start on the given {@code port}
     * 
     * @param port
     *            The port the server is running on
     * @param waitMillis
     *            How long to wail in milliseconds.
     * @throws InterruptedException
     *             If waiting is interrupted
     */
    private boolean waitForServerStartup(final int port, long waitMillis) throws InterruptedException
    {
        final long endWait = System.currentTimeMillis() + waitMillis;
        final String urlSpec = "http://localhost:" + port;
        try
        {
            URL url = new URL(urlSpec);
            InputStream inputStream = null;
            while (System.currentTimeMillis() < endWait && inputStream == null)
            {
                try
                {
                    inputStream = url.openStream();
                    if (inputStream != null)
                    {
                        IOUtils.closeQuietly(inputStream);
                        return true;
                    }
                } catch (IOException e)
                {
                    // ignore
                    // System.out.println("While waiting: " + e);
                    // e.printStackTrace();
                }
                Thread.sleep(100);
            }
        } catch (MalformedURLException e)
        {
            throw new IllegalStateException("Error in test code for URL " + urlSpec);
        }
        return false;
    }
}
