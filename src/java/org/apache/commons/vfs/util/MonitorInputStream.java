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
package org.apache.commons.vfs.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * An InputStream that provides buffering and end-of-stream monitoring.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2003/02/13 04:28:45 $
 */
public class MonitorInputStream
    extends BufferedInputStream
{
    private boolean finished;

    public MonitorInputStream( final InputStream in )
    {
        super( in );
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

        final int ch = super.read();
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
    public int read( final byte[] buffer, final int offset, final int length )
        throws IOException
    {
        if ( finished )
        {
            return -1;
        }

        final int nread = super.read( buffer, offset, length );
        if ( nread != -1 )
        {
            return nread;
        }

        // End-of-stream
        close();
        return -1;
    }

    /**
     * Closes this input stream and releases any system resources
     * associated with the stream.
     */
    public void close() throws IOException
    {
        if ( finished )
        {
            return;
        }

        // Close the stream
        IOException exc = null;
        try
        {
            super.close();
        }
        catch ( final IOException ioe )
        {
            exc = ioe;
        }

        // Notify that the stream has been closed
        try
        {
            onClose();
        }
        catch ( final IOException ioe )
        {
            exc = ioe;
        }

        finished = true;
        if ( exc != null )
        {
            throw exc;
        }
    }

    /**
     * Called after the stream has been closed.  This implementation does
     * nothing.
     */
    protected void onClose() throws IOException
    {
    }
}
