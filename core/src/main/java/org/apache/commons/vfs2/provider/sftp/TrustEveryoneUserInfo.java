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
package org.apache.commons.vfs2.provider.sftp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.UserInfo;

/**
 * Helper class to trust a new host.
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public class TrustEveryoneUserInfo implements UserInfo
{
    private final Log log = LogFactory.getLog(TrustEveryoneUserInfo.class);

    public String getPassphrase()
    {
        return null;
    }

    public String getPassword()
    {
        return null;
    }

    public boolean promptPassword(String s)
    {
        log.info(s + " - Answer: False");
        return false;
    }

    public boolean promptPassphrase(String s)
    {
        log.info(s + " - Answer: False");
        return false;
    }

    public boolean promptYesNo(String s)
    {
        log.debug(s + " - Answer: Yes");

        // trust
        return true;
    }

    public void showMessage(String s)
    {
        log.debug(s);
    }
}
