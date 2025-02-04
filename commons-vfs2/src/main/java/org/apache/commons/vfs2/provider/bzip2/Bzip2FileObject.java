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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.compressed.CompressedFileFileObject;
import org.apache.commons.vfs2.provider.compressed.CompressedFileFileSystem;

/**
 * the bzip2 file.
 */
public class Bzip2FileObject extends CompressedFileFileObject<Bzip2FileSystem> {

    private static Bzip2FileSystem cast(final CompressedFileFileSystem fs) {
        if (fs instanceof Bzip2FileSystem) {
            return (Bzip2FileSystem) fs;
        }
        throw new IllegalArgumentException("Bzip2FileObject requires a Bzip2FileSystem implementation");
    }

    /**
     * Wraps an input stream in a compressor input stream.
     *
     * @param name Unused.
     * @param inputStream The input stream to wrap.
     * @return a new compressor input stream.
     * @throws IOException if the stream content is malformed or an I/O error occurs.
     */
    public static InputStream wrapInputStream(final String name, final InputStream inputStream) throws IOException {
        return new BZip2CompressorInputStream(inputStream);
    }

    /**
     * Constructs a new instance.
     *
     * @param fileName the file name.
     * @param container the container.
     * @param fileSystem the file system.
     */
    protected Bzip2FileObject(final AbstractFileName fileName, final FileObject container, final Bzip2FileSystem fileSystem) {
        super(fileName, container, fileSystem);
    }

    /**
     * Deprecated since 2.1.
     *
     * @param name Abstract file name.
     * @param container My container.
     * @param fs My file system.
     * @deprecated Use {@link #Bzip2FileObject(AbstractFileName, FileObject, Bzip2FileSystem)} instead.
     */
    @Deprecated
    protected Bzip2FileObject(final AbstractFileName name, final FileObject container,
            final CompressedFileFileSystem fs) {
        super(name, container, cast(fs));
    }

    @Override
    protected InputStream doGetInputStream(final int bufferSize) throws Exception {
        // check file
        return wrapInputStream(getName().getURI(), getContainer().getContent().getInputStream(bufferSize));
    }

    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception {
        return new BZip2CompressorOutputStream(getContainer().getContent().getOutputStream(false));
    }
}
