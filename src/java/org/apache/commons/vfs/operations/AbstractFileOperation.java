package org.apache.commons.vfs.operations;

import org.apache.commons.vfs.FileObject;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public abstract class AbstractFileOperation implements FileOperation
{
	/**
	 * FileObject which the FileOperation is operate on.
	 */
	private FileObject fileObject;

	/**
	 * 
	 * @param file
	 */
	public AbstractFileOperation(final FileObject file)
	{
		fileObject = file;
	}

	/**
	 * 
	 * @return an instance of FileObject which this FileOperation is operate on.
	 */
	protected FileObject getFileObject()
	{
		return fileObject;
	}
}
