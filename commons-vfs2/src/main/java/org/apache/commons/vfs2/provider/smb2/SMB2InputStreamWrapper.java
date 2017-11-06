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
package org.apache.commons.vfs2.provider.smb2;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;

public class SMB2InputStreamWrapper extends InputStream
{
	private InputStream is;
	private final FileObject fo;
	
	public SMB2InputStreamWrapper(InputStream is, final FileObject fo)
	{
		this.is = is;
		this.fo = fo;
	}

	@Override
	public int read() throws IOException
	{
		return is.read();
	}
	
	//the only reason this class exists
	@Override
	public void close() throws IOException
	{
		is.close();
		fo.close();
	}
}
