/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.operations.vcs;

import org.apache.commons.vfs.operations.FileOperation;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public interface VcsStatus extends FileOperation
{
	public final static int UNKNOWN = -1;
	public final static int NOT_MODIFIED = 0;
	public final static int ADDED = 1;
	public final static int CONFLICTED = 2;
	public final static int DELETED = 3;
	public final static int MERGED = 4;
	public final static int IGNORED = 5;
	public final static int MODIFIED = 6;
	public final static int REPLACED = 7;
	public final static int UNVERSIONED = 8;
	public final static int MISSING = 9;
	public final static int OBSTRUCTED = 10;
	public final static int REVERTED = 11;
	public final static int RESOLVED = 12;
	public final static int COPIED = 13;
	public final static int MOVED = 14;
	public final static int RESTORED = 15;
	public final static int UPDATED = 16;
	public final static int EXTERNAL = 18;
	public final static int CORRUPTED = 19;
	public final static int NOT_REVERTED = 20;

	/**
	 * 
	 * @return the status of FileObject
	 */
	int getStatus();
}
