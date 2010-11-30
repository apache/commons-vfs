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

import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * Basic check for sftp
 */
public class SftpCheck
{
    public static void main(String args[]) throws Exception
    {
        if (args.length != 4)
        {
            throw new IllegalArgumentException("Usage: SftpCheck user pass host dir");
        }
        String user = args[0];
        String pass = args[1];
        String host = args[2];
        String dir = args[3];

        Properties props = new Properties();
        props.setProperty("StrictHostKeyChecking", "false");
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, 22);
        session.setUserInfo(new UserInfo()
        {
            public String getPassphrase()
            {
                return null;
            }

            public String getPassword()
            {
                return null;
            }

            public boolean promptPassword(String string)
            {
                return false;
            }

            public boolean promptPassphrase(String string)
            {
                return false;
            }

            public boolean promptYesNo(String string)
            {
                return true;
            }

            public void showMessage(String string)
            {
            }
        });
        session.setPassword(pass);
        session.connect();
        ChannelSftp chan = (ChannelSftp) session.openChannel("sftp");
        chan.connect();
        Vector<?> list = chan.ls(dir);
        Iterator<?> iterList = list.iterator();
        while (iterList.hasNext())
        {
            System.err.println(iterList.next());
        }
        System.err.println("done");
        chan.disconnect();
        session.disconnect();
    }
}
