/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider.webdav4.test;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.IPv6LocalConnectionTests;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.temp.TemporaryFileProvider;
import org.apache.commons.vfs2.provider.webdav4.Webdav4FileProvider;
import org.apache.commons.vfs2.provider.webdav4.Webdav4FileSystemConfigBuilder;
import org.apache.commons.vfs2.util.FreeSocketPortUtil;
import org.apache.jackrabbit.core.TransientRepository;

/**
 * Test cases for the WebDAV4 provider.
 * Do NOT use org.apache.jackrabbit.standalone.Main.
 */
public class Webdav4ProviderTestCase extends AbstractProviderTestConfig {

    private static final String USER_ID = "admin";

    private static final String PASSWORD = "admin";

    private static final char[] PASSWORD_CHARS = PASSWORD.toCharArray();

    private static int SocketPort;

    private static final String TEST_URI = "test.webdav4.uri";

    private static JackrabbitMain jrMain;

    /**
     * Use %40 for @ in URLs
     */
    private static String ConnectionUri;

    private static Path RepoDirectory;

    private static final boolean DEBUG = Boolean.getBoolean("Webdav4ProviderTestCase.Debug");

    static Path createTempDirectory() throws IOException {
        // create base folder
        final Path base = Paths.get("target/test").normalize();
        Files.createDirectories(base);
        final Path tempFile = Files.createTempFile(base, "WebdavProviderTestCase_", ".tmp");
        Files.delete(tempFile);
        Files.createDirectories(base);

        if (DEBUG) {
            System.out.println("Working in " + tempFile);
        }

        return tempFile;
    }

    /** Recursively outputs the contents of the given node. */
    private static void dump(final Node node) throws RepositoryException {
        // First output the node path
        message(node.getPath());
        // Skip the virtual (and large!) jcr:system subtree
        if (node.getName().equals("jcr:system") || node.getName().equals("jcr:content")) {
            return;
        }

        // Then output the properties
        final PropertyIterator properties = node.getProperties();
        while (properties.hasNext()) {
            final Property property = properties.nextProperty();
            if (property.getDefinition().isMultiple()) {
                // A multi-valued property, print all values
                final Value[] values = property.getValues();
                for (final Value value : values) {
                    message(property.getPath() + " = " + value.getString());
                }
            } else {
                // A single-valued property
                message(property.getPath() + " = " + property.getString());
            }
        }

        // Finally output all the child nodes recursively
        final NodeIterator nodes = node.getNodes();
        while (nodes.hasNext()) {
            dump(nodes.nextNode());
        }
    }

    private static void dump(final Path repoDirectory) throws Exception {
        final TransientRepository repository = getTransientRepository(repoDirectory);
        try {
            final Session session = getSession(repository);
            message("Root node dump:");
            dump(session.getRootNode());
            session.logout();
        } finally {
            repository.shutdown();
        }
    }

    private static Session getSession(final TransientRepository repository) throws RepositoryException {
        return repository.login(new SimpleCredentials(USER_ID, PASSWORD_CHARS));
    }

    public static String getSystemTestUriOverride() {
        return System.getProperty(TEST_URI);
    }

    private static TransientRepository getTransientRepository(final Path repoDirectory) {
        return new TransientRepository(repoDirectory.resolve("repository.xml").toString(), repoDirectory.toString());
    }

    private static void importFiles(final Node parent, final File sourceDir) throws RepositoryException, IOException {
        final File[] files = sourceDir.listFiles();
        // System.out.printf("Test importing %,d files from %s...%n", files.length, sourceDir);
        for (final File file : files) {
            if (file.isFile()) {
                try (final InputStream data = Files.newInputStream(file.toPath())) {
                    message("Importing file " + file);
                    JcrUtils.putFile(parent, file.getName(), MIME_TYPE_APPLICATION_OCTET_STREAM, data);
                }
            } else if (file.isDirectory()) {
                message("Importing folder " + file);
                final Node folder = JcrUtils.getOrAddFolder(parent, file.getName());
                importFiles(folder, file);
            }
        }
    }

    private static void importFiles(final Path repoDirectory, final File sourceDir) throws Exception {
        final TransientRepository repository = getTransientRepository(repoDirectory);
        try {
            final Session session = getSession(repository);
            importFiles(session.getRootNode(), sourceDir);
            session.save();
            session.logout();
        } finally {
            repository.shutdown();
        }
    }

