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
package org.apache.commons.vfs2.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.vfs2.FileSystemException;

/**
 * An OutputStream that provides buffering and end-of-stream monitoring.
 */
public class MonitorOutputStream
    extends BufferedOutputStream
{
    private boolean finished;

    public MonitorOutputStream(final OutputStream out)
    {
        super(out);
    }

    /**
     * Closes this output stream.
     * @throws IOException if an error occurs.
     */
    @Override
    public void close() throws IOException
    {
        if (finished)
        {
            return;
        }

        // Close the output stream
        IOException exc = null;
        try
        {
            super.close();
        }
        catch (final IOException ioe)
        {
            exc = ioe;
        }

        // Notify of end of output
        try
        {
            onClose();
        }
        catch (final IOException ioe)
        {
            exc = ioe;
        }

        finished = true;

        if (exc != null)
        {
            throw exc;
        }
    }


    /**
     * @param b The character to write.
     * @throws IOException if an error occurs.
     * @since 2.0
     */
    @Override
    public synchronized void write(int b) throws IOException
    {
        assertOpen();
        super.write(b);
    }

    /**
     * @param b The byte array.
     * @param off The offset into the array.
     * @param len The number of bytes to write.
     * @throws IOException if an error occurs.
     * @since 2.0
     */
    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException
    {
        assertOpen();
        super.write(b, off, len);
    }

    /**
     * @throws IOException if an error occurs.
     * @since 2.0
     */
    @Override
    public synchronized void flush() throws IOException
    {
        assertOpen();
        super.flush();
    }

    /**
     * @param b The byte array.
     * @throws IOException if an error occurs.
     * @since 2.0
     */
    @Override
    public void write(byte[] b) throws IOException
    {
        assertOpen();
        super.write(b);
    }

    /**
     * check if file is still open. <br />
     * This is a workaround for an oddity with Java's BufferedOutputStream where you can write to
     * even if the stream has been closed
     * @throws FileSystemException if an error occurs.
     * @since 2.0
     */
    protected void assertOpen() throws FileSystemException
    {
        if (finished)
        {
            throw new FileSystemException("vfs.provider/closed.error");
        }
    }

    /**
     * Called after this stream is closed.  This implementation does nothing.
     * @throws IOException if an error occurs.
     */
    // IOException is needed because subclasses may need to throw it
    protected void onClose() throws IOException
    {
    }
}
