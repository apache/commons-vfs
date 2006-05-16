package org.apache.commons.vfs.operations.vcs;

import org.apache.commons.vfs.operations.FileOperation;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsUpdate extends FileOperation
{
	/**
	 * 
	 * @param revision
	 */
	void setRevision(final long revision);

	/**
	 * 
	 * @param isRecursive
	 */
	void setRecursive(final boolean isRecursive);

	/**
	 * 
	 * @param listener
	 */
	void addUpdateListener(final VcsUpdateListener listener);

	/**
	 * 
	 * @param listener
	 */
	void removeUpdateListener(final VcsUpdateListener listener);
}
