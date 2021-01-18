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
package org.apache.commons.vfs2.provider;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.RandomAccessContent;

/**
 * Holds the data which needs to be local to the current thread
 */
class FileContentThreadData {

    private ArrayList<InputStream> inputStreamList;
    private ArrayList<RandomAccessContent> randomAccessContentList;
    private DefaultFileContent.FileContentOutputStream outputStream;

    FileContentThreadData() {
    }

    void add(final InputStream inputStream) {
        if (this.inputStreamList == null) {
            this.inputStreamList = new ArrayList<>();
        }
        this.inputStreamList.add(inputStream);
    }

    void add(final RandomAccessContent randomAccessContent) {
        if (this.randomAccessContentList == null) {
            this.randomAccessContentList = new ArrayList<>();
        }
        this.randomAccessContentList.add(randomAccessContent);
    }

    /**
     * Closes the output stream.
     *
     * @throws FileSystemException if an IO error occurs.
     * @@since 2.8.0
     */
    void closeOutputStream() throws FileSystemException {
        outputStream.close();
        outputStream = null;
    }

    DefaultFileContent.FileContentOutputStream getOutputStream() {
        return this.outputStream;
    }

    boolean hasInputStream() {
        return this.inputStreamList != null && !this.inputStreamList.isEmpty();
    }

    boolean hasRandomAccessContent() {
        return randomAccessContentList != null && !randomAccessContentList.isEmpty();
    }

    boolean hasStreams() {
        return hasInputStream() || outputStream != null || hasRandomAccessContent();
    }

    InputStream removeInputStream(final int pos) {
        return this.inputStreamList.remove(pos);
    }

    void remove(final InputStream inputStream) {
        this.inputStreamList.remove(inputStream);
    }

    Object removeRandomAccessContent(final int pos) {
        return this.randomAccessContentList.remove(pos);
    }

    void remove(final RandomAccessContent randomAccessContent) {
        this.randomAccessContentList.remove(randomAccessContent);
    }

    void setOutputStream(final DefaultFileContent.FileContentOutputStream outputStream) {
        this.outputStream = outputStream;
    }
}
