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
        @Override
        public void subBuildStarted(BuildEvent buildEvent)
        {
        }

        @Override
        public void subBuildFinished(BuildEvent buildEvent)
        {
            closeManager();
        }

        @Override
        public void buildFinished(BuildEvent event)
        {
            closeManager();
        }

        @Override
        public void buildStarted(BuildEvent event)
        {
        }

        @Override
        public void messageLogged(BuildEvent event)
        {
        }

        @Override
        public void targetFinished(BuildEvent event)
        {
        }

        @Override
        public void targetStarted(BuildEvent event)
        {
        }

        @Override
        public void taskFinished(BuildEvent event)
        {
        }

        @Override
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
        @Override
        public void debug(final Object o)
        {
            log(String.valueOf(o), Project.MSG_DEBUG);
        }

        @Override
        public void debug(Object o, Throwable throwable)
        {
            debug(o);
        }

        @Override
        public void error(Object o)
        {
            log(String.valueOf(o), Project.MSG_ERR);
        }

        @Override
        public void error(Object o, Throwable throwable)
        {
            error(o);
        }

        @Override
        public void fatal(Object o)
        {
            log(String.valueOf(o), Project.MSG_ERR);
        }

        @Override
        public void fatal(Object o, Throwable throwable)
        {
            fatal(o);
        }

        @Override
        public void info(Object o)
        {
            log(String.valueOf(o), Project.MSG_INFO);
        }

        @Override
        public void info(Object o, Throwable throwable)
        {
            info(o);
        }

        @Override
        public void trace(Object o)
        {
        }

        @Override
        public void trace(Object o, Throwable throwable)
        {
        }

        @Override
        public void warn(Object o)
        {
            log(String.valueOf(o), Project.MSG_WARN);
        }

        @Override
        public void warn(Object o, Throwable throwable)
        {
            warn(o);
        }

        @Override
        public boolean isDebugEnabled()
        {
            return true;
        }

        @Override
        public boolean isErrorEnabled()
        {
            return true;
        }

        @Override
        public boolean isFatalEnabled()
        {
            return true;
        }

        @Override
        public boolean isInfoEnabled()
        {
            return true;
        }

        @Override
        public boolean isTraceEnabled()
        {
            return false;
        }

        @Override
        public boolean isWarnEnabled()
        {
            return true;
        }
    }
}
