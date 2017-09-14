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
package org.apache.commons.vfs2.provider.webdav.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;

import junit.framework.Test;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.temp.TemporaryFileProvider;
import org.apache.commons.vfs2.provider.webdav.WebdavFileProvider;
import org.apache.commons.vfs2.provider.webdav.WebdavFileSystemConfigBuilder;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;
import org.apache.commons.vfs2.util.FreeSocketPortUtil;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Test cases for the WebDAV provider.
 *
 */
public class WebdavProviderTestCase extends AbstractProviderTestConfig {
    private static final char[] PASSWORD = new char[0];

    private static final String USER_ID = "admin";

    private static int SocketPort;

    private static final String TEST_URI = "test.webdav.uri";

    private static JackrabbitMain JrMain;

    /**
     * Use %40 for @ in URLs
     */
    private static String ConnectionUri;

    private static File RepoDirectory;

    private static boolean DEBUG = Boolean.getBoolean("WebdavProviderTestCase.Debug");

    static File createTempDirectory() throws IOException {
        // create base folder
        final File base = new File("./target/test").getCanonicalFile();
        base.mkdirs();

        final File tempFile = File.createTempFile("WebdavProviderTestCase_", ".tmp", base);

        if (!tempFile.delete()) {
            throw new IOException("Could not delete temp file: " + tempFile.getAbsolutePath());
        }

        if (!tempFile.mkdir()) {
            throw new IOException("Could not create temp directory: " + tempFile.getAbsolutePath());
        }

        if (DEBUG) {
            System.out.println("Working in " + tempFile);
        }

        return tempFile;
    }

    private static void dump(final File repoDirectory) throws Exception {
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

    /** Recursively outputs the contents of the given node. */
    private static void dump(final Node node) throws RepositoryException {
        // First output the node path
        message(node.getPath());
        // Skip the virtual (and large!) jcr:system subtree
        if (node.getName().equals("jcr:system")) {
            return;
        }

        if (node.getName().equals("jcr:content")) {
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

    private static Session getSession(final TransientRepository repository) throws RepositoryException {
        return repository.login(new SimpleCredentials(USER_ID, PASSWORD));
    }

    private static String getSystemTestUriOverride() {
        return System.getProperty(TEST_URI);
    }

    private static TransientRepository getTransientRepository(final File repoDirectory) throws IOException {
        // Jackrabbit 1.6:
        // TransientRepository repository = new TransientRepository(repoDirectory);
        // Jackrabbit 1.5.2:
        return new TransientRepository(new File(repoDirectory, "repository.xml").toString(), repoDirectory.toString());
    }

    private static void importFiles(final File repoDirectory, final File sourceDir) throws Exception {
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

    private static void importFiles(final Node parent, final File sourceDir) throws RepositoryException, IOException {
        final File[] files = sourceDir.listFiles();
        for (final File file : files) {
            if (file.isFile()) {
                final InputStream data = new FileInputStream(file);
                try {
                    message("Importing file " + file);
                    JcrUtils.putFile(parent, file.getName(), "application/octet-stream", data);
                } finally {
                    data.close();
                }
            } else if (file.isDirectory()) {
                message("Importing folder " + file);
                final Node folder = JcrUtils.getOrAddFolder(parent, file.getName());
                importFiles(folder, file);
            }
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
    private static void setUpClass() throws Exception {
        // Create temp dir for repo
        RepoDirectory = createTempDirectory();
        message("Created temp directory " + RepoDirectory);
        // Populate repo
        importFiles(RepoDirectory, new File(getTestDirectory()));
        dump(RepoDirectory);
        // Start server with temp repo
        startJackrabbit(RepoDirectory);
        message("Returned from org.apache.jackrabbit.standalone.Main " + SocketPort);
    }

    /**
     * Starts an embedded Apache Jackrabbit server.
     *
     * @param repoDirectory
     * @throws Exception
     */
    private static void startJackrabbit(final File repoDirectory) throws Exception {
        boolean quiet = false;
        if (!DEBUG) {
            Logger.getLogger("org.apache.jackrabbit").setLevel(Level.WARN);
            Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.ERROR);
            Logger.getLogger("org.apache.commons.vfs2").setLevel(Level.WARN);
            Logger.getLogger("org.mortbay").setLevel(Level.WARN);
            quiet = true;
        }
        JrMain = new JackrabbitMain(new String[] { "--port", Integer.toString(SocketPort), "--repo",
                repoDirectory.toString(), quiet ? "--quiet" : "" });
        JrMain.run();
    }

    public static Test suite() throws Exception {
        return new ProviderTestSuite(new WebdavProviderTestCase()) {
            @Override
            protected void setUp() throws Exception {
                if (getSystemTestUriOverride() == null) {
                    setUpClass();
                }
                super.setUp();
            }

            @Override
            protected void tearDown() throws Exception {
                tearDownClass();
                super.tearDown();
            }
        };
    }

    /**
     * Tears down resources for this test case.
     * <ol>
     * <li>Shuts down the embedded Jackrabbit</li>
     * <li>Extra clean up for org.apache.commons.httpclient.MultiThreadedHttpConnectionManager</li>
     * <li>Remove temporary repository directory.</li>
     * </ol>
     * Stops the embedded Apache WebDAV Server.
     *
     * @throws Exception @throws
     */
    private static void tearDownClass() throws Exception {
        // Main JR shutdown
        JrMain.shutdown();
        // WARN logged because one thread is still there, so clean up explicitly.
        MultiThreadedHttpConnectionManager.shutdownAll();

        if (DEBUG) {
            message("Skipping cleanup of " + RepoDirectory);
            return;
        }

        // Remove repo dir
        try {
            message("Deleting temp directory " + RepoDirectory);
            FileUtils.deleteDirectory(RepoDirectory);
        } catch (final IOException e) {
            message(e);
            if (RepoDirectory.exists()) {
                message("Directory will be deleted on VM exit " + RepoDirectory);
                RepoDirectory.deleteOnExit();
            }
        }
    }

    public WebdavProviderTestCase() throws IOException {
        SocketPort = FreeSocketPortUtil.findFreeLocalPort();
        message("FreeSocketPortUtil.findFreeLocalPort() = " + SocketPort);
        // Use %40 for @ in a URL
        // Any user id and password will do with the default Jackrabbit set up.
        ConnectionUri = String.format("webdav://%s@localhost:%d/repository/default", USER_ID, SocketPort);
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
        final WebdavFileSystemConfigBuilder builder = (WebdavFileSystemConfigBuilder) manager
                .getFileSystemConfigBuilder("webdav");
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
        manager.addProvider("webdav", new WebdavFileProvider());
        manager.addProvider("tmp", new TemporaryFileProvider());
    }

}
