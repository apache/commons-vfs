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
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileContentInfo;
import org.apache.commons.vfs.FileContentInfoFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.util.MonitorInputStream;
import org.apache.commons.vfs.util.MonitorOutputStream;
import org.apache.commons.vfs.util.MonitorRandomAccessContent;
import org.apache.commons.vfs.util.RandomAccessMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * The content of a file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public final class DefaultFileContent implements FileContent
{
    /*
    static final int STATE_NONE = 0;
    static final int STATE_READING = 1;
    static final int STATE_WRITING = 2;
    static final int STATE_RANDOM_ACCESS = 3;
    */

    static final int STATE_CLOSED = 0;
    static final int STATE_OPENED = 1;

    private final AbstractFileObject file;
    private Map<String, Object> attrs;
    private Map<String, Object> roAttrs;
    private FileContentInfo fileContentInfo;
    private final FileContentInfoFactory fileContentInfoFactory;

    private final ThreadLocal<FileContentThreadData> threadData = new ThreadLocal<FileContentThreadData>();
    private boolean resetAttributes;

    /**
     * open streams counter for this file
     */
    private int openStreams;

    public DefaultFileContent(final AbstractFileObject file, final FileContentInfoFactory fileContentInfoFactory)
    {
        this.file = file;
        this.fileContentInfoFactory = fileContentInfoFactory;
    }

    private FileContentThreadData getThreadData()
    {
        FileContentThreadData data = this.threadData.get();
        if (data == null)
        {
            data = new FileContentThreadData();
            this.threadData.set(data);
        }
        return data;
    }

    void streamOpened()
    {
        synchronized (this)
        {
            openStreams++;
        }
        ((AbstractFileSystem) file.getFileSystem()).streamOpened();
    }

    void streamClosed()
    {
        synchronized (this)
        {
            if (openStreams > 0)
            {
                openStreams--;
                if (openStreams < 1)
                {
                    file.notifyAllStreamsClosed();
                }
            }
        }
        ((AbstractFileSystem) file.getFileSystem()).streamClosed();
    }

    /**
     * Returns the file that this is the content of.
     * @return the FileObject.
     */
    public FileObject getFile()
    {
        return file;
    }

    /**
     * Returns the size of the content (in bytes).
     * @return The size of the content (in bytes).
     * @throws FileSystemException if an error occurs.
     */
    public long getSize() throws FileSystemException
    {
        // Do some checking
        if (!file.getType().hasContent())
        {
            throw new FileSystemException("vfs.provider/get-size-not-file.error", file);
        }
        /*
        if (getThreadData().getState() == STATE_WRITING || getThreadData().getState() == STATE_RANDOM_ACCESS)
        {
            throw new FileSystemException("vfs.provider/get-size-write.error", file);
        }
        */

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
     * @return The last modified timestamp.
     * @throws FileSystemException if an error occurs.
     */
    public long getLastModifiedTime() throws FileSystemException
    {
        /*
        if (getThreadData().getState() == STATE_WRITING || getThreadData().getState() == STATE_RANDOM_ACCESS)
        {
            throw new FileSystemException("vfs.provider/get-last-modified-writing.error", file);
        }
        */
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
     * @param modTime The last modified timestamp.
     * @throws FileSystemException if an error occurs.
     */
    public void setLastModifiedTime(final long modTime) throws FileSystemException
    {
        /*
        if (getThreadData().getState() == STATE_WRITING || getThreadData().getState() == STATE_RANDOM_ACCESS)
        {
            throw new FileSystemException("vfs.provider/set-last-modified-writing.error", file);
        }
        */
        if (!file.getType().hasAttributes())
        {
            throw new FileSystemException("vfs.provider/set-last-modified-no-exist.error", file);
        }
        try
        {
            if (!file.doSetLastModTime(modTime))
            {
                throw new FileSystemException("vfs.provider/set-last-modified.error", file);
            }
        }
        catch (final Exception e)
        {
            throw new FileSystemException("vfs.provider/set-last-modified.error", file, e);
        }
    }

    /**
     * Checks if an attribute exists.
     * @param attrName The name of the attribute to check.
     * @return true if the attribute is associated with the file.
     * @throws FileSystemException if an error occurs.
     */
    public boolean hasAttribute(final String attrName) throws FileSystemException
    {
        if (!file.getType().hasAttributes())
        {
            throw new FileSystemException("vfs.provider/exists-attributes-no-exist.error", file);
        }
        getAttributes();
        return attrs.containsKey(attrName);
    }

    /**
     * Returns a read-only map of this file's attributes.
     * @return a Map of the file's attributes.
     * @throws FileSystemException if an error occurs.
     */
    public Map<String, Object> getAttributes() throws FileSystemException
    {
        if (!file.getType().hasAttributes())
        {
            throw new FileSystemException("vfs.provider/get-attributes-no-exist.error", file);
        }
        if (resetAttributes || roAttrs == null)
        {
            try
            {
                synchronized (this)
                {
                    attrs = file.doGetAttributes();
                    roAttrs = Collections.unmodifiableMap(attrs);
                    resetAttributes = false;
                }
            }
            catch (final Exception e)
            {
                throw new FileSystemException("vfs.provider/get-attributes.error", file, e);
            }
        }
        return roAttrs;
    }

    /**
     * Used internally to flag situations where the file attributes should be
     * reretrieved.
     */
    public void resetAttributes()
    {
        resetAttributes = true;
    }

    /**
     * Lists the attributes of this file.
     * @return An array of attribute names.
     * @throws FileSystemException if an error occurs.
     */
    public String[] getAttributeNames() throws FileSystemException
    {
        getAttributes();
        final Set<String> names = attrs.keySet();
        return names.toArray(new String[names.size()]);
    }

    /**
     * Gets the value of an attribute.
     * @param attrName The attribute name.
     * @return The value of the attribute or null.
     * @throws FileSystemException if an error occurs.
     */
    public Object getAttribute(final String attrName)
        throws FileSystemException
    {
        getAttributes();
        return attrs.get(attrName);
    }

    /**
     * Sets the value of an attribute.
     * @param attrName The name of the attribute to add.
     * @param value The value of the attribute.
     * @throws FileSystemException if an error occurs.
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
     * Removes an attribute.
     * @param attrName The name of the attribute to remove.
     * @throws FileSystemException if an error occurs.
     */
    public void removeAttribute(final String attrName) throws FileSystemException
    {
        if (!file.getType().hasAttributes())
        {
            throw new FileSystemException("vfs.provider/remove-attribute-no-exist.error", file);
        }

        try
        {
            file.doRemoveAttribute(attrName);
        }
        catch (final Exception e)
        {
            throw new FileSystemException("vfs.provider/remove-attribute.error", new Object[]{attrName, file}, e);
        }

        if (attrs != null)
        {
            attrs.remove(attrName);
        }
    }

    /**
     * Returns the certificates used to sign this file.
     * @return An array of Certificates.
     * @throws FileSystemException if an error occurs.
     */
    public Certificate[] getCertificates() throws FileSystemException
    {
        if (!file.exists())
        {
            throw new FileSystemException("vfs.provider/get-certificates-no-exist.error", file);
        }
        /*
        if (getThreadData().getState() == STATE_WRITING || getThreadData().getState() == STATE_RANDOM_ACCESS)
        {
            throw new FileSystemException("vfs.provider/get-certificates-writing.error", file);
        }
        */

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
     * @return The InputStream
     * @throws FileSystemException if an error occurs.
     */
    public InputStream getInputStream() throws FileSystemException
    {
        /*
        if (getThreadData().getState() == STATE_WRITING || getThreadData().getState() == STATE_RANDOM_ACCESS)
        {
            throw new FileSystemException("vfs.provider/read-in-use.error", file);
        }
        */

        // Get the raw input stream
        final InputStream instr = file.getInputStream();

        final InputStream wrappedInstr = new FileContentInputStream(file, instr);

        this.getThreadData().addInstr(wrappedInstr);
        streamOpened();

        // setState(STATE_OPENED);
        return wrappedInstr;
    }

    /**
     * Returns an input/output stream to use to read and write the content of the file in an
     * random manner.
     * @param mode The RandomAccessMode.
     * @return A RandomAccessContent object to access the file.
     * @throws FileSystemException if an error occurs.
     */
    public RandomAccessContent getRandomAccessContent(final RandomAccessMode mode) throws FileSystemException
    {
        /*
        if (getThreadData().getState() != STATE_NONE)
        {
            throw new FileSystemException("vfs.provider/read-in-use.error", file);
        }
        */

        // Get the content
        final RandomAccessContent rastr = file.getRandomAccessContent(mode);

        FileRandomAccessContent rac = new FileRandomAccessContent(file, rastr);
        this.getThreadData().addRastr(rac);
        streamOpened();

        // setState(STATE_OPENED);
        return rac;
    }

    /**
     * Returns an output stream for writing the content.
     * @return The OutputStream for the file.
     * @throws FileSystemException if an error occurs.
     */
    public OutputStream getOutputStream() throws FileSystemException
    {
        return getOutputStream(false);
    }

    /**
     * Returns an output stream for writing the content in append mode.
     * @param bAppend true if the data written should be appended.
     * @return The OutputStream for the file.
     * @throws FileSystemException if an error occurs.
     */
    public OutputStream getOutputStream(boolean bAppend) throws FileSystemException
    {
        /*
        if (getThreadData().getState() != STATE_NONE)
        */
        if (this.getThreadData().getOutstr() != null)
        {
            throw new FileSystemException("vfs.provider/write-in-use.error", file);
        }

        // Get the raw output stream
        final OutputStream outstr = file.getOutputStream(bAppend);

        // Create wrapper
        this.getThreadData().setOutstr(new FileContentOutputStream(file, outstr));
        streamOpened();

        // setState(STATE_OPENED);
        return this.getThreadData().getOutstr();
    }

    /**
     * Closes all resources used by the content, including all streams, readers
     * and writers.
     * @throws FileSystemException if an error occurs.
     */
    public void close() throws FileSystemException
    {
        try
        {
            // Close the input stream
            while (getThreadData().getInstrsSize() > 0)
            {
                final FileContentInputStream instr = (FileContentInputStream) getThreadData().removeInstr(0);
                instr.close();
            }

            // Close the randomAccess stream
            while (getThreadData().getRastrsSize() > 0)
            {
                final RandomAccessContent ra = (RandomAccessContent) getThreadData().removeRastr(0);
                try
                {
                    ra.close();
                }
                catch (IOException e)
                {
                    throw new FileSystemException(e);
                }
            }

            // Close the output stream
            if (this.getThreadData().getOutstr() != null)
            {
                this.getThreadData().closeOutstr();
            }
        }
        finally
        {
            threadData.set(null);
        }
    }

    /**
     * Handles the end of input stream.
     */
    private void endInput(final FileContentInputStream instr)
    {
        getThreadData().removeInstr(instr);
        streamClosed();
        /*
        if (!getThreadData().hasStreams())
        {
            setState(STATE_CLOSED);
        }
        */
    }

    /**
     * Handles the end of random access.
     */
    private void endRandomAccess(RandomAccessContent rac)
    {
        getThreadData().removeRastr(rac);
        streamClosed();
        // setState(STATE_CLOSED);
    }

    /**
     * Handles the end of output stream.
     */
    private void endOutput() throws Exception
    {
        streamClosed();

        this.getThreadData().setOutstr(null);
        // setState(STATE_CLOSED);

        file.endOutput();
    }

    /*
    private void setState(int state)
    {
        getThreadData().setState(state);
    }
    */

    /**
     * check if a input and/or output stream is open.<br />
     * This checks only the scope of the current thread.
     *
     * @return true if this is the case
     */
    public boolean isOpen()
    {
        // return getThreadData().getState() == STATE_OPENED;
        return getThreadData().hasStreams();
    }

    /**
     * check if a input and/or output stream is open.<br />
     * This checks all threads.
     *
     * @return true if this is the case
     */
    public boolean isOpenGlobal()
    {
        synchronized (this)
        {
            return openStreams > 0;
        }
    }

    /**
     * An input stream for reading content.  Provides buffering, and
     * end-of-stream monitoring.
     */
    private final class FileContentInputStream
        extends MonitorInputStream
    {
        // avoid gc
        private final FileObject _file;

        FileContentInputStream(final FileObject file, final InputStream instr)
        {
            super(instr);
            this._file = file;
        }

        /**
         * Closes this input stream.
         */
        @Override
        public void close() throws FileSystemException
        {
            try
            {
                super.close();
            }
            catch (final IOException e)
            {
                throw new FileSystemException("vfs.provider/close-instr.error", _file, e);
            }
        }

        /**
         * Called after the stream has been closed.
         */
        @Override
        protected void onClose() throws IOException
        {
            try
            {
                super.onClose();
            }
            finally
            {
                endInput(this);
            }
        }
    }

    /**
     * An input/output stream for reading/writing content on random positions
     */
    private final class FileRandomAccessContent extends MonitorRandomAccessContent
    {
        // avoid gc
        @SuppressWarnings("unused")
        private final FileObject _file;
        @SuppressWarnings("unused")
        private final RandomAccessContent content;

        FileRandomAccessContent(final FileObject file, final RandomAccessContent content)
        {
            super(content);
            this._file = file;
            this.content = content;
        }

        /**
         * Called after the stream has been closed.
         */
        @Override
        protected void onClose() throws IOException
        {
            try
            {
                super.onClose();
            }
            finally
            {
                endRandomAccess(this);
            }
        }
    }

    /**
     * An output stream for writing content.
     */
    final class FileContentOutputStream extends MonitorOutputStream
    {
        // avoid gc
        private final FileObject _file;

        FileContentOutputStream(final FileObject file, final OutputStream outstr)
        {
            super(outstr);
            this._file = file;
        }

        /**
         * Closes this output stream.
         */
        @Override
        public void close() throws FileSystemException
        {
            try
            {
                super.close();
            }
            catch (final IOException e)
            {
                throw new FileSystemException("vfs.provider/close-outstr.error", _file, e);
            }
        }

        /**
         * Called after this stream is closed.
         */
        @Override
        protected void onClose() throws IOException
        {
            try
            {
                super.onClose();
            }
            finally
            {
                try
                {
                    endOutput();
                }
                catch (Exception e)
                {
                    throw new FileSystemException("vfs.provider/close-outstr.error", _file, e);
                }
            }
        }
    }

    /**
     * get the content info. e.g. content-type, content-encoding
     * @return The FileContentInfo.
     * @throws FileSystemException if an error occurs.
     */
    public FileContentInfo getContentInfo() throws FileSystemException
    {
        if (fileContentInfo == null)
        {
            fileContentInfo = fileContentInfoFactory.create(this);
        }

        return fileContentInfo;
    }
}
