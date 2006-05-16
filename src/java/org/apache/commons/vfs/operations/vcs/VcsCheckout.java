package org.apache.commons.vfs.operations.vcs;

import org.apache.commons.vfs.operations.FileOperation;
import org.apache.commons.vfs.FileObject;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsCheckout extends FileOperation
{
	/**
	 * 
	 * @param revision
	 */
	void setRevision(final long revision);

	/**
	 * 
	 * @param recursive
	 */
	void setRecursive(final boolean recursive);

	/**
	 * 
	 * @param targetDir
	 *            directory under which retrieved files should be placed.
	 */
	void setTargetDirectory(final FileObject targetDir);

	/**
	 * @param export
	 *            if true, administrative .svn directoies will not be created on
	 *            the retrieved tree. The checkout operation in this case is
	 *            equivalent to export function.
	 */
	void setExport(final boolean export);
}
