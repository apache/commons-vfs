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
package org.apache.commons.vfs.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileUtil;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.Selectors;

/**
 * A simple command-line shell for performing file operations.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2003/02/21 05:15:51 $
 */
public class Shell
{
    private final FileSystemManager mgr;
    private FileObject cwd;
    private BufferedReader reader;

    public static void main( final String[] args )
    {
        try
        {
            ( new Shell() ).go();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.exit( 1 );
        }
        System.exit( 0 );
    }

    private Shell() throws FileSystemException
    {
        mgr = VFS.getManager();
        cwd = mgr.resolveFile( System.getProperty( "user.dir" ) );
        reader = new BufferedReader( new InputStreamReader( System.in ) );
    }

    private void go() throws Exception
    {
        while ( true )
        {
            final String[] cmd = nextCommand();
            if ( cmd == null )
            {
                return;
            }
            if ( cmd.length == 0 )
            {
                continue;
            }
            final String cmdName = cmd[ 0 ];
            if ( cmdName.equalsIgnoreCase( "exit" ) )
            {
                return;
            }
            try
            {
                handleCommand( cmd );
            }
            catch ( final Exception e )
            {
                System.err.println( "Command failed:" );
                e.printStackTrace( System.err );
            }
        }
    }

    /** Handles a command. */
    private void handleCommand( final String[] cmd ) throws Exception
    {
        final String cmdName = cmd[ 0 ];
        if ( cmdName.equalsIgnoreCase( "cat" ) )
        {
            cat( cmd );
        }
        else if ( cmdName.equalsIgnoreCase( "cd" ) )
        {
            cd( cmd );
        }
        else if ( cmdName.equalsIgnoreCase( "cp" ) )
        {
            cp( cmd );
        }
        else if ( cmdName.equalsIgnoreCase( "help" ) )
        {
            help();
        }
        else if ( cmdName.equalsIgnoreCase( "ls" ) )
        {
            ls( cmd );
        }
        else if ( cmdName.equalsIgnoreCase( "pwd" ) )
        {
            pwd();
        }
        else if ( cmdName.equalsIgnoreCase( "rm" ) )
        {
            rm( cmd );
        }
        else
        {
            System.err.println( "Unknown command \"" + cmdName + "\"." );
        }
    }

    /** Does a 'help' command. */
    private void help()
    {
        System.out.println( "Commands:" );
        System.out.println( "cat <file>         Displays the contents of a file." );
        System.out.println( "cd [folder]        Changes current folder." );
        System.out.println( "cp <src> <dest>    Copies a file or folder." );
        System.out.println( "help               Shows this message." );
        System.out.println( "ls [-R] [folder]   Lists contents of a folder." );
        System.out.println( "pwd                Displays current folder." );
        System.out.println( "rm <path>          Deletes a file or folder." );
    }

    /** Does an 'rm' command. */
    private void rm( final String[] cmd ) throws Exception
    {
        if ( cmd.length < 2 )
        {
            throw new Exception( "USAGE: rm <path>" );
        }

        final FileObject file = mgr.resolveFile( cwd, cmd[ 1 ] );
        file.delete( Selectors.SELECT_SELF );
    }

    /** Does a 'cp' command. */
    private void cp( final String[] cmd ) throws Exception
    {
        if ( cmd.length < 3 )
        {
            throw new Exception( "USAGE: cp <src> <dest>" );
        }

        final FileObject src = mgr.resolveFile( cwd, cmd[ 1 ] );
        FileObject dest = mgr.resolveFile( cwd, cmd[ 2 ] );
        if ( dest.exists() && dest.getType() == FileType.FOLDER )
        {
            dest = dest.resolveFile( src.getName().getBaseName() );
        }

        dest.copyFrom( src, Selectors.SELECT_ALL );
    }

    /** Does a 'cat' command. */
    private void cat( final String[] cmd ) throws Exception
    {
        if ( cmd.length < 2 )
        {
            throw new Exception( "USAGE: cat <path>" );
        }

        // Locate the file
        final FileObject file = mgr.resolveFile( cwd, cmd[ 1 ] );

        // Dump the contents to System.out
        FileUtil.writeContent( file, System.out );
        System.out.println();
    }

    /** Does a 'pwd' command. */
    private void pwd()
    {
        System.out.println( "Current folder is " + cwd.getName() );
    }

    /** Does a 'cd' command. */
    private void cd( final String[] cmd ) throws Exception
    {
        final String path;
        if ( cmd.length > 1 )
        {
            path = cmd[ 1 ];
        }
        else
        {
            path = System.getProperty( "user.home" );
        }

        // Locate and validate the folder
        cwd = mgr.resolveFile( cwd, path );
        System.out.println( "Current folder is " + cwd.getName() );
    }

    /** Does an 'ls' command. */
    private void ls( final String[] cmd ) throws FileSystemException
    {
        int pos = 1;
        final boolean recursive;
        if ( cmd.length > pos && cmd[ pos ].equals( "-R" ) )
        {
            recursive = true;
            pos++;
        }
        else
        {
            recursive = false;
        }

        final FileObject dir;
        if ( cmd.length > pos )
        {
            dir = mgr.resolveFile( cwd, cmd[ pos ] );
        }
        else
        {
            dir = cwd;
        }

        // List the contents
        System.out.println( "Contents of " + dir.getName() );
        listChildren( dir, recursive, "" );
    }

    /** Lists the children of a folder. */
    private void listChildren( final FileObject dir,
                               final boolean recursive,
                               final String prefix )
        throws FileSystemException
    {
        final FileObject[] children = dir.getChildren();
        for ( int i = 0; i < children.length; i++ )
        {
            final FileObject child = children[ i ];
            System.out.print( prefix );
            System.out.print( child.getName().getBaseName() );
            if ( child.getType() == FileType.FOLDER )
            {
                System.out.println( "/" );
                if ( recursive )
                {
                    listChildren( child, recursive, prefix + "    " );
                }
            }
            else
            {
                System.out.println();
            }
        }
    }

    /** Returns the next command, split into tokens. */
    private String[] nextCommand() throws IOException
    {
        System.out.print( "> " );
        final String line = reader.readLine();
        if ( line == null )
        {
            return null;
        }
        final ArrayList cmd = new ArrayList();
        final StringTokenizer tokens = new StringTokenizer( line );
        while ( tokens.hasMoreTokens() )
        {
            cmd.add( tokens.nextToken() );
        }
        return (String[])cmd.toArray( new String[ cmd.size() ] );
    }
}
