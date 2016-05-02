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
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.SSLNHttpServerConnectionFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.nio.protocol.HttpAsyncRequestHandlerRegistry;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

/**
 * Adapted from org.apache.http.examples.nio.NHttpServer.
 *
 * <p>
 * HTTP/1.1 file server based on the non-blocking I/O model and capable of direct channel (zero copy) data transfer.
 * </p>
 * <p>
 * Please note the purpose of this application is demonstrate the usage of HttpCore APIs. It is NOT intended to
 * demonstrate the most efficient way of building an HTTP server.
 * </p>
 *
 * @version $Id$
 * @since 2.1
 */
public class NHttpServer
{

    static class HttpFileHandler implements HttpAsyncRequestHandler<HttpRequest>
    {

        private final File docRoot;

        public HttpFileHandler(final File docRoot)
        {
            super();
            this.docRoot = docRoot;
        }

        @Override
        public void handle(final HttpRequest request, final HttpAsyncExchange httpexchange, final HttpContext context)
                throws HttpException, IOException
        {
            final HttpResponse response = httpexchange.getResponse();
            this.handleInternal(request, response, context);
            httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
        }

        private void handleInternal(final HttpRequest request, final HttpResponse response, final HttpContext context)
                throws HttpException, IOException
        {

            final String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST"))
            {
                throw new MethodNotSupportedException(method + " method not supported");
            }

            final String target = request.getRequestLine().getUri();
            final File file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));
            if (!file.exists())
            {

                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                final NStringEntity entity = new NStringEntity("<html><body><h1>File" + file.getPath()
                        + " not found</h1></body></html>", ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
                NHttpServer.debug("File " + file.getPath() + " not found");

            } else if (!file.canRead())
            {

                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                final NStringEntity entity = new NStringEntity("<html><body><h1>Access denied</h1></body></html>",
                        ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
                NHttpServer.debug("Cannot read file " + file.getPath());

            } else
            {
                final NHttpConnection conn = (NHttpConnection) context.getAttribute(ExecutionContext.HTTP_CONNECTION);
                response.setStatusCode(HttpStatus.SC_OK);
                final NFileEntity body = new NFileEntity(file, ContentType.create("text/html"));
                response.setEntity(body);
                if (!response.containsHeader(HttpHeaders.LAST_MODIFIED))
                {
                    response.addHeader(HttpHeaders.LAST_MODIFIED, DateUtil.formatDate(new Date(file.lastModified())));
                }
                NHttpServer.debug(conn + ": serving file " + file.getPath());
            }
        }

        @Override
        public HttpAsyncRequestConsumer<HttpRequest> processRequest(final HttpRequest request, final HttpContext context)
        {
            // Buffer request content in memory for simplicity
            return new BasicAsyncRequestConsumer();
        }

    }

    static final boolean Debug = false;

    private static void debug(final String s)
    {
        if (Debug)
        {
            System.out.println(s);
        }
    }

    public static void main(final String[] args) throws Exception
    {
        new NHttpServer().run(Integer.valueOf(args[0]), new File(args[1]), 0);
    }

    volatile ListeningIOReactor ioReactor;

    public boolean run(final int port, final File docRoot, final long waitMillis) throws IOReactorException,
            InterruptedException
    {
        Executors.newSingleThreadExecutor().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    NHttpServer.this.runBlock(port, docRoot);
                } catch (final IOReactorException e)
                {
                    throw new IllegalStateException(e);
                } catch (final UnrecoverableKeyException e)
                {
                    throw new IllegalStateException(e);
                } catch (final KeyStoreException e)
                {
                    throw new IllegalStateException(e);
                } catch (final NoSuchAlgorithmException e)
                {
                    throw new IllegalStateException(e);
                } catch (final CertificateException e)
                {
                    throw new IllegalStateException(e);
                } catch (final IOException e)
                {
                    throw new IllegalStateException(e);
                } catch (final KeyManagementException e)
                {
                    throw new IllegalStateException(e);
                }
            }
        });
        return this.waitForServerStartup(port, waitMillis);
    }

    public void runBlock(final int port, final File docRoot) throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, UnrecoverableKeyException, KeyManagementException
    {
        if (docRoot == null)
        {
            throw new IllegalArgumentException("No doc root specified.");
        }
        // HTTP parameters for the server
        final HttpParams params = new SyncBasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpTest/1.1");
        // Create HTTP protocol processing chain
        final HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[]
        {
                // Use standard server-side protocol interceptors
                new ResponseDate(),
                new ResponseServer(),
                new ResponseContent(),
                new ResponseConnControl() });
        // Create request handler registry
        final HttpAsyncRequestHandlerRegistry reqistry = new HttpAsyncRequestHandlerRegistry();
        // Register the default handler for all URIs
        reqistry.register("*", new HttpFileHandler(docRoot));
        // Create server-side HTTP protocol handler
        final HttpAsyncService protocolHandler = new HttpAsyncService(httpproc, new DefaultConnectionReuseStrategy(),
                reqistry, params)
        {

            @Override
            public void closed(final NHttpServerConnection conn)
            {
                NHttpServer.debug(conn + ": connection closed");
                super.closed(conn);
            }

            @Override
            public void connected(final NHttpServerConnection conn)
            {
                NHttpServer.debug(conn + ": connection open");
                super.connected(conn);
            }

        };
        // Create HTTP connection factory
        NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory;
        if (port == 8443)
        {
            // Initialize SSL context
            final ClassLoader cl = NHttpServer.class.getClassLoader();
            final URL url = cl.getResource("my.keystore");
            if (url == null)
            {
                NHttpServer.debug("Keystore not found");
                System.exit(1);
            }
            final KeyStore keystore = KeyStore.getInstance("jks");
            keystore.load(url.openStream(), "secret".toCharArray());
            final KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keystore, "secret".toCharArray());
            final KeyManager[] keymanagers = kmfactory.getKeyManagers();
            final SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(keymanagers, null, null);
            connFactory = new SSLNHttpServerConnectionFactory(sslcontext, null, params);
        } else
        {
            connFactory = new DefaultNHttpServerConnectionFactory(params);
        }
        // Create server-side I/O event dispatch
        final IOEventDispatch ioEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, connFactory);
        // Create server-side I/O reactor
        this.ioReactor = new DefaultListeningIOReactor();
        try
        {
            // Listen of the given port
            this.ioReactor.listen(new InetSocketAddress(port));
            // Ready to go!
            this.ioReactor.execute(ioEventDispatch);
        } catch (final InterruptedIOException ex)
        {
            System.err.println("Interrupted");
        } catch (final IOException e)
        {
            System.err.println("I/O error: " + e.getMessage());
        }
        NHttpServer.debug("Shutdown");
    }

    public void stop() throws IOException
    {
        if (this.ioReactor != null)
        {
            this.ioReactor.shutdown(2000);
        }
    }

    private boolean waitForServerStartup(final int port, final long waitMillis) throws InterruptedException
    {
        final long endWait = System.currentTimeMillis() + waitMillis;
        final String urlSpec = "http://localhost:" + port;
        try
        {
            final URL url = new URL(urlSpec);
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
                } catch (final IOException e)
                {
                    // ignore
                    // debug("While waiting: " + e);
                    // e.printStackTrace();
                }
                Thread.sleep(100);
            }
        } catch (final MalformedURLException e)
        {
            throw new IllegalStateException("Error in test code for URL " + urlSpec);
        }
        return false;
    }

}
