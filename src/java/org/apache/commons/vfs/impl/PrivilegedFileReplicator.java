/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
package org.apache.commons.vfs.impl;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.apache.commons.logging.Log;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.FileReplicator;
import org.apache.commons.vfs.provider.VfsComponent;
import org.apache.commons.vfs.provider.VfsComponentContext;

/**
 * A file replicator that wraps another file replicator, performing
 * the replication as a privileged action.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.8 $ $Date: 2002/11/23 00:33:52 $
 */
public class PrivilegedFileReplicator
    implements FileReplicator, VfsComponent
{
    private final FileReplicator replicator;
    private final VfsComponent replicatorComponent;

    public PrivilegedFileReplicator( FileReplicator replicator )
    {
        this.replicator = replicator;
        if ( replicator instanceof VfsComponent )
        {
            replicatorComponent = (VfsComponent)replicator;
        }
        else
        {
            replicatorComponent = null;
        }
    }

    /**
     * Sets the Logger to use for the component.
     */
    public void setLogger( final Log logger )
    {
        if ( replicatorComponent != null  )
        {
            replicatorComponent.setLogger( logger );
        }
    }

    /**
     * Sets the context for the replicator.
     */
    public void setContext( final VfsComponentContext context )
    {
        if ( replicatorComponent != null )
        {
            replicatorComponent.setContext( context );
        }
    }

    /**
     * Initialises the component.
     */
    public void init() throws FileSystemException
    {
        if ( replicatorComponent != null )
        {
            try
            {
                AccessController.doPrivileged( new InitAction() );
            }
            catch ( final PrivilegedActionException e )
            {
                throw new FileSystemException( "vfs.impl/init-replicator.error", null, e );
            }
        }
    }

    /**
     * Closes the replicator.
     */
    public void close()
    {
        if ( replicatorComponent != null )
        {
            AccessController.doPrivileged( new CloseAction() );
        }
    }

    /**
     * Creates a local copy of the file, and all its descendents.
     */
    public File replicateFile( FileObject srcFile, FileSelector selector )
        throws FileSystemException
    {
        try
        {
            final ReplicateAction action = new ReplicateAction( srcFile, selector );
            return (File)AccessController.doPrivileged( action );
        }
        catch ( final PrivilegedActionException e )
        {
            throw new FileSystemException( "vfs.impl/replicate-file.error", new Object[]{srcFile.getName()}, e );
        }
    }

    /** An action that initialises the wrapped replicator. */
    private class InitAction implements PrivilegedExceptionAction
    {
        /**
         * Performs the action.
         */
        public Object run() throws Exception
        {
            replicatorComponent.init();
            return null;
        }
    }

    /** An action that replicates a file using the wrapped replicator. */
    private class ReplicateAction implements PrivilegedExceptionAction
    {
        private final FileObject srcFile;
        private final FileSelector selector;

        public ReplicateAction( final FileObject srcFile,
                                final FileSelector selector )
        {
            this.srcFile = srcFile;
            this.selector = selector;
        }

        /** Performs the action. */
        public Object run() throws Exception
        {
            // TODO - Do not pass the selector through.  It is untrusted
            // TODO - Need to determine which files can be read
            return replicator.replicateFile( srcFile, selector );
        }
    }

    /** An action that closes the wrapped replicator. */
    private class CloseAction implements PrivilegedAction
    {
        /** Performs the action. */
        public Object run()
        {
            replicatorComponent.close();
            return null;
        }
    }
}
