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
package org.apache.commons.vfs.operations.vcs;

import java.util.Calendar;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public class VcsLogEntry
{
	/**
	 * 
	 */
	private String author;

	/**
	 * Revision.
	 */
	private long revision;

	/**
	 * Message.
	 */
	private String message;

	/**
	 * Date.
	 */
	private Calendar date;

	/**
	 * Path.
	 */
	private String path;

	/**
	 * 
	 * @param revision
	 * @param message
	 * @param date
	 * @param path
	 */
	public VcsLogEntry(final String author, final long revision,
			final String message, final Calendar date, final String path)
	{
		this.author = author;
		this.revision = revision;
		this.message = message;
		this.date = date;
		this.path = path;
	}

	/**
	 * 
	 * @return
	 */
	public String getAuthor()
	{
		return author;
	}

	/**
	 * 
	 * @return
	 */
	public long getRevision()
	{
		return revision;
	}

	/**
	 * 
	 * @return
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * 
	 * @return
	 */
	public Calendar getDate()
	{
		return date;
	}

	/**
	 * 
	 * @return
	 */
	public String getPath()
	{
		return path;
	}
}
