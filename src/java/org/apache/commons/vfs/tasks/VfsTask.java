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
package org.apache.commons.vfs.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
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
 * @version $Revision$ $Date$
 */
public class VfsTask
    extends Task
{
    // private static StandardFileSystemManager manager;

    /**
     * Hold the reference to VFS and a refcounter.
     *
     * For every "target" which is called within the current target the refcount is incremented.
     * When the target ends this refcount is decremented.
     * If 0 is reached VFS will shutdown.
     */
    private static class VfsRef
    {
        StandardFileSystemManager manager;

        // start with refcount 1 as we are in an ant "target"
        volatile int refcount = 1;
    }

    /**
     * Resolves a URI to a file, relative to the project's base directory.
     *
     * @param uri The URI to resolve.
     */
    protected FileObject resolveFile(final String uri)
        throws FileSystemException
    {
        VfsRef vfsRef = (VfsRef) getProject().getReference(VFS.class.getName());
        if (vfsRef == null)
        {
            vfsRef = new VfsRef();
        }

        synchronized(vfsRef)
        {
            if (vfsRef.manager == null)
            {
                vfsRef.manager = new StandardFileSystemManager();
                vfsRef.manager.setLogger(new AntLogger());
                vfsRef.manager.init();
                getProject().addBuildListener(new CloseListener());

                getProject().addReference(VFS.class.getName(), vfsRef);
            }
        }

        return vfsRef.manager.resolveFile(getProject().getBaseDir(), uri);
    }

    /**
     * Closes the VFS manager when the project finishes.
     */
    private class CloseListener
        implements BuildListener
    {
        public void subBuildStarted(BuildEvent event)
        {
            // event.getProject().log("subbuild started", Project.MSG_ERR);
        }

        public void subBuildFinished(BuildEvent event)
        {
            // event.getProject().log("subbuild finished", Project.MSG_ERR);
        }

        public void buildFinished(BuildEvent event)
        {
            // event.getProject().log("build finished", Project.MSG_ERR);

            /*
            VfsRef vfsRef = (VfsRef) getProject().getReference(VFS.class.getName());
            if (vfsRef != null)
            {
                vfsRef.manager.close();
                vfsRef = null;
            }
            */
        }

        public void buildStarted(BuildEvent event)
        {
            // event.getProject().log("build started", Project.MSG_ERR);
        }

        public void messageLogged(BuildEvent event)
        {
        }

        public void targetFinished(BuildEvent event)
        {
            // event.getProject().log("target finished", Project.MSG_ERR);

            VfsRef vfsRef = (VfsRef) getProject().getReference(VFS.class.getName());
            if (vfsRef != null)
            {
                synchronized(vfsRef)
                {
                    if (vfsRef.manager != null)
                    {
                        vfsRef.refcount--;
                        if (vfsRef.refcount < 1)
                        {
                            vfsRef.manager.close();
                            vfsRef.manager = null;
                        }
                    }

                    getProject().removeBuildListener(CloseListener.this);
                }
            }
        }

        public void targetStarted(BuildEvent event)
        {
            // event.getProject().log("target started", Project.MSG_ERR);

            VfsRef vfsRef = (VfsRef) getProject().getReference(VFS.class.getName());
            if (vfsRef != null)
            {
                synchronized(vfsRef)
                {
                    if (vfsRef.manager != null)
                    {
                        vfsRef.refcount++;
                    }
                }
            }
        }

        public void taskFinished(BuildEvent event)
        {
            // event.getProject().log("task finished", Project.MSG_ERR);
        }

        public void taskStarted(BuildEvent event)
        {
            // event.getProject().log("task started", Project.MSG_ERR);
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
