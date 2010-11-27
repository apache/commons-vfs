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
package org.apache.commons.vfs2.provider.bzip2;

//TODO: Revert to [compress]
//import org.apache.commons.compress.bzip2.CBZip2InputStream;
//import org.apache.commons.compress.bzip2.CBZip2OutputStream;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.compressed.CompressedFileFileObject;
import org.apache.commons.vfs2.provider.compressed.CompressedFileFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * the bzip2 file.
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @version $Revision$ $Date$
 */
public class Bzip2FileObject extends CompressedFileFileObject
{
    protected Bzip2FileObject(AbstractFileName name, FileObject container, CompressedFileFileSystem fs)
    {
        super(name, container, fs);
    }

    @Override
    protected InputStream doGetInputStream() throws Exception
    {
        // check file
        InputStream is = getContainer().getContent().getInputStream();
        return wrapInputStream(getName().getURI(), is);
    }

    public static InputStream wrapInputStream(final String name, final InputStream is) throws IOException
    {
        final int b1 = is.read();
        final int b2 = is.read();
        if (b1 != 'B' || b2 != 'Z')
        {
            throw new FileSystemException("vfs.provider.compressedFile/not-a-compressedFile-file.error", name);
        }
        return new CBZip2InputStream(is);
    }

    @Override
    protected OutputStream doGetOutputStream(boolean bAppend) throws Exception
    {
        OutputStream os = getContainer().getContent().getOutputStream(false);
        os.write('B');
        os.write('Z');

        return new CBZip2OutputStream(os);
    }
}
