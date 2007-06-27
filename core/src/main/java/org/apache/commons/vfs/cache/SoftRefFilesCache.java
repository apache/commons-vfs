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
package org.apache.commons.vfs.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.VfsLog;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.util.Messages;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This implementation caches every file as long as it is strongly reachable by
 * the java vm. As soon as the vm needs memory - every softly reachable file
 * will be discarded.
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date: 2005-09-30 09:02:41 +0200 (Fr, 30 Sep
 *          2005) $
 * @see SoftReference
 */
public class SoftRefFilesCache extends AbstractFilesCache
{
	/**
	 * The logger to use.
	 */
	private Log log = LogFactory.getLog(SoftRefFilesCache.class);

	private final Map filesystemCache = new HashMap();
	private final Map refReverseMap = new HashMap(100);
	private final ReferenceQueue refqueue = new ReferenceQueue();

	private SoftRefReleaseThread softRefReleaseThread = null;

	/**
	 * This thread will listen on the ReferenceQueue and remove the entry in the
	 * filescache as soon as the vm removes the reference
	 */
	private class SoftRefReleaseThread extends Thread
	{
		private boolean requestEnd = false;

		private SoftRefReleaseThread()
		{
			setName(SoftRefReleaseThread.class.getName());
			setDaemon(true);
		}

		public void run()
		{
			loop: while (!requestEnd && !Thread.currentThread().isInterrupted())
			{
				try
				{
					Reference ref = refqueue.remove(1000);
					if (ref == null)
					{
						continue;
					}

					FileSystemAndNameKey key;
					synchronized(refReverseMap)
					{
						key = (FileSystemAndNameKey) refReverseMap
								.get(ref);
					}

					if (key != null)
					{
						if (removeFile(key))
						{
							filesystemClose(key.getFileSystem());
						}
					}
				}
				catch (InterruptedException e)
				{
					if (!requestEnd)
					{
						VfsLog
								.warn(
										getLogger(),
										log,
										Messages
												.getString("vfs.impl/SoftRefReleaseThread-interrupt.info"));
					}
					break loop;
				}
			}
		}
	}

	public SoftRefFilesCache()
	{
	}

	private void startThread()
	{
		if (softRefReleaseThread != null)
		{
			throw new IllegalStateException(
					Messages
							.getString("vfs.impl/SoftRefReleaseThread-already-running.warn"));
		}

		softRefReleaseThread = new SoftRefReleaseThread();
		softRefReleaseThread.start();
	}

	private void endThread()
	{
		if (softRefReleaseThread != null)
		{
			softRefReleaseThread.requestEnd = true;
			softRefReleaseThread.interrupt();
			softRefReleaseThread = null;
		}
	}

	public void putFile(final FileObject file)
	{
		if (log.isDebugEnabled())
		{
			log.debug("putFile: " + file.getName());
		}

		Map files = getOrCreateFilesystemCache(file.getFileSystem());

		Reference ref = createReference(file, refqueue);
		FileSystemAndNameKey key = new FileSystemAndNameKey(file
				.getFileSystem(), file.getName());

		synchronized (files)
		{
			files.put(file.getName(), ref);
			synchronized(refReverseMap)
			{
				refReverseMap.put(ref, key);
			}
		}
	}

	protected Reference createReference(FileObject file, ReferenceQueue refqueue)
	{
		return new SoftReference(file, refqueue);
	}

	public FileObject getFile(final FileSystem filesystem, final FileName name)
	{
		Map files = getOrCreateFilesystemCache(filesystem);

		synchronized (files)
		{
			Reference ref = (Reference) files.get(name);
			if (ref == null)
			{
				return null;
			}

			FileObject fo = (FileObject) ref.get();
			if (fo == null)
			{
				removeFile(filesystem, name);
			}
			return fo;
		}
	}

	public void clear(FileSystem filesystem)
	{
		Map files = getOrCreateFilesystemCache(filesystem);

		boolean closeFilesystem;

		synchronized (files)
		{
			synchronized(refReverseMap)
			{
				Iterator iterKeys = refReverseMap.values().iterator();
				while (iterKeys.hasNext())
				{
					FileSystemAndNameKey key = (FileSystemAndNameKey) iterKeys.next();
					if (key.getFileSystem() == filesystem)
					{
						iterKeys.remove();
						files.remove(key.getFileName());
					}
				}

				closeFilesystem = files.size() < 1;
			}
		}

		if (closeFilesystem)
		{
			filesystemClose(filesystem);
		}
	}

	private void filesystemClose(FileSystem filesystem)
	{
		if (log.isDebugEnabled())
		{
			log.debug("close fs: " + filesystem.getRootName());
		}
		synchronized (filesystemCache)
		{
			filesystemCache.remove(filesystem);
			if (filesystemCache.size() < 1)
			{
				endThread();
			}
		}
		((DefaultFileSystemManager) getContext().getFileSystemManager())
				._closeFileSystem(filesystem);
	}

	public void close()
	{
		super.close();

		endThread();

		// files.clear();
		synchronized (filesystemCache)
		{
			filesystemCache.clear();
		}

		synchronized(refReverseMap)
		{
			refReverseMap.clear();
		}
	}

	public void removeFile(FileSystem filesystem, FileName name)
	{
		if (removeFile(new FileSystemAndNameKey(filesystem, name)))
		{
			filesystemClose(filesystem);
		}
	}

	public void touchFile(FileObject file)
	{
	}

	private boolean removeFile(final FileSystemAndNameKey key)
	{
		if (log.isDebugEnabled())
		{
			log.debug("removeFile: " + key.getFileName());
		}

		Map files = getOrCreateFilesystemCache(key.getFileSystem());

		synchronized (files)
		{
			Object ref = files.remove(key.getFileName());
			if (ref != null)
			{
				synchronized(refReverseMap)
				{
					refReverseMap.remove(ref);
				}
			}

			return files.size() < 1;
		}
	}

	protected Map getOrCreateFilesystemCache(final FileSystem filesystem)
	{
		synchronized (filesystemCache)
		{
			if (filesystemCache.size() < 1)
			{
				startThread();
			}

			Map files = (Map) filesystemCache.get(filesystem);
			if (files == null)
			{
				files = new HashMap();
				filesystemCache.put(filesystem, files);
			}

			return files;
		}
	}
}
