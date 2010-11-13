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
package org.apache.commons.vfs2.perf;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

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
