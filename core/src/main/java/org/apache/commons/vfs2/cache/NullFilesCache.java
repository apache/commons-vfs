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
package org.apache.commons.vfs2.cache;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;

/**
 * <p>
 * A {@link org.apache.commons.vfs2.FilesCache} implementation.<br>
 * This implementation never ever caches a single file.
 * </p>
 * <p>
 * <b>Notice: if you use resolveFile(uri) multiple times with the same path, the system will always
 * create a new instance.
 * Changes on one instance of this file are not seen by the others.</b>
 * </p>
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class NullFilesCache extends AbstractFilesCache
{
    public void putFile(final FileObject file)
    {
    }

    public FileObject getFile(final FileSystem filesystem, final FileName name)
    {
        return null;
    }

    public void clear(FileSystem filesystem)
    {
    }

    @Override
    public void close()
    {
        super.close();
    }

    public void removeFile(FileSystem filesystem, FileName name)
    {
    }

    public void touchFile(FileObject file)
    {
    }
}
