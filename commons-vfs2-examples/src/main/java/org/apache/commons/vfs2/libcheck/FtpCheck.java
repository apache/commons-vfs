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
package org.apache.commons.vfs2.libcheck;

import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Basic check for FTP.
 */
public final class FtpCheck {
    private FtpCheck() {
        /* main class not instantiated. */
    }

    public static void main(final String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("Usage: FtpCheck user pass host dir");
        }
        final String user = args[0];
        final String pass = args[1];
        final String host = args[2];
        String dir = null;
        if (args.length == 4) {
            dir = args[3];
        }

        final FTPClient client = new FTPClient();
        client.connect(host);
        final int reply = client.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            throw new IllegalArgumentException("cant connect: " + reply);
        }
        if (!client.login(user, pass)) {
            throw new IllegalArgumentException("login failed");
        }
        client.enterLocalPassiveMode();

        final OutputStream os = client.storeFileStream(dir + "/test.txt");
        if (os == null) {
            throw new IllegalStateException(client.getReplyString());
        }
        os.write("test".getBytes(Charset.defaultCharset()));
        os.close();
        client.completePendingCommand();

        if (dir != null && !client.changeWorkingDirectory(dir)) {
            throw new IllegalArgumentException("change dir to '" + dir + "' failed");
        }

        System.err.println("System: " + client.getSystemType());

        final FTPFile[] files = client.listFiles();
        for (int i = 0; i < files.length; i++) {
            final FTPFile file = files[i];
            if (file == null) {
                System.err.println("#" + i + ": " + null);
            } else {
                System.err.println("#" + i + ": " + file.getRawListing());
                System.err.println("#" + i + ": " + file.toString());
                System.err.println("\t name:" + file.getName() + " type:" + file.getType());
            }
        }
        client.disconnect();
    }
}
