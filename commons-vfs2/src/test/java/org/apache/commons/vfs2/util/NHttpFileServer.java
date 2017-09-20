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
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.ExceptionLogger;
import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.impl.nio.bootstrap.HttpServer;
import org.apache.http.impl.nio.bootstrap.ServerBootstrap;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.ssl.SSLContexts;

/**
 * Embedded HTTP/1.1 file server based on a non-blocking I/O model and capable of direct channel (zero copy) data
 * transfer.
 */
public class NHttpFileServer {

    static class HttpFileHandler implements HttpAsyncRequestHandler<HttpRequest> {

        private final File docRoot;

        public HttpFileHandler(final File docRoot) {
            super();
            this.docRoot = docRoot;
        }

        @Override
        public void handle(final HttpRequest request, final HttpAsyncExchange httpexchange, final HttpContext context)
                throws HttpException, IOException {
            final HttpResponse response = httpexchange.getResponse();
            handleInternal(request, response, context);
            httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
        }

        private void handleInternal(final HttpRequest request, final HttpResponse response, final HttpContext context)
                throws HttpException, IOException {

            final String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }

            final String target = request.getRequestLine().getUri();
            final File file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));
            final String mimeType = "text/html";
            if (!file.exists()) {

                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                final NStringEntity entity = new NStringEntity(
                        "<html><body><h1>File " + file.getPath() + " not found</h1></body></html>",
                        ContentType.create(mimeType, "UTF-8"));
                response.setEntity(entity);
                debug("File " + file.getPath() + " not found");

            } else if (!file.canRead() /* || file.isDirectory() */) {

                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                final NStringEntity entity = new NStringEntity("<html><body><h1>Access denied</h1></body></html>",
                        ContentType.create(mimeType, "UTF-8"));
                response.setEntity(entity);
                debug("Cannot read file " + file.getPath());

            } else {

                final HttpCoreContext coreContext = HttpCoreContext.adapt(context);
                final HttpConnection conn = coreContext.getConnection(HttpConnection.class);
                response.setStatusCode(HttpStatus.SC_OK);
                final HttpEntity body = file.isDirectory()
                        ? new NStringEntity(file.toString(), ContentType.create(mimeType))
                        : new NFileEntity(file, ContentType.create(mimeType));
                response.setEntity(body);
                if (!response.containsHeader(HttpHeaders.LAST_MODIFIED)) {
                    response.addHeader(HttpHeaders.LAST_MODIFIED, DateUtils.formatDate(new Date(file.lastModified())));
                }
                debug(conn + ": serving file " + file.getPath());
            }
        }

        @Override
        public HttpAsyncRequestConsumer<HttpRequest> processRequest(final HttpRequest request,
                final HttpContext context) {
            // Buffer request content in memory for simplicity
            return new BasicAsyncRequestConsumer();
        }

    }

    public static boolean DEBUG = false;

    private static void debug(final String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }

    public static void main(final String[] args) throws KeyManagementException, UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, InterruptedException {
        if (args.length < 1) {
            System.err.println("Please specify document root directory");
            System.exit(1);
        }
        // Document root directory
        final File docRoot = new File(args[0]);
        int port = 8080;
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }
        new NHttpFileServer(port, docRoot).start().awaitTermination();
    }

    public static NHttpFileServer start(final int port, final File docRoot, final long waitMillis)
            throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
            CertificateException, IOException, InterruptedException {
        return new NHttpFileServer(port, docRoot).start();
    }

    private final File docRoot;
    private final int port;

    private HttpServer server;

    private NHttpFileServer(final int port, final File docRoot) {
        this.port = port;
        this.docRoot = docRoot;
    }

    private void awaitTermination() throws InterruptedException {
        server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown(5, TimeUnit.SECONDS);
            }
        });
    }

    public void shutdown(final long gracePeriod, final TimeUnit timeUnit) {
        if (server != null) {
            server.shutdown(gracePeriod, timeUnit);
        }

    }

    private NHttpFileServer start() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, CertificateException, IOException, InterruptedException {
        SSLContext sslContext = null;
        if (port == 8443) {
            // Initialize SSL context
            final URL url = NHttpFileServer.class.getResource("/test.keystore");
            if (url == null) {
                debug("Keystore not found");
                System.exit(1);
            }
            debug("Loading keystore " + url);
            sslContext = SSLContexts.custom()
                    .loadKeyMaterial(url, "nopassword".toCharArray(), "nopassword".toCharArray()).build();
        }

        final IOReactorConfig config = IOReactorConfig.custom().setSoTimeout(15000).setTcpNoDelay(true).build();

        // @formatter:off
        server = ServerBootstrap.bootstrap()
                .setListenerPort(port)
                .setServerInfo("Test/1.1")
                .setIOReactorConfig(config)
                .setSslContext(sslContext)
                .setExceptionLogger(ExceptionLogger.STD_ERR)
                .registerHandler("*", new HttpFileHandler(docRoot)).create();
        // @formatter:on

        server.start();
        debug("Serving " + docRoot + " on " + server.getEndpoint().getAddress()
                + (sslContext == null ? "" : " with " + sslContext.getProvider() + " " + sslContext.getProtocol()));
        server.getEndpoint().waitFor();
        // Thread.sleep(startWaitMillis); // hack
        return this;
    }

}
