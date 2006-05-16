package org.apache.commons.vfs.operations.vcs;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsModifyListener
{
	/**
	 * 
	 * @param path
	 * @param contentStatus
	 */
	void modified(final String path, final int contentStatus);
}
