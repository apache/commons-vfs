/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002, 2003 The Apache Software Foundation.  All rights
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
package org.apache.commons.vfs.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Base class for the VFS Ant tasks.  Takes care of creating a FileSystemManager,
 * and for cleaning it up at the end of the build.  Also provides some
 * utility methods.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.8 $ $Date: 2003/10/13 08:44:42 $
 */
public class VfsTask
    extends Task
{
    private static StandardFileSystemManager manager;

    /**
     * Resolves a URI to a file, relative to the project's base directory.
     *
     * @param uri The URI to resolve.
     */
    protected FileObject resolveFile( final String uri )
        throws FileSystemException
    {
        if ( manager == null )
        {
            manager = new StandardFileSystemManager();
            manager.setLogger( new AntLogger() );
            manager.init();
            getProject().addBuildListener( new CloseListener() );
        }
        return manager.resolveFile( getProject().getBaseDir(), uri );
    }

    /** Closes the VFS manager when the project finishes. */
    private class CloseListener
        implements BuildListener
    {
        public void buildFinished( BuildEvent event )
        {
            if ( manager != null )
            {
                manager.close();
                manager = null;
            }
        }

        public void buildStarted( BuildEvent event )
        {
        }

        public void messageLogged( BuildEvent event )
        {
        }

        public void targetFinished( BuildEvent event )
        {
        }

        public void targetStarted( BuildEvent event )
        {
        }

        public void taskFinished( BuildEvent event )
        {
        }

        public void taskStarted( BuildEvent event )
        {
        }
    }

    /** A commons-logging wrapper for Ant logging. */
    private class AntLogger
        implements Log
    {
        public void debug( final Object o )
        {
            log( String.valueOf( o ), Project.MSG_DEBUG );
        }

        public void debug( Object o, Throwable throwable )
        {
            debug( o );
        }

        public void error( Object o )
        {
            log( String.valueOf( o ), Project.MSG_ERR );
        }

        public void error( Object o, Throwable throwable )
        {
            error( o );
        }

        public void fatal( Object o )
        {
            log( String.valueOf( o ), Project.MSG_ERR );
        }

        public void fatal( Object o, Throwable throwable )
        {
            fatal( o );
        }

        public void info( Object o )
        {
            log( String.valueOf( o ), Project.MSG_INFO );
        }

        public void info( Object o, Throwable throwable )
        {
            info( o );
        }

        public void trace( Object o )
        {
        }

        public void trace( Object o, Throwable throwable )
        {
        }

        public void warn( Object o )
        {
            log( String.valueOf( o ), Project.MSG_WARN );
        }

        public void warn( Object o, Throwable throwable )
        {
            warn( o );
        }

        public boolean isDebugEnabled()
        {
            return true;
        }

        public boolean isErrorEnabled()
        {
            return true;
        }

        public boolean isFatalEnabled()
        {
            return true;
        }

        public boolean isInfoEnabled()
        {
            return true;
        }

        public boolean isTraceEnabled()
        {
            return false;
        }

        public boolean isWarnEnabled()
        {
            return true;
        }
    }
}
