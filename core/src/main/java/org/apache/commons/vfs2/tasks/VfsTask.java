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
package org.apache.commons.vfs2.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.SubBuildListener;
import org.apache.tools.ant.Task;

/**
 * Base class for the VFS Ant tasks.  Takes care of creating a FileSystemManager,
 * and for cleaning it up at the end of the build.  Also provides some
 * utility methods.
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
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
    protected FileObject resolveFile(final String uri)
        throws FileSystemException
    {
        if (manager == null)
        {
            StandardFileSystemManager mngr = new StandardFileSystemManager();
            mngr.setLogger(new AntLogger());
            mngr.init();
            manager = mngr;
            getProject().addBuildListener(new CloseListener());
        }
        return manager.resolveFile(getProject().getBaseDir(), uri);
    }

    /**
     * Close the manager
     */
    protected void closeManager()
    {
        if (manager != null)
        {
            manager.close();
            manager = null;
        }
    }

    /**
     * Closes the VFS manager when the project finishes.
     */
    private class CloseListener
        implements SubBuildListener
    {
        public void subBuildStarted(BuildEvent buildEvent)
        {
        }

        public void subBuildFinished(BuildEvent buildEvent)
        {
            closeManager();
        }

        public void buildFinished(BuildEvent event)
        {
            closeManager();
        }

        public void buildStarted(BuildEvent event)
        {
        }

        public void messageLogged(BuildEvent event)
        {
        }

        public void targetFinished(BuildEvent event)
        {
        }

        public void targetStarted(BuildEvent event)
        {
        }

        public void taskFinished(BuildEvent event)
        {
        }

        public void taskStarted(BuildEvent event)
        {
        }
    }

    /**
     * A commons-logging wrapper for Ant logging.
     */
    private class AntLogger
        implements Log
    {
        public void debug(final Object o)
        {
            log(String.valueOf(o), Project.MSG_DEBUG);
        }

        public void debug(Object o, Throwable throwable)
        {
            debug(o);
        }

        public void error(Object o)
        {
            log(String.valueOf(o), Project.MSG_ERR);
        }

        public void error(Object o, Throwable throwable)
        {
            error(o);
        }

        public void fatal(Object o)
        {
            log(String.valueOf(o), Project.MSG_ERR);
        }

        public void fatal(Object o, Throwable throwable)
        {
            fatal(o);
        }

        public void info(Object o)
        {
            log(String.valueOf(o), Project.MSG_INFO);
        }

        public void info(Object o, Throwable throwable)
        {
            info(o);
        }

        public void trace(Object o)
        {
        }

        public void trace(Object o, Throwable throwable)
        {
        }

        public void warn(Object o)
        {
            log(String.valueOf(o), Project.MSG_WARN);
        }

        public void warn(Object o, Throwable throwable)
        {
            warn(o);
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
