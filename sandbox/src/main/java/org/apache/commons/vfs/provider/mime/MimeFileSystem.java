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

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.util.FileObjectDataSource;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;

/**
 * An MIME file system.
 *
 * @author <a href="mailto:imario@apache.org">imario@apache.org</a>
 * @version $Revision$ $Date$
 */
public class MimeFileSystem
	extends AbstractFileSystem
	implements FileSystem
{
	private final static String NULL_BP_NAME = "_body_part_";

	private MimeMultipart mimeMultipart;
	private MimeMessage mimeMessage;

	protected MimeFileSystem(final FileName rootName, final FileObject parentLayer, final FileSystemOptions fileSystemOptions)
    {
        super(rootName, parentLayer, fileSystemOptions);
	}

	public void init() throws FileSystemException
	{
		super.init();

		List strongRef = new ArrayList(100);

		// check if parent exists
		if (!getParentLayer().exists())
		{
			return;
		}
		
		try
		{
			InputStream is = null;
			try
			{
				is = getParentLayer().getContent().getInputStream();
				mimeMessage = new MimeMessage(null, is);
			}
			finally
			{
				if (is != null)
				{
					try
					{
						is.close();
					}
					catch (IOException e)
					{
						// ignore close errors
					}
				}
			}

			// create root
			FileName name = getFileSystemManager().resolveName(getRootName(), "/");
			MimeFileObject foRoot = new MimeFileObject(name, mimeMessage, this);
			putFileToCache(foRoot);
			strongRef.add(foRoot);
			foRoot.holdObject(strongRef);

			Object content;
			try
			{
				content = mimeMessage.getContent();
			}
			catch (IOException e)
			{
				throw new FileSystemException(e);
			}

			if (content instanceof Multipart)
			{
				// yes ... get all children
				mimeMultipart = new MimeMultipart(new FileObjectDataSource(getParentLayer()));

				for (int i = 0; i< mimeMultipart.getCount(); i++)
				{
					BodyPart bp = mimeMultipart.getBodyPart(i);

					String filename = UriParser.encode(bp.getFileName());
					if (filename == null)
					{
						filename = NULL_BP_NAME + i;
					}

					name = getFileSystemManager().resolveName(getRootName(), filename);
					MimeFileObject fo = new MimeFileObject(name, bp, this);

					putFileToCache(fo);
					strongRef.add(fo);
					fo.holdObject(strongRef);

					MimeFileObject parent;
					for (FileName parentName = name.getParent();
						 parentName != null;
						 fo = parent, parentName = parentName.getParent())
					{
						// Locate the parent
						parent = (MimeFileObject) getFileFromCache(parentName);
						if (parent == null)
						{
							parent = new MimeFileObject(name, null, this);
							putFileToCache(parent);
							strongRef.add(parent);
							parent.holdObject(strongRef);
						}

						// Attach child to parent
						parent.attachChild(fo.getName());
					}
				}
			}
		}
		catch (MessagingException e)
		{
			throw new FileSystemException(e);
		}

	}

	/**
     * Creates a file object.
     */
    protected FileObject createFile(final FileName name) throws FileSystemException
	{
        return new MimeFileObject(name, null, this);
    }

    /**
     * Returns the capabilities of this file system.
     */
    protected void addCapabilities(final Collection caps)
    {
        caps.addAll(MimeFileProvider.capabilities);
    }

	protected MimeMultipart getMimeMultipart()
	{
		return mimeMultipart;
	}

	protected MimeMessage getMimeMessage()
	{
		return mimeMessage;
	}
}
