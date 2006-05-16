package org.apache.commons.vfs.operations.vcs;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsUpdateListener
{
	/**
	 * 
	 * @param path
	 * @param revision
	 * @param contentStatus
	 *            takes one of the values as defined in the
	 * @see VcsStatus constants.
	 */
	void updated(final String path, final long revision, final int contentStatus);
}
