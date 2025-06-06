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

// COPIED FROM JACKRABBIT 2.20.11 (No additional NOTICE required, see VFS-841)

package org.apache.commons.vfs2.provider.webdav4.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.RepositoryCopier;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.servlet.jackrabbit.JackrabbitRepositoryServlet;
import org.apache.jackrabbit.standalone.Main;
import org.apache.jackrabbit.standalone.cli.CommandException;
import org.apache.jackrabbit.standalone.cli.CommandHelper;
import org.apache.jackrabbit.standalone.cli.JcrClient;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * For testing purpose, copied from org.apache.jackrabbit.standalone.Main with renaming the class name,
 * except of the part calling <CODE>Main.class.getProtectionDomain().getCodeSource().getLocation()</CODE>,
 * which reads some files from the jackrabbit-standalone-component.jar file directly.
 */
public class JackrabbitMain {

    /**
     * @param args
     */
    public static void main(final String[] args) throws Exception {
        new JackrabbitMain(args).run();
    }

    private final Options options = new Options();

    private final CommandLine command;

    private final RequestLogHandler accessLog = new RequestLogHandler();

    private final WebAppContext webapp = new WebAppContext();

    private final Server server = new Server();

    private final ServerConnector connector = new ServerConnector(server);

    /**
     * Constructs Main application instance.
     * <P>
     * <EM>Note:</EM> Constructor is protected because other projects such as Commons VFS can extend this for some reasons
     *       (e.g, unit testing against Jackrabbit WebDAV).
     */
    protected JackrabbitMain(final String[] args) throws ParseException {
        options.addOption("?", "help", false, "print this message");
        options.addOption("n", "notice", false, "print copyright notices");
        options.addOption("l", "license", false, "print license information");
        options.addOption(
                "b", "backup", false, "create a backup of the repository");
        options.addOption(
                "i", "cli", true, "command line access to a remote repository");

        options.addOption("q", "quiet", false, "disable console output");
        options.addOption("d", "debug", false, "enable debug logging");

        options.addOption("h", "host", true, "IP address of the HTTP server");
        options.addOption("p", "port", true, "TCP port of the HTTP server (8080)");
        options.addOption("f", "file", true, "location of this jar file");
        options.addOption("r", "repo", true, "repository directory (jackrabbit)");
        options.addOption("c", "conf", true, "repository configuration file");
        options.addOption(
                "R", "backup-repo", true,
                "backup repository directory (jackrabbit-backupN)");
        options.addOption(
                "C", "backup-conf", true,
                "backup repository configuration file");

        command = new DefaultParser().parse(options, args);
    }

    private void backup(final File sourceDir) throws Exception {
        RepositoryConfig source;
        if (command.hasOption("conf")) {
            source = RepositoryConfig.create(
                    new File(command.getOptionValue("conf")), sourceDir);
        } else {
            source = RepositoryConfig.create(sourceDir);
        }

        File targetDir;
        if (command.hasOption("backup-repo")) {
            targetDir = new File(command.getOptionValue("backup-repo"));
        } else {
            int i = 1;
            do {
                targetDir = new File("jackrabbit-backup" + i++);
            } while (targetDir.exists());
        }

        RepositoryConfig target;
        if (command.hasOption("backup-conf")) {
            target = RepositoryConfig.install(
                    new File(command.getOptionValue("backup-conf")), targetDir);
        } else {
            target = RepositoryConfig.install(targetDir);
        }

        message("Creating a repository copy in " + targetDir);
        RepositoryCopier.copy(source, target);
        message("The repository has been successfully copied.");
    }

    private void copyToOutput(final String resource) throws IOException {
        try (InputStream stream = JackrabbitMain.class.getResourceAsStream(resource)) {
            IOUtils.copy(stream, System.out);
        }
    }

    private void message(final String message) {
        if (!command.hasOption("quiet")) {
            System.out.println(message);
        }
    }

    private void prepareAccessLog(final File log) {
        final NCSARequestLog ncsa = new NCSARequestLog(
                new File(log, "access.log.yyyy_mm_dd").getPath());
        ncsa.setFilenameDateFormat("yyyy-MM-dd");
        accessLog.setRequestLog(ncsa);
    }

    private void prepareConnector() {
        final String port = command.getOptionValue("port", "8080");
        connector.setPort(Integer.parseInt(port));
        final String host = command.getOptionValue("host");
        if (host != null) {
            connector.setHost(host);
        }
    }

