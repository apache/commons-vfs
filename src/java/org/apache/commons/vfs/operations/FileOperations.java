package org.apache.commons.vfs.operations;

import org.apache.commons.vfs.FileSystemException;

/**
 * FileOperations interface provides API to work with operations.
 * 
 * @see FileOperation on what a operation in the context of VFS is.
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface FileOperations
{
	/**
	 * @return all operations associated with the fileObject
	 * @throws FileSystemException
	 */
	Class[] getOperations() throws FileSystemException;

	/**
	 * @return a operation implementing the given <code>operationClass</code>
	 * @throws FileSystemException
	 */
	FileOperation getOperation(Class operationClass) throws FileSystemException;

	/**
	 * @return if a operation <code>operationClass</code> is available
	 * @throws FileSystemException
	 */
	boolean hasOperation(Class operationClass) throws FileSystemException;
}
