/*
 * Created by IntelliJ IDEA.
 * User: im
 * Date: 26.03.2004
 * Time: 08:03:29
 */
package org.apache.commons.vfs.provider.sftp;

import com.jcraft.jsch.UserInfo;

public class TrustEveryoneUserInfo implements UserInfo
{
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
        return false;
    }

    public boolean promptPassphrase(String s)
    {
        return false;
    }

    public boolean promptYesNo(String s)
    {
        // trust
        return true;
    }

    public void showMessage(String s)
    {
    }
}