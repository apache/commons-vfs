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
        if ( file.isFolder() )
        {
            throw new FileSystemException( "vfs.provider/get-size-folder.error", file );
        }

        // Do some checking
        if ( !file.exists() )
        {
            throw new FileSystemException( "vfs.provider/get-size-no-exist.error", file );
        }
        if ( state == STATE_WRITING )
        {
            throw new FileSystemException( "vfs.provider/get-size-write.error", file );
        }

        try
        {
            // Get the size
            return file.doGetContentSize();
        }
        catch ( Exception exc )
        {
            throw new FileSystemException( "vfs.provider/get-size.error", new Object[]{file}, exc );
        }
    }

    /**
     * Returns the last-modified timestamp.
     */
    public long getLastModifiedTime() throws FileSystemException
    {
        if ( !file.exists() )
        {
            throw new FileSystemException( "vfs.provider/get-last-modified-no-exist.error", file );
        }
        return file.doGetLastModifiedTime();
    }

    /**
     * Sets the last-modified timestamp.
     */
    public void setLastModifiedTime( long modTime ) throws FileSystemException
    {
        if ( !file.exists() )
        {
            throw new FileSystemException( "vfs.provider/set-last-modified-no-exist.error", file );
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
        if ( !file.exists() )
        {
            throw new FileSystemException( "vfs.provider/get-certificates-no-exist.error", file );
        }
        return file.doGetCertificates();
    }

    /**
     * Returns an input stream for reading the content.
     */
    public InputStream getInputStream() throws FileSystemException
    {
        if ( file.isFolder() )
        {
            throw new FileSystemException( "vfs.provider/read-folder.error", file );
        }
        if ( !file.exists() )
        {
            throw new FileSystemException( "vfs.provider/read-no-exist.error", file );
        }
        if ( state != STATE_NONE )
        {
            throw new FileSystemException( "vfs.provider/read-in-use.error", file );
        }

        // Get the raw input stream
        InputStream instr;
        try
        {
            instr = file.doGetInputStream();
        }
        catch ( Exception exc )
        {
            throw new FileSystemException( "vfs.provider/read.error", new Object[]{file}, exc );
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
        if ( file.isFolder() )
        {
            throw new FileSystemException( "vfs.provider/write-folder.error", file );
        }
        if ( state != STATE_NONE )
        {
            throw new FileSystemException( "vfs.provider/write-in-use.error", file );
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
                    throw new FileSystemException( "vfs.provider/close-instr.error", null, ioe );
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
                    throw new FileSystemException( "vfs.provider/close-outstr.error", null, ioe );
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
        private boolean _finished;

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
