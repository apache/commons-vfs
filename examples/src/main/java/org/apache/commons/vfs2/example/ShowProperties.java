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
package org.apache.commons.vfs2.example;

import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;

/**
 * A simple that prints the properties of the file passed as first parameter.
 *
 * @author <a href="mailto:anthony@antcommander.com">Anthony Goubard</a>
 * @version $Revision:232419 $ $Date:2005-08-13 07:23:40 +0200 (Sa, 13 Aug 2005) $
 */


public class ShowProperties
{
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.err.println("Please pass the name of a file as parameter.");
            System.err.println("e.g. java org.apache.commons.vfs2.example.ShowProperties LICENSE.txt");
            return;
        }
        for (int i = 0; i < args.length; i++)
        {
            try
            {
                FileSystemManager mgr = VFS.getManager();
                System.out.println();
                System.out.println("Parsing: " + args[i]);
                FileObject file = mgr.resolveFile(args[i]);
                System.out.println("URL: " + file.getURL());
                System.out.println("getName(): " + file.getName());
                System.out.println("BaseName: " + file.getName().getBaseName());
                System.out.println("Extension: " + file.getName().getExtension());
                System.out.println("Path: " + file.getName().getPath());
                System.out.println("Scheme: " + file.getName().getScheme());
                System.out.println("URI: " + file.getName().getURI());
                System.out.println("Root URI: " + file.getName().getRootURI());
                System.out.println("Parent: " + file.getName().getParent());
                System.out.println("Type: " + file.getType());
                System.out.println("Exists: " + file.exists());
                System.out.println("Readable: " + file.isReadable());
                System.out.println("Writeable: " + file.isWriteable());
                System.out.println("Root path: " + file.getFileSystem().getRoot().getName().getPath());
                if (file.exists())
                {
                    if (file.getType().equals(FileType.FILE))
                    {
                        System.out.println("Size: " + file.getContent().getSize() + " bytes");
                    }
                    else if (file.getType().equals(FileType.FOLDER) && file.isReadable())
                    {
                        FileObject[] children = file.getChildren();
                        System.out.println("Directory with " + children.length + " files");
                        for (int iterChildren = 0; iterChildren < children.length; iterChildren++)
                        {
                            System.out.println("#" + iterChildren + ": " + children[iterChildren].getName());
                            if (iterChildren > 5)
                            {
                                break;
                            }
                        }
                    }
                    System.out.println("Last modified: " + DateFormat.getInstance().format(new Date(file.getContent().getLastModifiedTime())));
                }
                else
                {
                    System.out.println("The file does not exist");
                }
                file.close();
            }
            catch (FileSystemException ex)
            {
                ex.printStackTrace();
            }
        }
    }
}

