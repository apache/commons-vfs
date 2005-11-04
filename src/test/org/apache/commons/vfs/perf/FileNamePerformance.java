package org.apache.commons.vfs.perf;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

public class FileNamePerformance
{
	private final static int NUOF_RESOLVES = 100000;

	public static void main(String[] args) throws FileSystemException
	{
		FileSystemManager mgr = VFS.getManager();

		FileObject root = mgr
				.resolveFile("smb://HOME\\vfsusr:vfs%2f%25\\te:st@10.0.1.54/vfsusr");
		FileName rootName = root.getName();
	
		testNames(mgr, rootName);

		testChildren(root);

		testFiles(mgr);
	}

	private static void testFiles(FileSystemManager mgr) throws FileSystemException
	{
		for (int i = 0; i < 10; i++)
		{
			// warmup jvm
			mgr.resolveFile("smb://HOME\\vfsusr:vfs%2f%25\\te:st@10.0.1.54/vfsusr/many/path/elements/with%25esc/any%25where/to/file.txt");
		}

		long start = System.currentTimeMillis();
		for (int i = 0; i < NUOF_RESOLVES; i++)
		{
			mgr.resolveFile("smb://HOME\\vfsusr:vfs%2f%25\\te:st@10.0.1.54/vfsusr/many/path/elements/with%25esc/any%25where/to/file.txt");
		}
		long end = System.currentTimeMillis();

		System.err.println("time to resolve " + NUOF_RESOLVES + " files: "
				+ (end - start) + "ms");
	}

	private static void testChildren(FileObject root) throws FileSystemException
	{
		for (int i = 0; i < 10; i++)
		{
			// warmup jvm
			root.resolveFile("/many/path/elements/with%25esc/any%25where/to/file.txt");
		}

		long start = System.currentTimeMillis();
		for (int i = 0; i < NUOF_RESOLVES; i++)
		{
			root.resolveFile("/many/path/elements/with%25esc/any%25where/to/file.txt");
		}
		long end = System.currentTimeMillis();

		System.err.println("time to resolve " + NUOF_RESOLVES + " childs: "
				+ (end - start) + "ms");
	}

	private static void testNames(FileSystemManager mgr, FileName rootName) throws FileSystemException
	{
		for (int i = 0; i < 10; i++)
		{
			// warmup jvm
			mgr.resolveName(rootName,
					"/many/path/elements/with%25esc/any%25where/to/file.txt");
		}

		long start = System.currentTimeMillis();
		for (int i = 0; i < NUOF_RESOLVES; i++)
		{
			mgr.resolveName(rootName,
					"/many/path/elements/with%25esc/any%25where/to/file.txt");
		}
		long end = System.currentTimeMillis();

		System.err.println("time to resolve " + NUOF_RESOLVES + " names: "
				+ (end - start) + "ms");
	}
}
