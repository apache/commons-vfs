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
package org.apache.commons.vfs2;

import java.util.Properties;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;

public class RunTest {
    public static void main(final String[] args) throws Exception {
        final String ip = "192.168.0.128";

        final Properties props = System.getProperties();
        props.setProperty("test.data.src", "src/test-data");
        props.setProperty("test.basedir", "core/target/test-classes/test-data");
        props.setProperty("test.basedir.res", "test-data");
        props.setProperty("test.policy", "src/test-data/test.policy");
        props.setProperty("test.secure", "false");
        props.setProperty("test.smb.uri", "smb://HOME\\vfsusr:vfs%2f%25\\te:st@" + ip + "/vfsusr/vfstest");
        props.setProperty("test.ftp.uri", "ftp://vfsusr:vfs%2f%25\\te:st@" + ip + "/vfstest");
        props.setProperty("test.ftps.uri", "ftps://vfsusr:vfs%2f%25\\te:st@" + ip + "/vfstest");

        props.setProperty("test.http.uri", "http://" + ip + "/vfstest");
        props.setProperty("test.webdav.uri", "webdav://vfsusr:vfs%2f%25\\te:st@" + ip + "/vfstest");
        props.setProperty("test.sftp.uri", "sftp://vfsusr:vfs%2f%25\\te:st@" + ip + "/vfstest");

        final Test tests[] = new Test[] {
                // LocalProviderTestCase.suite(),
                // FtpProviderTestCase.suite(),
                // UrlProviderHttpTestCase.suite(),
                // VirtualProviderTestCase.suite(),
                // TemporaryProviderTestCase.suite(),
                // UrlProviderTestCase.suite(),
                // ResourceProviderTestCase.suite(),
                // HttpProviderTestCase.suite(),
                // SftpProviderTestCase.suite(),
                // JarProviderTestCase.suite(),
                // NestedJarTestCase.suite(),
                // ZipProviderTestCase.suite(),
                // NestedZipTestCase.suite(),
                // TarProviderTestCase.suite(),
                // TgzProviderTestCase.suite(),
                // Tbz2ProviderTestCase.suite(),
                // NestedTarTestCase.suite(),
                // NestedTgzTestCase.suite(),
                // NestedTbz2TestCase.suite(),
                // RamProviderTestCase.suite(),

                // SmbProviderTestCase.suite(),
                // WebdavProviderTestCase.suite(),
        };

        final TestResult result = new TestResult() {
            @Override
            public void startTest(final Test test) {
                System.out.println("start " + test);
                System.out.flush();
            }

            @Override
            public void endTest(final Test test) {
                // System.err.println("end " + test);
            }

            @Override
            public synchronized void addError(final Test test, final Throwable throwable) {
                // throw new RuntimeException(throwable.getMessage());
                throwable.printStackTrace();
            }

            @Override
            public synchronized void addFailure(final Test test, final AssertionFailedError assertionFailedError) {
                // throw new RuntimeException(assertionFailedError.getMessage());
                assertionFailedError.printStackTrace();
            }
        };

        for (int i = 0; i < tests.length; i++) {
            System.out.println("start test#" + i);
            System.out.flush();

            final Test test = tests[i];
            test.run(result);

            // break;
        }
    }
}
