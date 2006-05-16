package org.apache.commons.vfs.operations.vcs;

import org.apache.commons.vfs.operations.FileOperation;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsAdd extends FileOperation
{
	/**
	 * 
	 * @param makedir
	 */
	void setMakedir(final boolean makedir);

	/**
	 * 
	 * @param recirsive
	 */
	void setRecursive(final boolean recirsive);
}
