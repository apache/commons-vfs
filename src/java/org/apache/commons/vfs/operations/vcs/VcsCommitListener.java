package org.apache.commons.vfs.operations.vcs;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsCommitListener
{
	/**
	 * 
	 * @param path
	 * @param contentStatus
	 *            takes one of the values as defined in the
	 * @see VcsStatus constants.
	 */
	void commited(final String path, final int contentStatus);
}
