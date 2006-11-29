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

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.tools.ant.BuildException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * An Ant task that writes the details of a file to Ant's log.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class ShowFileTask
    extends VfsTask
{
    private String url;
    private boolean showContent;
    private boolean recursive;
    private static final String INDENT = "  ";

    /**
     * The URL of the file to display.
     */
    public void setFile(final String url)
    {
        this.url = url;
    }

    /**
     * Shows the content.  Assumes the content is text, encoded using the
     * platform's default encoding.
     */
    public void setShowContent(final boolean showContent)
    {
        this.showContent = showContent;
    }

    /**
     * Recursively shows the decendents of the file.
     */
    public void setRecursive(final boolean recursive)
    {
        this.recursive = recursive;
    }

    /**
     * Executes the task.
     */
    public void execute() throws BuildException
    {
        try
        {
            final FileObject file = resolveFile(url);
            log("Details of " + file.getName().getURI());
            showFile(file, INDENT);
        }
        catch (final Exception e)
        {
            throw new BuildException(e);
        }
    }

    /**
     * Logs the details of a file.
     */
    private void showFile(final FileObject file, final String prefix) throws Exception
    {
        // Write details
        StringBuffer msg = new StringBuffer(prefix);
        msg.append(file.getName().getBaseName());
        if (file.exists())
        {
            msg.append(" (");
            msg.append(file.getType().getName());
            msg.append(")");
        }
        else
        {
            msg.append(" (unknown)");
        }
        log(msg.toString());

        if (file.exists())
        {
            final String newPrefix = prefix + INDENT;
            if (file.getType().hasContent())
            {
                final FileContent content = file.getContent();
                log(newPrefix + "Content-Length: " + content.getSize());
                log(newPrefix + "Last-Modified" + new Date(content.getLastModifiedTime()));
                if (showContent)
                {
                    log(newPrefix + "Content:");
                    logContent(file, newPrefix);
                }
            }
            if (file.getType().hasChildren())
            {
                final FileObject[] children = file.getChildren();
                for (int i = 0; i < children.length; i++)
                {
                    FileObject child = children[i];
                    if (recursive)
                    {
                        showFile(child, newPrefix);
                    }
                    else
                    {
                        log(newPrefix + child.getName().getBaseName());
                    }
                }
            }
        }
    }

    /**
     * Writes the content of the file to Ant log.
     */
    private void logContent(final FileObject file, final String prefix)
        throws Exception
    {
        final InputStream instr = file.getContent().getInputStream();
        try
        {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(instr));
            while (true)
            {
                final String line = reader.readLine();
                if (line == null)
                {
                    break;
                }
                log(prefix + line);
            }
        }
        finally
        {
            instr.close();
        }
    }
}
