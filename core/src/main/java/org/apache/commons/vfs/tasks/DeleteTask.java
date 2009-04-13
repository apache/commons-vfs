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
package org.apache.commons.vfs.tasks;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.util.Messages;
import org.apache.tools.ant.BuildException;

import java.util.StringTokenizer;

/**
 * An Ant task that deletes matching files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 * @todo Allow selector to be specified.
 */
public class DeleteTask
    extends VfsTask
{
    private String file;
    private String srcDirUrl;
    private String filesList;

    /**
     * Sets the file/folder to delete.
     *
     * @param file
     */
    public void setFile(final String file)
    {
        this.file = file;
    }

    /**
     * Sets the source directory
     */
    public void setSrcDir(final String srcDir)
    {
        this.srcDirUrl = srcDir;
    }

    /**
     * Sets the files to includes
     */
    public void setIncludes(final String filesList)
    {
        this.filesList = filesList;
    }

    /**
     * Executes this task.
     */
    public void execute() throws BuildException
    {
        if ((file == null && srcDirUrl == null) || (srcDirUrl != null && filesList == null))
        {
            final String message = Messages.getString("vfs.tasks/delete.no-source-files.error");
            throw new BuildException(message);
        }

        try
        {
            if (srcDirUrl != null && filesList != null)
            {
                log("Deleting " + filesList + " in the directory " + srcDirUrl);
                if (!srcDirUrl.endsWith("/"))
                {
                    srcDirUrl += "/";
                }
                StringTokenizer tok = new StringTokenizer(filesList, ", \t\n\r\f", false);
                while (tok.hasMoreTokens())
                {
                    String nextFile = tok.nextToken();
                    final FileObject srcFile = resolveFile(srcDirUrl + nextFile);
                    srcFile.delete(Selectors.SELECT_ALL);
                }
            }
            else
            {
                final FileObject srcFile = resolveFile(file);
                log("Deleting " + srcFile);
                srcFile.delete(Selectors.SELECT_ALL);
            }
        }
        catch (final Exception e)
        {
            throw new BuildException(e);
        }
    }
}
