package org.apache.commons.vfs.operations.vcs;

import org.apache.commons.vfs.operations.FileOperation;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsDelete extends FileOperation
{
	/**
	 * 
	 * @param force
	 */
	void setForce(final boolean force);
}
