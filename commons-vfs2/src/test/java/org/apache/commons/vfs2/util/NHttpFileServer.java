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
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.MethodNotSupportedException;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.impl.bootstrap.AsyncServerBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncServer;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.entity.AsyncEntityProducers;
import org.apache.hc.core5.http.nio.entity.NoopEntityConsumer;
import org.apache.hc.core5.http.nio.ssl.BasicServerTlsStrategy;
import org.apache.hc.core5.http.nio.ssl.FixedPortStrategy;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.nio.support.BasicRequestConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.http.protocol.HttpDateGenerator;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.reactor.ListenerEndpoint;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;

/**
 * Embedded HTTP/1.1 file server based on a non-blocking I/O model and capable of direct channel (zero copy) data
 * transfer.
 */
public class NHttpFileServer {

    private static class HttpFileHandler implements AsyncServerRequestHandler<Message<HttpRequest, Void>> {

        private final File docRoot;

        HttpFileHandler(final File docRoot) {
            this.docRoot = docRoot;
        }

        @Override
        public void handle(final Message<HttpRequest, Void> message, final ResponseTrigger responseTrigger,
            final HttpContext context) throws HttpException, IOException {
            final HttpRequest request = message.getHead();
            final String method = request.getMethod().toUpperCase(Locale.ROOT);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }

            final URI requestUri;
            try {
                requestUri = request.getUri();
            } catch (final URISyntaxException ex) {
                throw new ProtocolException(ex.getMessage(), ex);
            }
            final String path = requestUri.getPath();
            final File file = new File(docRoot, path);
            final ContentType mimeType = ContentType.TEXT_HTML;
            if (!file.exists()) {

                final String msg = "File " + file.getPath() + " not found";
                println(msg);
                responseTrigger.submitResponse(AsyncResponseBuilder.create(HttpStatus.SC_NOT_FOUND)
                    .setEntity("<html><body><h1>" + msg + "</h1></body></html>", mimeType).build(), context);

            } else if (!file.canRead()) {
                final String msg = "Cannot read file " + file.getPath();
                println(msg);
                responseTrigger.submitResponse(AsyncResponseBuilder.create(HttpStatus.SC_FORBIDDEN)
                    .setEntity("<html><body><h1>" + msg + "</h1></body></html>", mimeType).build(), context);

            } else {

                ContentType contentType;
                final String filename = file.getName().toLowerCase(Locale.ROOT);
// The following causes a failure on Linux and Macos in HttpProviderTestCase:
// org.apache.commons.vfs2.FileSystemException: GET method failed for "http://localhost:37637/read-tests/file1.txt" range "10" with HTTP status 200.
//                at org.apache.commons.vfs2.provider.http.HttpRandomAccessContent.getDataInputStream(HttpRandomAccessContent.java:80)
//                if (filename.endsWith(".txt")) {
//                    contentType = ContentType.TEXT_PLAIN;
//                } else if (filename.endsWith(".html") || filename.endsWith(".htm") || file.isDirectory()) {
//                    contentType = ContentType.TEXT_HTML;
//                } else if (filename.endsWith(".xml")) {
//                    contentType = ContentType.TEXT_XML;
//                } else {
//                    contentType = ContentType.DEFAULT_BINARY;
//                }
                contentType = ContentType.TEXT_HTML;
                final HttpCoreContext coreContext = HttpCoreContext.adapt(context);
                final EndpointDetails endpoint = coreContext.getEndpointDetails();

                println(endpoint + " | serving file " + file.getPath());

                // @formatter:off
                responseTrigger.submitResponse(
                    AsyncResponseBuilder.create(HttpStatus.SC_OK)
                        .setEntity(file.isDirectory()
                            ? AsyncEntityProducers.create(file.toString(), contentType)
                            : AsyncEntityProducers.create(file, contentType))
                        .addHeader(HttpHeaders.LAST_MODIFIED, DateUtils.formatDate(new Date(file.lastModified())))
                    .build(), context);
                // @formatter:on
            }
        }

        @Override
        public AsyncRequestConsumer<Message<HttpRequest, Void>> prepare(final HttpRequest request,
            final EntityDetails entityDetails, final HttpContext context) throws HttpException {
            return new BasicRequestConsumer<>(entityDetails != null ? new NoopEntityConsumer() : null);
        }

    }

    public static boolean DEBUG = Boolean.getBoolean(NHttpFileServer.class.getSimpleName() + ".debug");

    public static void main(final String[] args) throws Exception {
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

    static final void println(final String msg) {
        if (DEBUG) {
            System.out.println(HttpDateGenerator.INSTANCE.getCurrentDate() + " | " + msg);
        }
    }

    public static NHttpFileServer start(final int port, final File docRoot, final long waitMillis)
        throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
        CertificateException, IOException, InterruptedException, ExecutionException {
        return new NHttpFileServer(port, docRoot).start();
    }

    private final File docRoot;
    private ListenerEndpoint listenerEndpoint;
    private final int port;
    private HttpAsyncServer server;

    private NHttpFileServer(final int port, final File docRoot) {
        this.port = port;
        this.docRoot = docRoot;
    }

    private void awaitTermination() throws InterruptedException {
        server.awaitShutdown(TimeValue.MAX_VALUE);
    }

    public void close() {
        if (server.getStatus() == IOReactorStatus.ACTIVE) {
            final CloseMode closeMode = CloseMode.GRACEFUL;
            println("HTTP server shutting down (closeMode=" + closeMode + ")...");
            server.close(closeMode);
            println("HTTP server shut down.");
        }
    }

    public int getPort() {
        if (server == null) {
            return port;
        }
        return ((InetSocketAddress) listenerEndpoint.getAddress()).getPort();
    }

    public void shutdown(final long gracePeriod, final TimeUnit timeUnit) throws InterruptedException {
        if (server != null) {
            server.initiateShutdown();
            server.awaitShutdown(TimeValue.of(gracePeriod, timeUnit));
        }

    }

    private NHttpFileServer start() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException,
        KeyStoreException, CertificateException, IOException, InterruptedException, ExecutionException {
        final AsyncServerBootstrap bootstrap = AsyncServerBootstrap.bootstrap();
        SSLContext sslContext = null;
        if (port == 8443 || port == 443) {
            // Initialize SSL context
            final URL url = NHttpFileServer.class.getResource("/test.keystore");
            if (url == null) {
                println("Keystore not found");
                System.exit(1);
            }
            println("Loading keystore " + url);
            sslContext = SSLContexts.custom()
                .loadKeyMaterial(url, "nopassword".toCharArray(), "nopassword".toCharArray()).build();
            bootstrap.setTlsStrategy(new BasicServerTlsStrategy(sslContext, new FixedPortStrategy(port)));
        }

        // @formatter:off
        final IOReactorConfig config = IOReactorConfig.custom()
                .setSoTimeout(15, TimeUnit.SECONDS)
                .setTcpNoDelay(true)
                .build();
        // @formatter:on

        server = bootstrap.setIOReactorConfig(config).register("*", new HttpFileHandler(docRoot)).create();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }

        });

        server.start();

        final Future<ListenerEndpoint> future = server.listen(new InetSocketAddress(port));
        listenerEndpoint = future.get();
        println("Serving " + docRoot + " on " + listenerEndpoint.getAddress()
            + (sslContext == null ? "" : " with " + sslContext.getProvider() + " " + sslContext.getProtocol()));
        return this;
    }

}
