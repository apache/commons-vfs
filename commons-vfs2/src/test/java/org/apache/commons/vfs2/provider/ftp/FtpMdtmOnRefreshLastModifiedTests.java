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
package org.apache.commons.vfs2.provider.ftp;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.LastModifiedTests;
import org.junit.jupiter.api.Assertions;
import org.junit.Test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class FtpMdtmOnRefreshLastModifiedTests extends LastModifiedTests {

    /**
     * Tests {@link FileContent#getLastModifiedTime()} re-calls {@link FtpClient#mdtmInstant(String)} after refresh.
     */
    @Test
    public void testGetLastModifiedFileExactMatchRefresh() throws IOException {
        final String fileName = "file1.txt";
        final FileObject readFolder = getReadFolder();
        final FtpFileObject fileObject = (FtpFileObject) readFolder.resolveFile(fileName);

        returnsCorrectMdtmValue(fileObject);
        fileObject.refresh();
        returnsCorrectMdtmValue(fileObject);
    }

    private void returnsCorrectMdtmValue(final FtpFileObject fileObject) throws IOException {
        final String relPath = fileObject.getRelPath();
        final FtpClient ftpClient = spyClient(fileObject);

        final long expected = ThreadLocalRandom.current().nextLong(Instant.now().toEpochMilli());
        when(ftpClient.mdtmInstant(relPath)).thenReturn(Instant.ofEpochMilli(expected));

        final long lastModTIme = fileObject.getContent().getLastModifiedTime();

        if (expected != lastModTIme) {
            Assertions.fail(String.format("%s returned epoch %s not expected: %s.",
                    FtpFileObject.class.getSimpleName(), lastModTIme, expected));
        }
    }

    private FtpClient spyClient(final FtpFileObject fileObject) throws FileSystemException {
        final FtpFileSystem fileSystem = (FtpFileSystem) fileObject.getFileSystem();
        final FtpClient ftpClientSpy = spy(fileSystem.getClient());
        fileSystem.putClient(ftpClientSpy);
        return ftpClientSpy;
    }

}
