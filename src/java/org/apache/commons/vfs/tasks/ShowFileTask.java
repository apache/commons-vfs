/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.tasks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.tools.ant.BuildException;

/**
 * An Ant task, which writes the details of a file to Ant's log.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/10/23 10:58:12 $
 */
public class ShowFileTask
    extends VfsTask
{
    private String url;
    private boolean showContent;

    /**
     * The URL of the file to display.
     */
    public void setFile( final String url )
    {
        this.url = url;
    }

    /**
     * Shows the content.  Assumes the content is text, encoded using the
     * platform's default encoding.
     */
    public void setShowContent( final boolean showContent )
    {
        this.showContent = showContent;
    }

    /**
     * Executes the task.
     */
    public void execute() throws BuildException
    {
        try
        {
            // Lookup the file
            final FileObject file = resolveFile( url );

            // Write details
            log( "URI: " + file.getName().getURI() );
            log( "Exists: " + file.exists() );
            if ( !file.exists() )
            {
                return;
            }
            log( "Type: " + file.getType().getName() );
            if ( file.getType() == FileType.FILE )
            {
                final FileContent content = file.getContent();
                log( "Content-Length: " + content.getSize() );
                log( "Last-Modified" + new Date( content.getLastModifiedTime() ) );
                if ( showContent )
                {
                    log( "Content:" );
                    logContent( file );
                }
            }
            else
            {
                final FileObject[] children = file.getChildren();
                log( "Child Count: " + children.length );
                log( "Children:" );
                for ( int i = 0; i < children.length; i++ )
                {
                    FileObject child = children[ i ];
                    log( "    " + child.getName().getBaseName() );
                }
            }
        }
        catch ( final Exception e )
        {
            throw new BuildException( e );
        }
    }

    /**
     * Writes the content of the file to Ant log.
     */
    private void logContent( FileObject file ) throws Exception
    {
        final InputStream instr = file.getContent().getInputStream();
        try
        {
            final BufferedReader reader = new BufferedReader( new InputStreamReader( instr ) );
            while ( true )
            {
                final String line = reader.readLine();
                if ( line == null )
                {
                    break;
                }
                log( line );
            }
        }
        finally
        {
            instr.close();
        }
    }
}
