/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.util.MonitorInputStream;
import org.apache.commons.vfs.util.MonitorOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
    private final ArrayList instrs = new ArrayList();
    private FileContentOutputStream outstr;
    private Map attrs;
    private Map roAttrs;

    public DefaultFileContent(final AbstractFileObject file)
    {
        this.file = file;
    }

    /**
     * Returns the file that this is the content of.
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
        // Do some checking
        if (!file.getType().hasContent())
        {
            throw new FileSystemException("vfs.provider/get-size-not-file.error", file);
        }
        if (state == STATE_WRITING)
        {
            throw new FileSystemException("vfs.provider/get-size-write.error", file);
        }

        try
        {
            // Get the size
            return file.doGetContentSize();
        }
        catch (final Exception exc)
        {
            throw new FileSystemException("vfs.provider/get-size.error", new Object[]{file}, exc);
        }
    }

    /**
     * Returns the last-modified timestamp.
     */
    public long getLastModifiedTime() throws FileSystemException
    {
        if (state == STATE_WRITING)
        {
            throw new FileSystemException("vfs.provider/get-last-modified-writing.error", file);
        }
        if (!file.getType().hasAttributes())
        {
            throw new FileSystemException("vfs.provider/get-last-modified-no-exist.error", file);
        }
        try
        {
            return file.doGetLastModifiedTime();
        }
        catch (final Exception e)
        {
            throw new FileSystemException("vfs.provider/get-last-modified.error", file, e);
        }
    }

    /**
     * Sets the last-modified timestamp.
     */
    public void setLastModifiedTime(final long modTime) throws FileSystemException
    {
        if (state == STATE_WRITING)
        {
            throw new FileSystemException("vfs.provider/set-last-modified-writing.error", file);
        }
        if (!file.getType().hasAttributes())
        {
            throw new FileSystemException("vfs.provider/set-last-modified-no-exist.error", file);
        }
        try
        {
            file.doSetLastModifiedTime(modTime);
        }
        catch (final Exception e)
        {
            throw new FileSystemException("vfs.provider/set-last-modified.error", file, e);
        }
    }

    /**
     * Returns a read-only map of this file's attributes.
     */
    public Map getAttributes() throws FileSystemException
    {
        if (!file.getType().hasAttributes())
        {
            throw new FileSystemException("vfs.provider/get-attributes-no-exist.error", file);
        }
        if (roAttrs == null)
        {
            try
            {
                attrs = file.doGetAttributes();
                roAttrs = Collections.unmodifiableMap(attrs);
            }
            catch (final Exception e)
            {
                throw new FileSystemException("vfs.provider/get-attributes.error", file, e);
            }
        }
        return roAttrs;
    }

    /**
     * Lists the attributes of this file.
     */
    public String[] getAttributeNames() throws FileSystemException
    {
        getAttributes();
        final Set names = attrs.keySet();
        return (String[]) names.toArray(new String[names.size()]);
    }

    /**
     * Gets the value of an attribute.
     */
    public Object getAttribute(final String attrName)
        throws FileSystemException
    {
        getAttributes();
        return attrs.get(attrName.toLowerCase());
    }

    /**
     * Sets the value of an attribute.
     */
    public void setAttribute(final String attrName, final Object value)
        throws FileSystemException
    {
        if (!file.getType().hasAttributes())
        {
            throw new FileSystemException("vfs.provider/set-attribute-no-exist.error", new Object[]{attrName, file});
        }
        try
        {
            file.doSetAttribute(attrName, value);
        }
        catch (final Exception e)
        {
            throw new FileSystemException("vfs.provider/set-attribute.error", new Object[]{attrName, file}, e);
        }

        if (attrs != null)
        {
            attrs.put(attrName, value);
        }
    }

    /**
     * Returns the certificates used to sign this file.
     */
    public Certificate[] getCertificates() throws FileSystemException
    {
        if (!file.exists())
        {
            throw new FileSystemException("vfs.provider/get-certificates-no-exist.error", file);
        }
        if (state == STATE_WRITING)
        {
            throw new FileSystemException("vfs.provider/get-certificates-writing.error", file);
        }

        try
        {
            final Certificate[] certs = file.doGetCertificates();
            if (certs != null)
            {
                return certs;
            }
            else
            {
                return new Certificate[0];
            }
        }
        catch (final Exception e)
        {
            throw new FileSystemException("vfs.provider/get-certificates.error", file, e);
        }
    }

    /**
     * Returns an input stream for reading the content.
     */
    public InputStream getInputStream() throws FileSystemException
    {
        if (state == STATE_WRITING)
        {
            throw new FileSystemException("vfs.provider/read-in-use.error", file);
        }

        // Get the raw input stream
        final InputStream instr = file.getInputStream();
        final InputStream wrappedInstr = new FileContentInputStream(instr);
        this.instrs.add(wrappedInstr);
        state = STATE_READING;
        return wrappedInstr;
    }

    /**
     * Returns an output stream for writing the content.
     */
    public OutputStream getOutputStream() throws FileSystemException
    {
        return getOutputStream(false);
    }

    /**
     * Returns an output stream for writing the content in append mode.
     */
    public OutputStream getOutputStream(boolean bAppend) throws FileSystemException
    {
        if (state != STATE_NONE)
        {
            throw new FileSystemException("vfs.provider/write-in-use.error", file);
        }

        // Get the raw output stream
        final OutputStream outstr = file.getOutputStream(bAppend);

        // Create wrapper
        this.outstr = new FileContentOutputStream(outstr);
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
            while (instrs.size() > 0)
            {
                final FileContentInputStream instr = (FileContentInputStream) instrs.remove(0);
                instr.close();
            }

            // Close the output stream
            if (outstr != null)
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
    private void endInput(final FileContentInputStream instr)
    {
        instrs.remove(instr);
        if (instrs.size() == 0)
        {
            state = STATE_NONE;
        }
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
     * check if a input and/or output stream is open.
     *
     * @return true if this is the case
     */
    public boolean isOpen()
    {
        return state != STATE_NONE;
    }

    /**
     * An input stream for reading content.  Provides buffering, and
     * end-of-stream monitoring.
     */
    private final class FileContentInputStream
        extends MonitorInputStream
    {
        FileContentInputStream(final InputStream instr)
        {
            super(instr);
        }

        /**
         * Closes this input stream.
         */
        public void close() throws FileSystemException
        {
            try
            {
                super.close();
            }
            catch (final IOException e)
            {
                throw new FileSystemException("vfs.provider/close-instr.error", file, e);
            }
        }

        /**
         * Called after the stream has been closed.
         */
        protected void onClose() throws IOException
        {
            endInput(this);
        }
    }

    /**
     * An output stream for writing content.
     */
    private final class FileContentOutputStream
        extends MonitorOutputStream
    {
        FileContentOutputStream(final OutputStream outstr)
        {
            super(outstr);
        }

        /**
         * Closes this output stream.
         */
        public void close() throws FileSystemException
        {
            try
            {
                super.close();
            }
            catch (final FileSystemException e)
            {
                throw e;
            }
            catch (final IOException e)
            {
                throw new FileSystemException("vfs.provider/close-outstr.error", file, e);
            }
        }

        /**
         * Called after this stream is closed.
         */
        protected void onClose() throws IOException
        {
            try
            {
                endOutput();
            }
            catch (final Exception e)
            {
                throw new FileSystemException("vfs.provider/close-outstr.error", file, e);
            }
        }
    }

}
