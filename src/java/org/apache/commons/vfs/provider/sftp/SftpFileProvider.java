/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
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
 * @version $Revision: 1.4 $ $Date: 2003/10/13 08:44:27 $
 */
public class SftpFileProvider
    extends AbstractOriginatingFileProvider
{
    private JSch jSch = new JSch();

    /**
     * Initialises the component.
     */
    public void init() throws FileSystemException
    {
        // Figure out where the ssh directory is
        final File sshDir;
        if ( Os.isFamily( Os.OS_FAMILY_WINDOWS ) )
        {
            // TODO - this may not be true
            final String userName = System.getProperty( "user.name" );
            sshDir = new File("C:\\cygwin\\home\\" + userName + "\\.ssh" );
        }
        else
        {
            sshDir = new File( System.getProperty( "user.home" ), ".ssh" );
        }

        // Load the known hosts file
        final File knownHostsFile = new File(sshDir, "known_hosts");
        if ( knownHostsFile.isFile() && knownHostsFile.canRead() )
        {
            jSch.setKnownHosts( knownHostsFile.getAbsolutePath() );
        }

        // Load the private key
        final File privateKeyFile = new File( sshDir, "id_rsa" );
        if ( privateKeyFile.isFile() && privateKeyFile.canRead() )
        {
            try
            {
                jSch.addIdentity(privateKeyFile.getAbsolutePath());
            }
            catch ( final JSchException e )
            {
                throw new FileSystemException("vfs.provider.sftp/load-private-key.error", privateKeyFile, e);
            }
        }
    }

    /**
     * Parses an absolute URI.
     */
    protected FileName parseUri( final String uri )
        throws FileSystemException
    {
        return GenericFileName.parseUri( uri, 22 );
    }

    /**
     * Creates a {@link FileSystem}.
     */
    protected FileSystem doCreateFileSystem( final FileName rootName )
        throws FileSystemException
    {
        return new SftpFileSystem( (GenericFileName)rootName, jSch );
    }
}
