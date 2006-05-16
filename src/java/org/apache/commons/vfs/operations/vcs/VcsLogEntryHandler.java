package org.apache.commons.vfs.operations.vcs;

import org.apache.commons.vfs.FileSystemException;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsLogEntryHandler
{
	/**
	 * 
	 * @param entry
	 * @throws FileSystemException
	 */
	void handleLogEntry(final VcsLogEntry entry) throws FileSystemException;
}
