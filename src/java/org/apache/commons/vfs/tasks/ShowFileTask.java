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
 * An Ant task that writes the details of a file to Ant's log.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.4 $ $Date: 2002/10/23 13:09:45 $
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
