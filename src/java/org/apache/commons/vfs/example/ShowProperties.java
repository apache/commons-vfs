/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.commons.vfs.example;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

/**
 * A simple that prints the properties of the file passed as first parameter.
 *
 * @author <a href="mailto:anthony@antcommander.com">Anthony Goubard</a>
 * @version $Revision: 1.1 $ $Date: 2004/05/17 18:30:45 $
 */
public class ShowProperties
{

    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.err.println("Please pass the name of a file as parameter.");
            System.err.println("e.g. java org.apache.commons.vfs.example.ShowProperties LICENSE.txt");
            return;
        }
        try
        {
            FileSystemManager mgr = VFS.getManager();
            FileObject file = mgr.resolveFile(args[0]);
            System.out.println("file " + file);
            System.out.println("file.getURL() " + file.getURL());
            System.out.println("file.getURL().toExternalForm() " + file.getURL().toExternalForm());
            System.out.println("file.getName() " + file.getName());
            System.out.println("file.getName().getBaseName() " + file.getName().getBaseName());
            System.out.println("file.getName().getExtension() " + file.getName().getExtension());
            System.out.println("file.getName().getPath() " + file.getName().getPath());
            System.out.println("file.getName().getScheme() " + file.getName().getScheme());
            System.out.println("file.getName().getURI() " + file.getName().getURI());
            System.out.println("file.getName().getRootURI() " + file.getName().getRootURI());
            System.out.println("file.getName().getParent() " + file.getName().getParent());
            System.out.println("file.getType() " + file.getType());
            System.out.println("file.getFileSystem().getRoot().getName().getPath() " + file.getFileSystem().getRoot().getName().getPath());
        }
        catch (FileSystemException ex)
        {
            ex.printStackTrace();
        }
    }
}
