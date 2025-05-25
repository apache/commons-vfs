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
package org.apache.commons.vfs2.provider;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.RandomAccessContent;

/**
 * Holds the data which needs to be local to the current thread
 */
final class FileContentThreadData {

    private ArrayList<InputStream> inputStreamList;
    private ArrayList<RandomAccessContent> randomAccessContentList;
    private DefaultFileContent.FileContentOutputStream outputStream;

    FileContentThreadData() {
    }

    void add(final InputStream inputStream) {
        if (inputStreamList == null) {
            inputStreamList = new ArrayList<>();
        }
        inputStreamList.add(inputStream);
    }

    void add(final RandomAccessContent randomAccessContent) {
        if (randomAccessContentList == null) {
            randomAccessContentList = new ArrayList<>();
        }
        randomAccessContentList.add(randomAccessContent);
    }

    /**
     * Closes the output stream.
     *
     * @throws FileSystemException if an IO error occurs.
     */
    void closeOutputStream() throws FileSystemException {
        outputStream.close();
        outputStream = null;
    }

    DefaultFileContent.FileContentOutputStream getOutputStream() {
        return outputStream;
    }

    boolean hasInputStream() {
        return inputStreamList != null && !inputStreamList.isEmpty();
    }

    boolean hasRandomAccessContent() {
        return randomAccessContentList != null && !randomAccessContentList.isEmpty();
    }

    boolean hasStreams() {
        return hasInputStream() || outputStream != null || hasRandomAccessContent();
    }

    void remove(final InputStream inputStream) {
        // this null-check (as well as the one in the other `remove` method) should not
        // be needed because `remove` is called only in `DefaultFileContent.endInput` which
        // should only be called after an input stream has been created and hence the `inputStreamList`
        // variable initialized. However, `DefaultFileContent` uses this class per thread -
        // so it is possible to get a stream, pass it to another thread and close it there -
        // and that would lead to a NPE here if it weren't for that check. This "solution" here -
        // adding a null-check - is really "bad" in the sense that it will fix a crash but there will
        // be a leak because the input stream won't be removed from the original thread's `inputStreamList`.
        // See https://github.com/apache/commons-vfs/pull/166 for more context.
        // TODO: fix this problem
        if (inputStreamList != null) {
            inputStreamList.remove(inputStream);
        }
    }

    void remove(final RandomAccessContent randomAccessContent) {
        if (randomAccessContentList != null) {
            randomAccessContentList.remove(randomAccessContent);
        }
    }

    InputStream removeInputStream(final int pos) {
        return inputStreamList.remove(pos);
    }

    Object removeRandomAccessContent(final int pos) {
        return randomAccessContentList.remove(pos);
    }

    void setOutputStream(final DefaultFileContent.FileContentOutputStream outputStream) {
        this.outputStream = outputStream;
    }
}
