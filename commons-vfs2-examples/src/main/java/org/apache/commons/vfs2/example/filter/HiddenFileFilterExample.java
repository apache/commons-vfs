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
import org.apache.commons.vfs2.filter.HiddenFileFilter;

/**
 * Example for using {@link HiddenFileFilter}.
 */
// CHECKSTYLE:OFF Example code
public class HiddenFileFilterExample {

    public static void main(final String[] args) throws Exception {

        // Example, showing how to print out a list of the current directory's
        // hidden files
        {
            System.out.println("---HIDDEN---");
            final FileSystemManager fsManager = VFS.getManager();
            final FileObject dir = fsManager.toFileObject(new File("."));
            final FileObject[] files = dir.findFiles(new FileFilterSelector(HiddenFileFilter.HIDDEN));
            for (FileObject file : files) {
                System.out.println(file);
            }
        }

        // Example, showing how to print out a list of the current directory's
        // visible (i.e. not hidden) files
        {
            System.out.println("---VISIBLE---");
            final FileSystemManager fsManager = VFS.getManager();
            final FileObject dir = fsManager.toFileObject(new File("."));
            final FileObject[] files = dir.findFiles(new FileFilterSelector(HiddenFileFilter.VISIBLE));
            for (FileObject file : files) {
                System.out.println(file);
            }
        }

    }

}
// CHECKSTYLE:ON