    private void prepareServerLog(final File log)
            throws IOException {
        System.setProperty(
                "jackrabbit.log", new File(log, "jackrabbit.log").getPath());
        System.setProperty(
                "jetty.log", new File(log, "jetty.log").getPath());

        if (command.hasOption("debug")) {
            System.setProperty("log.level", "DEBUG");
        } else {
            System.setProperty("log.level", "INFO");
        }

        System.setProperty(
                "derby.stream.error.file",
                new File(log, "derby.log").getPath());
    }

    private void prepareShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    private void prepareWebapp(final File file, final File repository, final File tmp) {
        webapp.setContextPath("/");
        webapp.setWar(file.getPath());
        webapp.setExtractWAR(true);
        webapp.setTempDirectory(tmp);

        final ServletHolder servlet =
            new ServletHolder(JackrabbitRepositoryServlet.class);
        servlet.setInitOrder(1);
        servlet.setInitParameter("repository.home", repository.getPath());
        final String conf = command.getOptionValue("conf");
        if (conf != null) {
            servlet.setInitParameter("repository.config", conf);
        }
        webapp.addServlet(servlet, "/repository.properties");
    }

    /**
     * Run this Main application.
     * <P>
     * <EM>Note:</EM> this is public because this can be used by other projects in unit tests. e.g, Commons-VFS.
     * @throws Exception if any exception occurs
     */
    public void run() throws Exception {
        String defaultFile = "jackrabbit-standalone.jar";
        final URL location =
            Main.class.getProtectionDomain().getCodeSource().getLocation();
        if (location != null && "file".equals(location.getProtocol())) {
            final File file = new File(location.getPath());
            if (file.isFile()) {
                defaultFile = location.getPath();
            }
        }
        final File file = new File(command.getOptionValue("file", defaultFile));

        if (command.hasOption("help")) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar " + file.getName(), options, true);
        } else if (command.hasOption("notice")) {
            copyToOutput("/META-INF/NOTICE.txt");
        } else if (command.hasOption("license")) {
            copyToOutput("/META-INF/LICENSE.txt");
        } else if (command.hasOption("cli")) {
            System.setProperty("logback.configurationFile", "logback-cli.xml");

            final String uri = command.getOptionValue("cli");
            final Repository repository = JcrUtils.getRepository(uri);

            final Context context = new ContextBase();
            CommandHelper.setRepository(context, repository, uri);
            try {
                final Session session = repository.login();
                CommandHelper.setSession(context, session);
                CommandHelper.setCurrentNode(context, session.getRootNode());
            } catch (final RepositoryException ignore) {
                // anonymous login not possible
            }

            new JcrClient(context).runInteractive();

            try {
                CommandHelper.getSession(context).logout();
            } catch (final CommandException ignore) {
                // already logged out
            }
        } else {
            message("Welcome to Apache Jackrabbit!");
            message("-------------------------------");

            final File repository =
                new File(command.getOptionValue("repo", "jackrabbit"));
            message("Using repository directory " + repository);
            repository.mkdirs();
            final File tmp = new File(repository, "tmp");
            tmp.mkdir();
            final File log = new File(repository, "log");
            log.mkdir();

            message("Writing log messages to " + log);
            prepareServerLog(log);

            if (command.hasOption("backup")) {
                backup(repository);
            } else {
                message("Starting the server...");
                prepareWebapp(file, repository, tmp);
                accessLog.setHandler(webapp);
                prepareAccessLog(log);
                server.setHandler(accessLog);
                prepareConnector();
                server.addConnector(connector);
                prepareShutdown();

                try {
                    server.start();

                    String host = connector.getHost();
                    if (host == null) {
                        host = "localhost";
                    }
                    message("Apache Jackrabbit is now running at "
                            +"http://" + host + ":" + connector.getPort() + "/");
                } catch (final Throwable t) {
                    System.err.println(
                            "Unable to start the server: " + t.getMessage());
                    System.exit(1);
                }
            }
        }
    }

    /**
     * Shutdown this Main application.
     * <P>
     * <EM>Note:</EM> this is public because this can be used by other projects in unit tests for graceful shutdown.
     * e.g, Commons-VFS. If this is not invoked properly, some unexpected exceptions may occur on shutdown hook
     * due to an unexpected, invalid state for org.apache.lucene.index.IndexFileDeleter for instance.
     */
    public void shutdown() {
        try {
            message("Shutting down the server...");
            server.stop();
            server.join();
            message("-------------------------------");
            message("Goodbye from Apache Jackrabbit!");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}