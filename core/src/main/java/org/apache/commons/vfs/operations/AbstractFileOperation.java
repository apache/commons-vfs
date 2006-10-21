/*
 * Copyright 2002-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
