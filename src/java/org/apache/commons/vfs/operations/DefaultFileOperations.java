package org.apache.commons.vfs.operations;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileObject;

import java.util.List;
import java.util.ArrayList;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public class DefaultFileOperations implements FileOperations
{
	/**
	 * 
	 */
	private FileSystemManager fsmanager;

	/**
	 * 
	 */
	private FileObject fileObject;

	/**
	 * 
	 * @param file
	 */
	public DefaultFileOperations(final FileObject file)
	{
		fileObject = file;

		fsmanager = file.getFileSystem().getFileSystemManager();
	}

	/**
	 * @return
	 * @throws org.apache.commons.vfs.FileSystemException
	 * 
	 */
	public Class[] getOperations() throws FileSystemException
	{

		final String scheme = fileObject.getURL().getProtocol();
		final FileOperationProvider[] providers = fsmanager
				.getOperationProviders(scheme);

		if (providers == null)
		{
			return null;
		}

		final List operations = new ArrayList();

		for (int i = 0; i < providers.length; i++)
		{
			FileOperationProvider provider = providers[i];

			provider.collectOperations(operations, fileObject);
		}

		return (Class[]) operations.toArray(new Class[] {});
	}

	/**
	 * @param operationClass
	 * @return
	 * @throws org.apache.commons.vfs.FileSystemException
	 * 
	 */
	public FileOperation getOperation(Class operationClass)
			throws FileSystemException
	{

		final String scheme = fileObject.getURL().getProtocol();
		final FileOperationProvider[] providers = fsmanager
				.getOperationProviders(scheme);

		if (providers == null)
		{
			throw new FileSystemException(
					"vfs.provider/operation-not-supported.error", operationClass);
		}

		FileOperation resultOperation = null;

		for (int i = 0; i < providers.length; i++)
		{
			FileOperationProvider provider = providers[i];

			resultOperation = provider.getOperation(fileObject, operationClass);

			if (resultOperation != null)
			{
				break;
			}
		}

		if (resultOperation == null)
		{
			throw new FileSystemException(
					"vfs.provider/operation-not-supported.error", operationClass);
		}

		return resultOperation;
	}

	/**
	 * @param operationClass
	 *            the operation's class.
	 * 
	 * @return true if the operation of specified class is supported for current
	 *         FileObject and false otherwise.
	 * 
	 * @throws org.apache.commons.vfs.FileSystemException
	 * 
	 */
	public boolean hasOperation(Class operationClass) throws FileSystemException
	{
		Class[] operations = getOperations();
		if (operations == null)
		{
			return false;
		}

		for (int i = 0; i < operations.length; i++)
		{
			Class operation = operations[i];
			if (operationClass.isAssignableFrom(operation))
			{
				return true;
			}
		}
		return false;
	}
}
