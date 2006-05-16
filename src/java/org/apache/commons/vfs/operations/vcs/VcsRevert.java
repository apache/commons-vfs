package org.apache.commons.vfs.operations.vcs;

import org.apache.commons.vfs.operations.FileOperation;

/**
 * <p>
 * Restores pristine working copy file and cancels all local modifications. In
 * other words, VcsRevert replaces working copy file with the latest version
 * from the repository.
 * </p>
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsRevert extends FileOperation
{
	/**
	 * 
	 * @param recursive
	 */
	void setRecursive(final boolean recursive);

	/**
	 * 
	 * @param listener
	 */
	void addModifyListener(final VcsModifyListener listener);

	/**
	 * 
	 * @param listener
	 */
	void removeModifyListener(final VcsModifyListener listener);
}
