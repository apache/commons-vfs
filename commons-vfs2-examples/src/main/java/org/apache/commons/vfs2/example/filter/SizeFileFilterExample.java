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
package org.apache.commons.vfs2.example.filter;

import java.io.File;

import org.apache.commons.vfs2.FileFilterSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.filter.SizeFileFilter;

/**
 * Example for using {@link SizeFileFilter}.
 */
// CHECKSTYLE:OFF Example code
public class SizeFileFilterExample {

    public static void main(final String[] args) throws Exception {

        // Example, to print all files and directories in the current directory
        // whose size is greater than 1 MB
        final FileSystemManager fsManager = VFS.getManager();
        final FileObject dir = fsManager.toFileObject(new File("."));
        final SizeFileFilter filter = new SizeFileFilter(1024 * 1024);
        final FileObject[] files = dir.findFiles(new FileFilterSelector(filter));
        for (FileObject file : files) {
            System.out.println(file);
        }

    }

}
// CHECKSTYLE:ON

