/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
import java.io.File;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.commons.vfs.util.Os;

/**
 * A provider for accessing files over SFTP.
 * 
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @author Gary D. Gregory
 * @version $Id: SftpFileProvider.java,v 1.6 2004/02/28 03:35:51 bayard Exp $
 */
public class SftpFileProvider extends AbstractOriginatingFileProvider {

    private static final String SSH_DIR_NAME = ".ssh";

    private JSch jSch = new JSch();

    /**
	 * Creates a {@link FileSystem}.
	 */
    protected FileSystem doCreateFileSystem(final FileName rootName) {
        return new SftpFileSystem((GenericFileName) rootName, this.getJSch());
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
	 * <p>
     * Windows Notes: 
     * The default installation directory for Cygwin is <code>C:\cygwin</code>.
     * On my set up (Gary here), I have Cygwin in C:\bin\cygwin, not the default.
     * Also, my .ssh directory was created in the {user.home} directory.
     * </p> 
	 * @return The .ssh directory
	 */
    private File findSshDir() {
        String sshDirPath;
        sshDirPath = System.getProperty("vfs.sftp.sshdir");
        if (sshDirPath != null) {
            File sshDir = new File(sshDirPath);
            if (sshDir.exists()) {
                return sshDir;
            }
        }

        File sshDir = new File(System.getProperty("user.home"), SSH_DIR_NAME);
        if (sshDir.exists()) {
            return sshDir;
        }

        if (Os.isFamily(Os.OS_FAMILY_WINDOWS)) {
            // TODO - this may not be true
            final String userName = System.getProperty("user.name");
            sshDir = new File("C:\\cygwin\\home\\" + userName + "\\" + SSH_DIR_NAME);
            if (sshDir.exists()) {
                return sshDir;
            }
        }
        return new File("");
    }

    /**
	 * Returns the JSch.
	 * 
	 * @return Returns the jSch.
	 */
    private JSch getJSch() {
        return this.jSch;
    }

    /**
	 * Initialises the component.
	 */
    public void init() throws FileSystemException {
        // Figure out where the ssh directory is
        File sshDir = this.findSshDir();

        // Load the known hosts file
        final File knownHostsFile = new File(sshDir, "known_hosts");
        if (knownHostsFile.isFile() && knownHostsFile.canRead()) {
            this.getJSch().setKnownHosts(knownHostsFile.getAbsolutePath());
        }

        // Load the private key
        final File privateKeyFile = new File(sshDir, "id_rsa");
        if (privateKeyFile.isFile() && privateKeyFile.canRead()) {
            try {
                this.getJSch().addIdentity(privateKeyFile.getAbsolutePath());
            } catch (final JSchException e) {
                throw new FileSystemException("vfs.provider.sftp/load-private-key.error", privateKeyFile, e);
            }
        }
    }

    /**
	 * Parses an absolute URI.
	 */
    protected FileName parseUri(final String uri) throws FileSystemException {
        return GenericFileName.parseUri(uri, 22);
    }
}