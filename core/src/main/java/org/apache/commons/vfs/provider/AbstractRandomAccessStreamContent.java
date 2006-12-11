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

import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.util.RandomAccessMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;

/**
 * Implements the part usable for all stream base random access implementations
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public abstract class AbstractRandomAccessStreamContent extends AbstractRandomAccessContent
{
    protected AbstractRandomAccessStreamContent(final RandomAccessMode mode)
    {
		super(mode);
    }

	protected abstract DataInputStream getDataInputStream() throws IOException;

	public byte readByte() throws IOException
	{
		byte data = getDataInputStream().readByte();
		return data;
	}

	public char readChar() throws IOException
	{
		char data = getDataInputStream().readChar();
		return data;
	}

	public double readDouble() throws IOException
	{
		double data = getDataInputStream().readDouble();
		return data;
	}

	public float readFloat() throws IOException
	{
		float data = getDataInputStream().readFloat();
		return data;
	}

	public int readInt() throws IOException
	{
		int data = getDataInputStream().readInt();
		return data;
	}

	public int readUnsignedByte() throws IOException
	{
		int data = getDataInputStream().readUnsignedByte();
		return data;
	}

	public int readUnsignedShort() throws IOException
	{
		int data = getDataInputStream().readUnsignedShort();
		return data;
	}

	public long readLong() throws IOException
	{
		long data = getDataInputStream().readLong();
		return data;
	}

	public short readShort() throws IOException
	{
		short data = getDataInputStream().readShort();
		return data;
	}

	public boolean readBoolean() throws IOException
	{
		boolean data = getDataInputStream().readBoolean();
		return data;
	}

	public int skipBytes(int n) throws IOException
	{
		int data = getDataInputStream().skipBytes(n);
		return data;
	}

	public void readFully(byte b[]) throws IOException
	{
		getDataInputStream().readFully(b);
	}

	public void readFully(byte b[], int off, int len) throws IOException
	{
		getDataInputStream().readFully(b, off, len);
	}

	public String readUTF() throws IOException
	{
		String data = getDataInputStream().readUTF();
		return data;
	}

	public InputStream getInputStream() throws IOException
	{
		return getDataInputStream();
	}
}
