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
package org.apache.commons.vfs.provider.mime;

import org.apache.commons.vfs.FileContentInfoFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A part of a MIME message.
 *
 * @author <a href="mailto:imario@apache.org">imario@apache.org</a>
 * @version $Revision$ $Date$
 */
public class MimeFileObject
	extends AbstractFileObject
	implements FileObject
{
	private final HashSet children = new HashSet();
	private final Part part;
	private final MimeFileSystem fileSystem;

	protected MimeFileObject(final FileName name,
							final Part bodyPart,
							final MimeFileSystem fileSystem) throws FileSystemException
	{
		super(name, fileSystem);
		this.part = bodyPart;
		this.fileSystem = fileSystem;
	}

	/**
	 * Attaches a child
	 */
	protected void attachChild(FileName childName)
	{
		children.add(childName.getBaseName());
	}

	/**
     * Attaches this file object to its file resource.
     */
    protected void doAttach() throws Exception
    {
    }

    protected void doDetach() throws Exception
    {
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     */
    protected FileType doGetType() throws Exception
    {
		if (part == null)
		{
			return FileType.IMAGINARY;
		}

		return FileType.FILE_OR_FOLDER;
    }

    /**
     * Lists the children of the file.  Is only called if {@link #doGetType}
     * returns {@link org.apache.commons.vfs.FileType#FOLDER}.
     */
    protected String[] doListChildren() throws Exception
    {
		return (String[]) children.toArray(new String[children.size()]);
	}

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize() throws Exception
    {
		return part.getSize();
	}

    /**
     * Returns the last modified time of this file.
     */
    protected long doGetLastModifiedTime()
        throws Exception
    {
		MimeMessage mm = fileSystem.getMimeMessage();
		if (mm.getSentDate() != null)
		{
			return mm.getSentDate().getTime();
		}
		if (mm.getReceivedDate() != null)
		{
			mm.getReceivedDate();
		}
		return 0;
	}

    /**
     * Creates an input stream to read the file content from.
     */
    protected InputStream doGetInputStream() throws Exception
    {
		return part.getInputStream();
	}

	protected FileContentInfoFactory getFileContentInfoFactory()
	{
		return new MimeFileContentInfoFactory();
	}

	protected Part getPart()
	{
		return part;
	}

	/**
	 * Returns all headers of this part.<br />
	 * The map key is a java.lang.String and the value is a:<br />
	 * <ul>
	 * <li>java.lang.Strings for single entries</li>
	 * or a
	 * <li>java.utils.List of java.lang.Strings for entries with multiple values</li>
	 * </ul>
	 */
	protected Map doGetAttributes() throws Exception
	{
		Map ret = new TreeMap();

		Enumeration headers = getAllHeaders();
		while (headers.hasMoreElements())
		{
			Header header = (Header) headers.nextElement();
			Object values = ret.get(header.getName());

			if (values == null)
			{
				ret.put(header.getName(), header.getValue());
			}
			else if (values instanceof String)
			{
				List newValues = new ArrayList();
				newValues.add(values);
				newValues.add(header.getValue());
				ret.put(header.getName(), newValues);
			}
			else if (values instanceof List)
			{
				((List) values).add(header.getValue());
			}
		}

		return ret;
	}

	protected Enumeration getAllHeaders() throws MessagingException
	{
		return part.getAllHeaders();
	}
}
