package org.apache.commons.vfs.operations.vcs;

import org.apache.commons.vfs.operations.FileOperation;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsCommit extends FileOperation
{

	/**
	 * 
	 * @param isRecursive
	 */
	void setRecursive(final boolean isRecursive);

	/**
	 * 
	 * @param message
	 */
	void setMessage(final String message);

	/**
	 * 
	 * @param listener
	 */
	void addCommitListener(final VcsCommitListener listener);

	/**
	 * 
	 * @param listener
	 */
	void removeCommitListener(final VcsCommitListener listener);
}
