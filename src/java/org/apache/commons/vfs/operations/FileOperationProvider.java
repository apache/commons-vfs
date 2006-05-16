package org.apache.commons.vfs.operations;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

import java.util.List;
import java.util.Collection;

/**
 * FileOperationProvider is responsible for dealing with FileOperation's.
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface FileOperationProvider
{

	/**
	 * Gather available operations for the specified FileObject and put them into
	 * specified operationsList.
	 * 
	 * @param operationsList
	 *            the list of available operations for the specivied FileObject.
	 *            The operationList contains classes of available operations, e.g.
	 *            Class objects.
	 * @param file
	 *            the FileObject for which we want to get the list of available
	 *            operations.
	 * 
	 * @throws FileSystemException
	 *             if list of operations cannto be retrieved.
	 */
	void collectOperations(final Collection operationsList, final FileObject file)
			throws FileSystemException;

	/**
	 * 
	 * @param file
	 *            the FileObject for which we need a operation.
	 * @param operationClass
	 *            the Class which instance we are needed.
	 * @return the requried operation instance. s
	 * @throws FileSystemException
	 *             if operation cannot be retrieved.
	 */
	FileOperation getOperation(final FileObject file, final Class operationClass)
			throws FileSystemException;
}
