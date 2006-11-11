/*
 * Copyright 2002-2005 The Apache Software Foundation.
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
package org.apache.commons.vfs.util;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.RandomAccessContent;

import javax.mail.internet.SharedInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper to an FileObject to get a {@link javax.mail.internet.SharedInputStream} 
 *
 * @author <a href="mailto:imario@apache.org">imario@apache.org</a>
 * @version $Revision$ $Date$
 */
public class SharedRandomContentInputStream extends BufferedInputStream implements SharedInputStream
{
	private final FileObject fo;
	private final long fileStart;
	private final long start;
	private final long end;
	
	private long pos;
	private long resetCount;

	private SharedRandomContentInputStream(final FileObject fo, final long fileStart, final long start, final long end, final InputStream is) throws FileSystemException
	{
		super(is);

		if (!fo.getFileSystem().hasCapability(Capability.RANDOM_ACCESS_READ))
		{
			throw new FileSystemException("vfs.util/missing-capability.error", Capability.RANDOM_ACCESS_READ);
		}
		
		this.fo = fo;
		this.fileStart = fileStart;
		this.start = start;
		this.end = end;
	}

	public SharedRandomContentInputStream(final FileObject fo) throws FileSystemException
	{
		this(fo, 0, 0, -1, fo.getContent().getInputStream());
	}


	public synchronized int read() throws IOException
	{
		if (checkEnd())
		{
			return -1;
		}
		int r = super.read();
		pos++;
		resetCount++;
		return r;
	}

	public synchronized int read(byte b[], int off, int len) throws IOException
	{
		if (checkEnd())
		{
			return -1;
		}

		int nread = super.read(b, off, len);
		pos+=nread;
		resetCount+=nread;
		return nread;
	}

	public synchronized long skip(long n) throws IOException
	{
		if (checkEnd())
		{
			return -1;
		}

		long nskip = super.skip(n);
		pos+=nskip;
		resetCount+=nskip;
		return nskip;
	}

	private boolean checkEnd()
	{
		return end > -1 && (start + pos) >= end;
	}

	public synchronized void mark(int readlimit)
	{
		super.mark(readlimit);
		resetCount = 0;
	}

	public synchronized void reset() throws IOException
	{
		super.reset();
		pos-=resetCount;
		resetCount=0;
	}

	public long getPosition()
	{
		return pos;
	}

	public InputStream newStream(long start, long end)
	{
		try
		{
			RandomAccessContent rac = fo.getContent().getRandomAccessContent(RandomAccessMode.READ);
			rac.seek(this.fileStart+start);
			return new SharedRandomContentInputStream(fo, this.fileStart+start, start, end, rac.getInputStream());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
