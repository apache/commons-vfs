/*
 * Copyright 2002-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.provider.sftp;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.util.Os;

import java.io.File;
import java.util.Properties;

/**
 * Create a HttpClient instance
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class SftpClientFactory
{
    private static final String SSH_DIR_NAME = ".ssh";

    private SftpClientFactory()
    {
    }

    /**
     * Creates a new connection to the server.
     */
    public static Session createConnection(String hostname, int port, String username, String password, FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        JSch jsch = new JSch();

        File sshDir = null;

        // new style - user passed
        File knownHostsFile = SftpFileSystemConfigBuilder.getInstance().getKnownHosts(fileSystemOptions);
        File[] identities = SftpFileSystemConfigBuilder.getInstance().getIdentities(fileSystemOptions);

        if (knownHostsFile != null)
        {
            try
            {
                jsch.setKnownHosts(knownHostsFile.getAbsolutePath());
            }
            catch (JSchException e)
            {
                throw new FileSystemException("vfs.provider.sftp/known-hosts.error", knownHostsFile.getAbsolutePath(), e);
            }
        }
        else
        {
            if (sshDir == null)
            {
                sshDir = findSshDir();
            }
            // Load the known hosts file
            knownHostsFile = new File(sshDir, "known_hosts");
            if (knownHostsFile.isFile() && knownHostsFile.canRead())
            {
                try
                {
                    jsch.setKnownHosts(knownHostsFile.getAbsolutePath());
                }
                catch (JSchException e)
                {
                    throw new FileSystemException("vfs.provider.sftp/known-hosts.error", knownHostsFile.getAbsolutePath(), e);
                }
            }
        }

        if (identities != null)
        {
            for (int iterIdentities = 0; iterIdentities < identities.length; iterIdentities++)
            {
                final File privateKeyFile = identities[iterIdentities];
                try
                {
                    jsch.addIdentity(privateKeyFile.getAbsolutePath());
                }
                catch (final JSchException e)
                {
                    throw new FileSystemException("vfs.provider.sftp/load-private-key.error", privateKeyFile, e);
                }
            }
        }
        else
        {
            if (sshDir == null)
            {
                sshDir = findSshDir();
            }

            // Load the private key (rsa-key only)
            final File privateKeyFile = new File(sshDir, "id_rsa");
            if (privateKeyFile.isFile() && privateKeyFile.canRead())
            {
                try
                {
                    jsch.addIdentity(privateKeyFile.getAbsolutePath());
                }
                catch (final JSchException e)
                {
                    throw new FileSystemException("vfs.provider.sftp/load-private-key.error", privateKeyFile, e);
                }
            }
        }

        Session session;
        try
        {
            session = jsch.getSession(username,
                    hostname,
                    port);
            session.setPassword(password);

            Integer timeout = SftpFileSystemConfigBuilder.getInstance().getTimeout(fileSystemOptions);
            if (timeout != null)
            {
            	session.setTimeout(timeout.intValue());
            }
            
            UserInfo userInfo = SftpFileSystemConfigBuilder.getInstance().getUserInfo(fileSystemOptions);
            if (userInfo != null)
            {
                session.setUserInfo(userInfo);
            }

            Properties config = new Properties();

            //set StrictHostKeyChecking property
            String strictHostKeyChecking = SftpFileSystemConfigBuilder.getInstance().getStrictHostKeyChecking(fileSystemOptions);
            if (strictHostKeyChecking != null)
            {
                config.setProperty("StrictHostKeyChecking", strictHostKeyChecking);
            }

            //set compression property
            String compression = SftpFileSystemConfigBuilder.getInstance().getCompression(fileSystemOptions);
            if (compression != null)
            {
                config.setProperty("compression.s2c", strictHostKeyChecking);
                config.setProperty("compression.c2s", strictHostKeyChecking);
            }

            //set properties for the session
            if (config.size() > 0)
            {
                session.setConfig(config);
            }

            session.connect();
        }
        catch (final Exception exc)
        {
            throw new FileSystemException("vfs.provider.sftp/connect.error", new Object[]{hostname}, exc);
        }


        return session;
    }

    /**
     * Finds the .ssh directory.
     * <p>The lookup order is:</p>
     * <ol>
     * <li>The system property <code>vfs.sftp.sshdir</code> (the override
     * mechanism)</li>
     * <li><code>{user.home}/.ssh</code></li>
     * <li>On Windows only: C:\cygwin\home\{user.name}\.ssh</li>
     * <li>The current directory, as a last resort.</li>
     * <ol>
     * <p/>
     * Windows Notes:
     * The default installation directory for Cygwin is <code>C:\cygwin</code>.
     * On my set up (Gary here), I have Cygwin in C:\bin\cygwin, not the default.
     * Also, my .ssh directory was created in the {user.home} directory.
     * </p>
     *
     * @return The .ssh directory
     */
    private static File findSshDir()
    {
        String sshDirPath;
        sshDirPath = System.getProperty("vfs.sftp.sshdir");
        if (sshDirPath != null)
        {
            File sshDir = new File(sshDirPath);
            if (sshDir.exists())
            {
                return sshDir;
            }
        }

        File sshDir = new File(System.getProperty("user.home"), SSH_DIR_NAME);
        if (sshDir.exists())
        {
            return sshDir;
        }

        if (Os.isFamily(Os.OS_FAMILY_WINDOWS))
        {
            // TODO - this may not be true
            final String userName = System.getProperty("user.name");
            sshDir = new File("C:\\cygwin\\home\\" + userName + "\\" + SSH_DIR_NAME);
            if (sshDir.exists())
            {
                return sshDir;
            }
        }
        return new File("");
    }
}