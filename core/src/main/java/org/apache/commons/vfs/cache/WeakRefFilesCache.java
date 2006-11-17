package org.apache.commons.vfs.cache;

import org.apache.commons.vfs.FileObject;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * This implementation caches every file as long as it is strongly reachable by
 * the java vm. As soon as the object is no longer reachable it will be discarded.
 * In contrast to the SoftRefFilesCache this implementation might free resources faster
 * as it don't wait until a memory limitation.
 * 
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date: 2005-09-30 09:02:41 +0200 (Fr, 30 Sep
 *          2005) $
 * @see java.lang.ref.WeakReference
 */
public class WeakRefFilesCache extends SoftRefFilesCache
{
	protected Reference createReference(FileObject file, ReferenceQueue refqueue)
	{
		return new WeakReference(file, refqueue);
	}
}
