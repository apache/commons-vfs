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
        try
        {
            return file.doGetLastModifiedTime();
        }
        catch ( final Exception e )
        {
            throw new FileSystemException( "vfs.provider/get-last-modified.error", file, e );
        }
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
        try
        {
            file.doSetLastModifiedTime( modTime );
        }
        catch ( final Exception e )
        {
            throw new FileSystemException( "vfs.provider/set-last-modified.error", file, e );
        }
    }

    /**
     * Gets the value of an attribute.
     */
    public Object getAttribute( final String attrName )
        throws FileSystemException
    {
        try
        {
            return file.doGetAttribute( attrName );
        }
        catch ( final Exception e )
        {
            throw new FileSystemException( "vfs.provider/get-attribute.error", new Object[]{attrName, file}, e );
        }
    }

    /**
     * Sets the value of an attribute.
     */
    public void setAttribute( final String attrName, final Object value )
        throws FileSystemException
    {
        try
        {
            file.doSetAttribute( attrName, value );
        }
        catch ( final Exception e )
        {
            throw new FileSystemException( "vfs.provider/set-attribute.error", new Object[]{attrName, file}, e );
        }
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
                instr.close();
            }

            // Close the output stream
            if ( outstr != null )
            {
                outstr.close();
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
        private boolean finished;

        FileContentInputStream( InputStream instr )
        {
            super( instr );
        }

        /**
         * Reads a character.
         */
        public int read() throws IOException
        {
            if ( finished )
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
            if ( finished )
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
        public void close() throws FileSystemException
        {
            if ( finished )
            {
                return;
            }

            // Close the stream
            FileSystemException exc = null;
            try
            {
                super.close();
            }
            catch ( IOException ioe )
            {
                exc = new FileSystemException( "vfs.provider/close-instr.error", file, ioe );
            }

            // Notify the file object
            try
            {
                endInput();
            }
            catch ( final Exception e )
            {
                exc = new FileSystemException( "vfs.provider/close-instr.error", file, e );
            }

            finished = true;

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
        public void close() throws FileSystemException
        {
            FileSystemException exc = null;

            // Close the output stream
            try
            {
                super.close();
            }
            catch ( final IOException e )
            {
                exc = new FileSystemException( "vfs.provider/close-outstr.error", file, e );
            }

            // Notify of end of output
            try
            {
                endOutput();
            }
            catch ( final Exception e )
            {
                exc = new FileSystemException( "vfs.provider/close-outstr.error", file, e );
            }

            if ( exc != null )
            {
                throw exc;
            }
        }
    }

}
