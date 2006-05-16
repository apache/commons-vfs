package org.apache.commons.vfs.operations.vcs;

import org.apache.commons.vfs.operations.FileOperation;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsStatus extends FileOperation
{
	public final static int UNKNOWN = -1;
	public final static int NOT_MODIFIED = 0;
	public final static int ADDED = 1;
	public final static int CONFLICTED = 2;
	public final static int DELETED = 3;
	public final static int MERGED = 4;
	public final static int IGNORED = 5;
	public final static int MODIFIED = 6;
	public final static int REPLACED = 7;
	public final static int UNVERSIONED = 8;
	public final static int MISSING = 9;
	public final static int OBSTRUCTED = 10;
	public final static int REVERTED = 11;
	public final static int RESOLVED = 12;
	public final static int COPIED = 13;
	public final static int MOVED = 14;
	public final static int RESTORED = 15;
	public final static int UPDATED = 16;
	public final static int EXTERNAL = 18;
	public final static int CORRUPTED = 19;
	public final static int NOT_REVERTED = 20;

	/**
	 * 
	 * @return the status of FileObject
	 */
	int getStatus();
}
