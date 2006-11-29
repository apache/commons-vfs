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
package org.apache.commons.vfs.provider.mime;

import org.apache.commons.vfs.FileContentInfoFactory;
import org.apache.commons.vfs.FileContentInfo;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileContentInfo;

import javax.mail.MessagingException;
import javax.mail.internet.ContentType;

/**
 * get access to the content info stuff for mime objects
 *
 * @author <a href="mailto:imario@apache.org">imario@apache.org</a>
 * @version $Revision$ $Date$
 */
public class MimeFileContentInfoFactory implements FileContentInfoFactory
{
	public FileContentInfo create(FileContent fileContent) throws FileSystemException
	{
		MimeFileObject mimeFile = (MimeFileObject) fileContent.getFile();

		ContentType contentType;
		try
		{
			String contentTypeString = mimeFile.getPart().getContentType();
			contentType = new ContentType(contentTypeString);
		}
		catch (MessagingException e)
		{
			throw new FileSystemException(e);
		}

		String charset = contentType.getParameter("charset");
		try
		{
			if (mimeFile.isMultipart())
			{
				// if this is a multipart message we deliver the preamble instead of an inupt string
				// the preamble will be delivered in UTF-8 - fixed
				charset = MimeFileSystem.PREAMBLE_CHARSET;
			}
		}
		catch (MessagingException e)
		{
			throw  new FileSystemException(e);
		}

		return new DefaultFileContentInfo(contentType.getBaseType(), charset);
	}
}