    private static void message(final IOException e) {
        if (DEBUG) {
            e.printStackTrace();
        }
    }

    private static void message(final String string) {
        if (DEBUG) {
            System.out.println(string);
        }
    }

    /**
     * Creates and starts an embedded Apache WebDAV Server (Jackrabbit).
     *
     * @throws Exception
     */
    public static void setUpClass() throws Exception {
        // Create temp dir for repo
        RepoDirectory = createTempDirectory();
        message("Created temp directory " + RepoDirectory);
        // Populate repo
        importFiles(RepoDirectory, new File(getTestDirectory()));
        dump(RepoDirectory);
        // Start server with temp repo
        startJackrabbit(RepoDirectory);
        message("Returned from " + JackrabbitMain.class.getName() +  " " + SocketPort);
    }

    /**
     * Starts an embedded Apache Jackrabbit server.
     *
     * @param repoDirectory
     * @throws Exception
     */
    private static void startJackrabbit(final Path repoDirectory) throws Exception {
        boolean quiet = false;

        if (!DEBUG) {
            quiet = true;
        }

        jrMain = new JackrabbitMain(new String[] { "--port", Integer.toString(SocketPort), "--repo", repoDirectory.toString(),
                quiet ? "--quiet" : "" }) {
        };

        jrMain.run();
    }



    /**
     * Tears down resources for this test case.
     * <ol>
     * <li>Shuts down the embedded Jackrabbit</li>
     * <li>Extra clean up for org.apache.commons.httpclient.MultiThreadedHttpConnectionManager</li>
     * <li>Remove temporary repository directory.</li>
     * </ol>
     * Stops the embedded Apache WebDAV Server.
     */
    public static void tearDownClass() {
        // Stop Jackrabbit Main for graceful shutdown
        jrMain.shutdown();

        if (DEBUG) {
            message("Skipping cleanup of " + RepoDirectory);
            return;
        }

        // Remove repo dir
        try {
            message("Deleting temp directory " + RepoDirectory);
            PathUtils.deleteDirectory(RepoDirectory);
        } catch (final IOException e) {
            message(e);
            try {
                Files.delete(RepoDirectory);
            } catch (final IOException e1) {
                message("Directory will be deleted on VM exit " + RepoDirectory);
                RepoDirectory.toFile().deleteOnExit();
            }
        }
    }

    public Webdav4ProviderTestCase() throws IOException {
        SocketPort = FreeSocketPortUtil.findFreeLocalPort();
        message("FreeSocketPortUtil.findFreeLocalPort() = " + SocketPort);
        // Use %40 for @ in a URL
        // Any user id and password will do with the default Jackrabbit set up.
        ConnectionUri = String.format("webdav4://%s:%s@localhost:%d/repository/default", USER_ID, PASSWORD, SocketPort);
    }

    /**
     * Returns the base folder for tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        String uri = getSystemTestUriOverride();
        if (uri == null) {
            uri = ConnectionUri;
        }
        final Webdav4FileSystemConfigBuilder builder = (Webdav4FileSystemConfigBuilder) manager
                .getFileSystemConfigBuilder("webdav4");
        final FileSystemOptions opts = new FileSystemOptions();
        builder.setRootURI(opts, uri);
        return manager.resolveFile(uri, opts);
    }

    @Override
    public boolean isFileSystemRootAccessible() {
        return false;
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("webdav4", new Webdav4FileProvider());
        manager.addProvider("tmp", new TemporaryFileProvider());
    }

    @org.junit.Test
    public void testResolveIPv6Url() throws Exception {
        final String ipv6Url = "webdav4://user:pass@[fe80::1c42:dae:8370:aea6%en1]/file.txt";

        final FileObject fileObject = VFS.getManager().resolveFile(ipv6Url, new FileSystemOptions());

        assertEquals("webdav4://user:pass@[fe80::1c42:dae:8370:aea6%en1]/", fileObject.getFileSystem().getRootURI());
        assertEquals("webdav4://user:pass@[fe80::1c42:dae:8370:aea6%en1]/file.txt", fileObject.getName().getURI());
    }
}
