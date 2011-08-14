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
package org.apache.commons.vfs2.provider.tar;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The TarInputStream reads a UNIX tar archive as an InputStream. methods are
 * provided to position at each successive entry in the archive, and the read
 * each entry as a normal input stream using read().
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @see TarInputStream
 * @see TarEntry
 */
class TarInputStream
    extends FilterInputStream
{
    private TarBuffer buffer;
    private TarEntry currEntry;
    private boolean debug;
    private int entryOffset;
    private long entrySize;
    private boolean hasHitEOF;
    private byte[] oneBuf;
    private byte[] readBuf;

    /**
     * Construct a TarInputStream using specified input
     * stream and default block and record sizes.
     *
     * @param input stream to create TarInputStream from
     * @see TarBuffer#DEFAULT_BLOCKSIZE
     * @see TarBuffer#DEFAULT_RECORDSIZE
     */
    TarInputStream(final InputStream input)
    {
        this(input, TarBuffer.DEFAULT_BLOCKSIZE, TarBuffer.DEFAULT_RECORDSIZE);
    }

    /**
     * Construct a TarInputStream using specified input
     * stream, block size and default record sizes.
     *
     * @param input     stream to create TarInputStream from
     * @param blockSize the block size to use
     * @see TarBuffer#DEFAULT_RECORDSIZE
     */
    TarInputStream(final InputStream input,
                   final int blockSize)
    {
        this(input, blockSize, TarBuffer.DEFAULT_RECORDSIZE);
    }

    /**
     * Construct a TarInputStream using specified input
     * stream, block size and record sizes.
     *
     * @param input      stream to create TarInputStream from
     * @param blockSize  the block size to use
     * @param recordSize the record size to use
     */
    TarInputStream(final InputStream input, final int blockSize, final int recordSize)
    {
        super(input);

        buffer = new TarBuffer(input, blockSize, recordSize);
        oneBuf = new byte[1];
    }

    /**
     * Sets the debugging flag.
     *
     * @param debug The new Debug value
     */
    public void setDebug(final boolean debug)
    {
        this.debug = debug;
        buffer.setDebug(debug);
    }

    /**
     * Get the next entry in this tar archive. This will skip over any remaining
     * data in the current entry, if there is one, and place the input stream at
     * the header of the next entry, and read the header and instantiate a new
     * TarEntry from the header bytes and return that entry. If there are no
     * more entries in the archive, null will be returned to indicate that the
     * end of the archive has been reached.
     *
     * @return The next TarEntry in the archive, or null.
     * @throws IOException Description of Exception
     */
    public TarEntry getNextEntry() throws IOException
    {
        if (hasHitEOF)
        {
            return null;
        }

        if (currEntry != null)
        {
            final long numToSkip = entrySize - entryOffset;

            if (debug)
            {
                final String message = "TarInputStream: SKIP currENTRY '" +
                    currEntry.getName() + "' SZ " + entrySize +
                    " OFF " + entryOffset + "  skipping " + numToSkip + " bytes";
                debug(message);
            }

            if (numToSkip > 0)
            {
                // Use our internal skip to move to the end of the current entry
                longSkip(numToSkip);
            }

            readBuf = null;
        }

        final byte[] headerBuf = buffer.readRecord();
        if (headerBuf == null)
        {
            if (debug)
            {
                debug("READ NULL RECORD");
            }
            hasHitEOF = true;
            currEntry = null;
        }
        else if (buffer.isEOFRecord(headerBuf))
        {
            if (debug)
            {
                debug("READ EOF RECORD");
            }
            hasHitEOF = true;
            currEntry = null;
        }
        else
        {
            currEntry = new TarEntry(headerBuf);

            if (!(headerBuf[257] == 'u' && headerBuf[258] == 's' &&
                headerBuf[259] == 't' && headerBuf[260] == 'a' &&
                headerBuf[261] == 'r'))
            {
                //Must be v7Format
            }

            if (debug)
            {
                final String message = "TarInputStream: SET CURRENTRY '" +
                    currEntry.getName() + "' size = " + currEntry.getSize();
                debug(message);
            }

            entryOffset = 0;

            entrySize = currEntry.getSize();
        }

        if (null != currEntry && currEntry.isGNULongNameEntry())
        {
            // read in the name
            final StringBuffer longName = new StringBuffer();
            final byte[] buffer = new byte[256];
            int length = 0;
            while ((length = read(buffer)) >= 0)
            {
                final String str = new String(buffer, 0, length);
                longName.append(str);
            }
            getNextEntry();

            // remove trailing null terminator
            if (longName.length() > 0
                && longName.charAt(longName.length() - 1) == 0)
            {
                longName.deleteCharAt(longName.length() - 1);
            }

            currEntry.setName(longName.toString());
        }

        return currEntry;
    }

    /**
     * Get the record size being used by this stream's TarBuffer.
     *
     * @return The TarBuffer record size.
     */
    public int getRecordSize()
    {
        return buffer.getRecordSize();
    }

    /**
     * Get the available data that can be read from the current entry in the
     * archive. This does not indicate how much data is left in the entire
     * archive, only in the current entry. This value is determined from the
     * entry's size header field and the amount of data already read from the
     * current entry.
     *
     * @return The number of available bytes for the current entry.
     * @throws IOException when an IO error causes operation to fail
     */
    @Override
    public int available() throws IOException
    {
        long remaining = entrySize - entryOffset;

        if (remaining > Integer.MAX_VALUE)
        {
            return Integer.MAX_VALUE;
        }

        return (int) remaining;
    }

    /**
     * Closes this stream. Calls the TarBuffer's close() method.
     *
     * @throws IOException when an IO error causes operation to fail
     */
    @Override
    public void close() throws IOException
    {
        buffer.close();
    }

    /**
     * Copies the contents of the current tar archive entry directly into an
     * output stream.
     *
     * @param output The OutputStream into which to write the entry's data.
     * @throws IOException when an IO error causes operation to fail
     */
    public void copyEntryContents(final OutputStream output) throws IOException
    {
        final byte[] buffer = new byte[32 * 1024];
        while (true)
        {
            final int numRead = read(buffer, 0, buffer.length);
            if (numRead == -1)
            {
                break;
            }

            output.write(buffer, 0, numRead);
        }
    }

    /**
     * Since we do not support marking just yet, we do nothing.
     *
     * @param markLimit The limit to mark.
     */
    @Override
    public void mark(int markLimit)
    {
    }

    /**
     * Since we do not support marking just yet, we return false.
     *
     * @return False.
     */
    @Override
    public boolean markSupported()
    {
        return false;
    }

    /**
     * Reads a byte from the current tar archive entry. This method simply calls
     * read( byte[], int, int ).
     *
     * @return The byte read, or -1 at EOF.
     * @throws IOException when an IO error causes operation to fail
     */
    @Override
    public int read() throws IOException
    {
        final int num = read(oneBuf, 0, 1);
        if (num == -1)
        {
            return num;
        }
        else
        {
            return oneBuf[0];
        }
    }

    /**
     * Reads bytes from the current tar archive entry. This method simply calls
     * read( byte[], int, int ).
     *
     * @param buffer The buffer into which to place bytes read.
     * @return The number of bytes read, or -1 at EOF.
     * @throws IOException when an IO error causes operation to fail
     */
    @Override
    public int read(final byte[] buffer) throws IOException
    {
        return read(buffer, 0, buffer.length);
    }

    /**
     * Reads bytes from the current tar archive entry. This method is aware of
     * the boundaries of the current entry in the archive and will deal with
     * them as if they were this stream's start and EOF.
     *
     * @param buffer The buffer into which to place bytes read.
     * @param offset The offset at which to place bytes read.
     * @param count  The number of bytes to read.
     * @return The number of bytes read, or -1 at EOF.
     * @throws IOException when an IO error causes operation to fail
     */
    @Override
    public int read(final byte[] buffer, final int offset, final int count) throws IOException
    {
        int position = offset;
        int numToRead = count;
        int totalRead = 0;

        if (entryOffset >= entrySize)
        {
            return -1;
        }

        if ((numToRead + entryOffset) > entrySize)
        {
            numToRead = (int) (entrySize - entryOffset);
        }

        if (null != readBuf)
        {
            final int size =
                (numToRead > readBuf.length) ? readBuf.length : numToRead;

            System.arraycopy(readBuf, 0, buffer, position, size);

            if (size >= readBuf.length)
            {
                readBuf = null;
            }
            else
            {
                final int newLength = readBuf.length - size;
                final byte[] newBuffer = new byte[newLength];

                System.arraycopy(readBuf, size, newBuffer, 0, newLength);

                readBuf = newBuffer;
            }

            totalRead += size;
            numToRead -= size;
            position += size;
        }

        while (numToRead > 0)
        {
            final byte[] rec = this.buffer.readRecord();
            if (null == rec)
            {
                // Unexpected EOF!
                final String message =
                    "unexpected EOF with " + numToRead + " bytes unread";
                throw new IOException(message);
            }

            int size = numToRead;
            final int recordLength = rec.length;

            if (recordLength > size)
            {
                System.arraycopy(rec, 0, buffer, position, size);

                readBuf = new byte[recordLength - size];

                System.arraycopy(rec, size, readBuf, 0, recordLength - size);
            }
            else
            {
                size = recordLength;

                System.arraycopy(rec, 0, buffer, position, recordLength);
            }

            totalRead += size;
            numToRead -= size;
            position += size;
        }

        entryOffset += totalRead;

        return totalRead;
    }

    /**
     * Since we do not support marking just yet, we do nothing.
     */
    @Override
    public void reset()
    {
    }

    public void longSkip(final long numToSkip) throws IOException
    {
        for (long skipped = 0; skipped < numToSkip;)
        {
            if (numToSkip - skipped > Integer.MAX_VALUE)
            {
                skip(Integer.MAX_VALUE);
                skipped += Integer.MAX_VALUE;
            }
            else
            {
                skip((int) (numToSkip - skipped));
                skipped += numToSkip - skipped;
            }
        }
    }

    /**
     * Skip bytes in the input buffer. This skips bytes in the current entry's
     * data, not the entire archive, and will stop at the end of the current
     * entry's data if the number to skip extends beyond that point.
     *
     * @param numToSkip The number of bytes to skip.
     * @throws IOException when an IO error causes operation to fail
     */
    public void skip(final int numToSkip) throws IOException
    {
        // REVIEW
        // This is horribly inefficient, but it ensures that we
        // properly skip over bytes via the TarBuffer...
        //
        final byte[] skipBuf = new byte[8 * 1024];
        int num = numToSkip;
        while (num > 0)
        {
            final int count = (num > skipBuf.length) ? skipBuf.length : num;
            final int numRead = read(skipBuf, 0, count);
            if (numRead == -1)
            {
                break;
            }

            num -= numRead;
        }
    }

    /**
     * Utility method to do debugging.
     * Capable of being overidden in sub-classes.
     *
     * @param message the message to use in debugging
     */
    protected void debug(final String message)
    {
        if (debug)
        {
            System.err.println(message);
        }
    }
}
