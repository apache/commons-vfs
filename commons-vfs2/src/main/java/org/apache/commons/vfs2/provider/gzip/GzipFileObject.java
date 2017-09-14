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
package org.apache.commons.vfs2.provider.gzip;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.compressed.CompressedFileFileObject;
import org.apache.commons.vfs2.provider.compressed.CompressedFileFileSystem;

/**
 * the gzip file.
 */
public class GzipFileObject extends CompressedFileFileObject<GzipFileSystem> {
    /**
     * Deprecated since 2.1.
     *
     * @deprecated Use {@link #GzipFileObject(AbstractFileName, FileObject, GzipFileSystem)} instead.
     */
    @Deprecated
    protected GzipFileObject(final AbstractFileName name, final FileObject container,
            final CompressedFileFileSystem fs) {
        super(name, container, cast(fs));
    }

    protected GzipFileObject(final AbstractFileName name, final FileObject container, final GzipFileSystem fs) {
        super(name, container, fs);
    }

    @Override
    protected InputStream doGetInputStream() throws Exception {
        final InputStream is = getContainer().getContent().getInputStream();
        return new GZIPInputStream(is);
    }

    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception {
        final OutputStream os = getContainer().getContent().getOutputStream(false);
        return new GZIPOutputStream(os);
    }

    private static GzipFileSystem cast(final CompressedFileFileSystem fs) {
        if (fs instanceof GzipFileSystem) {
            return (GzipFileSystem) fs;
        }
        throw new IllegalArgumentException("GzipFileObject expects an instance of GzipFileSystem");
    }
}
