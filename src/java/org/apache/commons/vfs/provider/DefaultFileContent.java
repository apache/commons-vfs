/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

/**
 * The content of a file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.7 $ $Date: 2002/07/05 04:08:17 $
 */
public final class DefaultFileContent
    implements FileContent
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( DefaultFileContent.class );

    private static final int STATE_NONE = 0;
    private static final int STATE_READING = 1;
    private static final int STATE_WRITING = 2;

    private final AbstractFileObject file;
    private int state = STATE_NONE;
    private FileContentInputStream instr;
    private FileContentOutputStream outstr;

    public DefaultFileContent( final AbstractFileObject file )
    {
        this.file = file;
    }

    /**
     * Returns the file which this is the content of.
     */
    public FileObject getFile()
    {
        return file;
    }

    /**
     * Returns the size of the content (in bytes).
     */
    public long getSize() throws FileSystemException
    {
        if( file.isFolder() )
        {
            final String message = REZ.getString( "get-size-folder.error", file );
            throw new FileSystemException( message );
        }

        // Do some checking
        if ( !file.exists() )
        {
            final String message = REZ.getString( "get-size-no-exist.error", file );
            throw new FileSystemException( message );
        }
        if ( state == STATE_WRITING )
        {
            final String message = REZ.getString( "get-size-write.error", file );
            throw new FileSystemException( message );
        }

        try
        {
            // Get the size
            return file.doGetContentSize();
        }
        catch ( Exception exc )
        {
            final String message = REZ.getString( "get-size.error", file );
            throw new FileSystemException( message, exc );
        }
    }

    /**
     * Returns the last-modified timestamp.
     */
    public long getLastModifiedTime() throws FileSystemException
    {
        if( !file.exists() )
        {
            final String message = REZ.getString( "get-last-modified-no-exist.error", file );
            throw new FileSystemException( message );
        }
        return file.doGetLastModifiedTime();
    }

    /**
     * Sets the last-modified timestamp.
     */
    public void setLastModifiedTime( long modTime ) throws FileSystemException
    {
        if( !file.exists() )
        {
            final String message = REZ.getString( "set-last-modified-no-exist.error", file );
            throw new FileSystemException( message );
        }
        file.doSetLastModifiedTime( modTime );
    }

    /**
     * Gets the value of an attribute.
     */
    public Object getAttribute( String attrName ) throws FileSystemException
    {
        return file.doGetAttribute( attrName );
    }

    /**
     * Sets the value of an attribute.
     */
    public void setAttribute( String attrName, Object value ) throws FileSystemException
    {
        file.doSetAttribute( attrName, value );
    }

    /**
     * Returns the certificates used to sign this file.
     */
    public Certificate[] getCertificates() throws FileSystemException
    {
        if( !file.exists() )
        {
            final String message = REZ.getString( "get-certificates-no-exist.error", file );
            throw new FileSystemException( message );
        }
        return file.doGetCertificates();
    }

    /**
     * Returns an input stream for reading the content.
     */
    public InputStream getInputStream() throws FileSystemException
    {
        if( file.isFolder() )
        {
            final String message = REZ.getString( "read-folder.error", file );
            throw new FileSystemException( message );
        }
        if ( !file.exists() )
        {
            final String message = REZ.getString( "read-no-exist.error", file );
            throw new FileSystemException( message );
        }
        if ( state != STATE_NONE )
        {
            final String message = REZ.getString( "read-in-use.error", file );
            throw new FileSystemException( message );
        }

        // Get the raw input stream
        InputStream instr;
        try
        {
            instr = file.doGetInputStream();
        }
        catch ( Exception exc )
        {
            final String message = REZ.getString( "read.error", file );
            throw new FileSystemException( message, exc );
        }

        // TODO - reuse
        this.instr = new FileContentInputStream( instr );
        state = STATE_READING;
        return this.instr;
    }

    /**
     * Returns an output stream for writing the content.
     */
    public OutputStream getOutputStream() throws FileSystemException
    {
        if( file.isFolder() )
        {
            final String message = REZ.getString( "write-folder.error", file );
            throw new FileSystemException( message );
        }
        if ( state != STATE_NONE )
        {
            final String message = REZ.getString( "write-in-use.error", file );
            throw new FileSystemException( message );
        }

        // Get the raw output stream
        OutputStream outstr = file.getOutputStream();

        // Create wrapper
        // TODO - reuse
        this.outstr = new FileContentOutputStream( outstr );
        state = STATE_WRITING;
        return this.outstr;
    }

    /**
     * Closes all resources used by the content, including all streams, readers
     * and writers.
     */
    public void close() throws FileSystemException
    {

        try
        {
            // Close the input stream
            if ( instr != null )
            {
                try
                {
                    instr.close();
                }
                catch ( IOException ioe )
                {
                    final String message = REZ.getString( "close-instr.error" );
                    throw new FileSystemException( message, ioe );
                }
            }

            // Close the output stream
            if ( outstr != null )
            {
                try
                {
                    outstr.close();
                }
                catch ( IOException ioe )
                {
                    final String message = REZ.getString( "close-outstr.error" );
                    throw new FileSystemException( message, ioe );
                }
            }
        }
        finally
        {
            state = STATE_NONE;
        }
    }

    /**
     * Handles the end of input stream.
     */
    private void endInput() throws Exception
    {
        instr = null;
        state = STATE_NONE;
        file.doEndInput();
    }

    /**
     * Handles the end of output stream.
     */
    private void endOutput() throws Exception
    {
        outstr = null;
        state = STATE_NONE;
        file.endOutput();
    }

    /**
     * An input stream for reading content.  Provides buffering, and
     * end-of-stream monitoring.
     */
    private final class FileContentInputStream extends BufferedInputStream
    {
        boolean _finished;

        FileContentInputStream( InputStream instr )
        {
            super( instr );
        }

        /**
         * Reads a character.
         */
        public int read() throws IOException
        {
            if ( _finished )
            {
                return -1;
            }

            int ch = super.read();
            if ( ch != -1 )
            {
                return ch;
            }

            // End-of-stream
            close();
            return -1;
        }

        /**
         * Reads bytes from this input stream.error occurs.
         */
        public int read( byte[] buffer, int offset, int length )
            throws IOException
        {
            if ( _finished )
            {
                return -1;
            }

            int nread = super.read( buffer, offset, length );
            if ( nread != -1 )
            {
                return nread;
            }

            // End-of-stream
            close();
            return -1;
        }

        /**
         * Closes this input stream.
         */
        public void close() throws IOException
        {
            if ( _finished )
            {
                return;
            }

            // Close the stream
            IOException exc = null;
            try
            {
                super.close();
            }
            catch ( IOException e )
            {
                exc = e;
            }

            // Notify the file object
            try
            {
                endInput();
            }
            catch ( Exception e )
            {
                exc = new IOException( e.getMessage() );
            }

            _finished = true;

            if ( exc != null )
            {
                throw exc;
            }
        }
    }

    /**
     * An output stream for writing content.
     */
    private final class FileContentOutputStream
        extends BufferedOutputStream
    {
        FileContentOutputStream( OutputStream outstr )
        {
            super( outstr );
        }

        /**
         * Closes this output stream.
         */
        public void close() throws IOException
        {
            IOException exc = null;

            // Close the output stream
            try
            {
                super.close();
            }
            catch ( IOException e )
            {
                exc = e;
            }

            // Notify of end of output
            try
            {
                endOutput();
            }
            catch ( Exception e )
            {
                exc = new IOException( e.getMessage() );
            }

            if ( exc != null )
            {
                throw exc;
            }
        }
    }

}
