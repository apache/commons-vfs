/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
 * A Gzip file.
 */
public class GzipFileObject extends CompressedFileFileObject<GzipFileSystem> {

    private static GzipFileSystem cast(final CompressedFileFileSystem fs) {
        if (fs instanceof GzipFileSystem) {
            return (GzipFileSystem) fs;
        }
        throw new IllegalArgumentException("GzipFileObject expects an instance of GzipFileSystem");
    }

    /**
     * Deprecated since 2.1.
     *
     * @param name Abstract file name.
     * @param container My container.
     * @param fs My file system.
     * @deprecated Use {@link #GzipFileObject(AbstractFileName, FileObject, GzipFileSystem)} instead.
     */
    @Deprecated
    protected GzipFileObject(final AbstractFileName name, final FileObject container,
            final CompressedFileFileSystem fs) {
        super(name, container, cast(fs));
    }

    /**
     * Constructs a new instance.
     *
     * @param fileName the file name.
     * @param container the container.
     * @param fileSystem the file system.
     */
    protected GzipFileObject(final AbstractFileName fileName, final FileObject container, final GzipFileSystem fileSystem) {
        super(fileName, container, fileSystem);
    }

    @Override
    protected InputStream doGetInputStream(final int bufferSize) throws Exception {
        return new GZIPInputStream(getContainer().getContent().getInputStream(), bufferSize);
    }

    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception {
        return new GZIPOutputStream(getContainer().getContent().getOutputStream(false));
    }
}
