package org.apache.commons.vfs.operations.vcs;

import org.apache.commons.vfs.operations.FileOperation;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsLog extends FileOperation
{
	/**
	 * 
	 * @param startRev
	 */
	void setStartRevision(final long startRev);

	/**
	 * 
	 * @param endRev
	 */
	void setEndRevision(final long endRev);

	/**
	 * 
	 * @param handler
	 */
	void setLogEntryHandler(final VcsLogEntryHandler handler);
}
