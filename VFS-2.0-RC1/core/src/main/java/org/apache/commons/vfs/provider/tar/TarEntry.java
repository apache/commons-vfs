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
package org.apache.commons.vfs.provider.tar;

import java.io.File;
import java.util.Date;
import java.util.Locale;

/**
 * This class represents an entry in a Tar archive. It consists of the entry's
 * header, as well as the entry's File. Entries can be instantiated in one of
 * three ways, depending on how they are to be used. <p>
 * <p/>
 * TarEntries that are created from the header bytes read from an archive are
 * instantiated with the TarEntry( byte[] ) constructor. These entries will be
 * used when extracting from or listing the contents of an archive. These
 * entries have their header filled in using the header bytes. They also set the
 * File to null, since they reference an archive entry not a file. <p>
 * <p/>
 * TarEntries that are created from Files that are to be written into an archive
 * are instantiated with the TarEntry( File ) constructor. These entries have
 * their header filled in using the File's information. They also keep a
 * reference to the File for convenience when writing entries. <p>
 * <p/>
 * Finally, TarEntries can be constructed from nothing but a name. This allows
 * the programmer to construct the entry by hand, for instance when only an
 * InputStream is available for writing to the archive, and the header
 * information is constructed from other information. In this case the header
 * fields are set to defaults and the File is set to null. <p>
 * <p/>
 * The C structure for a Tar Entry's header is: <pre>
 * struct header {
 * char name[NAMSIZ];
 * char mode[8];
 * char uid[8];
 * char gid[8];
 * char size[12];
 * char mtime[12];
 * char chksum[8];
 * char linkflag;
 * char linkname[NAMSIZ];
 * char magic[8];
 * char uname[TUNMLEN];
 * char gname[TGNMLEN];
 * char devmajor[8];
 * char devminor[8];
 * } header;
 * </pre>
 *
 * @author <a href="mailto:time@ice.com">Timothy Gerard Endres</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version $Revision$ $Date$
 * @see TarInputStream
 */
class TarEntry
{
    /**
     * The length of the name field in a header buffer.
     */
    public static final int NAMELEN = 100;

    /**
     * The entry's modification time.
     */
    private int checkSum;

    /**
     * The entry's group name.
     */
    private int devMajor;

    /**
     * The entry's major device number.
     */
    private int devMinor;

    /**
     * The entry's minor device number.
     */
    private File file;

    /**
     * The entry's user id.
     */
    private int groupID;

    /**
     * The entry's user name.
     */
    private StringBuffer groupName;

    /**
     * The entry's checksum.
     */
    private byte linkFlag;

    /**
     * The entry's link flag.
     */
    private StringBuffer linkName;

    /**
     * The entry's link name.
     */
    private StringBuffer magic;

    /**
     * The entry's size.
     */
    private long modTime;

    /**
     * The entry's name.
     */
    private int mode;

    private StringBuffer name;

    /**
     * The entry's group id.
     */
    private long size;

    /**
     * The entry's permission mode.
     */
    private int userID;

    /**
     * The entry's magic tag.
     */
    private StringBuffer userName;

    /**
     * Construct an entry with only a name. This allows the programmer to
     * construct the entry's header "by hand". File is set to null.
     *
     * @param name the name of the entry
     */
    TarEntry(final String name)
    {
        this();

        final boolean isDir = name.endsWith("/");

        this.name = new StringBuffer(name);
        mode = isDir ? 040755 : 0100644;
        linkFlag = isDir ? TarConstants.LF_DIR : TarConstants.LF_NORMAL;
        modTime = (new Date()).getTime() / 1000;
        linkName = new StringBuffer("");
        userName = new StringBuffer("");
        groupName = new StringBuffer("");
    }

    /**
     * Construct an entry with a name an a link flag.
     *
     * @param name     Description of Parameter
     * @param linkFlag Description of Parameter
     */
    TarEntry(final String name, final byte linkFlag)
    {
        this(name);
        this.linkFlag = linkFlag;
    }

    /**
     * Construct an entry for a file. File is set to file, and the header is
     * constructed from information from the file.
     *
     * @param file The file that the entry represents.
     */
    TarEntry(final File file)
    {
        this();

        this.file = file;

        String name = file.getPath();

        // Strip off drive letters!
        final String osName =
                System.getProperty("os.name").toLowerCase(Locale.US);
        if (-1 != osName.indexOf("netware"))
        {
            if (name.length() > 2)
            {
                final char ch1 = name.charAt(0);
                final char ch2 = name.charAt(1);

                if (ch2 == ':' &&
                        ((ch1 >= 'a' && ch1 <= 'z') ||
                                (ch1 >= 'A' && ch1 <= 'Z')))
                {
                    name = name.substring(2);
                }
            }
        }
        else if (-1 != osName.indexOf("netware"))
        {
            final int colon = name.indexOf(':');
            if (colon != -1)
            {
                name = name.substring(colon + 1);
            }
        }

        name = name.replace(File.separatorChar, '/');

        // No absolute pathnames
        // Windows (and Posix?) paths can start with "\\NetworkDrive\",
        // so we loop on starting /'s.
        while (name.startsWith("/"))
        {
            name = name.substring(1);
        }

        linkName = new StringBuffer("");
        this.name = new StringBuffer(name);

        if (file.isDirectory())
        {
            mode = 040755;
            linkFlag = TarConstants.LF_DIR;

            if (this.name.charAt(this.name.length() - 1) != '/')
            {
                this.name.append("/");
            }
        }
        else
        {
            mode = 0100644;
            linkFlag = TarConstants.LF_NORMAL;
        }

        size = file.length();
        modTime = file.lastModified() / 1000;
        checkSum = 0;
        devMajor = 0;
        devMinor = 0;
    }

    /**
     * Construct an entry from an archive's header bytes. File is set to null.
     *
     * @param header The header bytes from a tar archive entry.
     */
    TarEntry(final byte[] header)
    {
        this();
        parseTarHeader(header);
    }

    /**
     * Construct an empty entry and prepares the header values.
     */
    private TarEntry()
    {
        magic = new StringBuffer(TarConstants.TMAGIC);
        name = new StringBuffer();
        linkName = new StringBuffer();

        String user = System.getProperty("user.name", "");
        if (user.length() > 31)
        {
            user = user.substring(0, 31);
        }

        userName = new StringBuffer(user);
        groupName = new StringBuffer("");
    }

    /**
     * Set this entry's group id.
     *
     * @param groupId This entry's new group id.
     */
    public void setGroupID(final int groupId)
    {
        groupID = groupId;
    }

    /**
     * Set this entry's group id.
     *
     * @param groupId This entry's new group id.
     * @see #setGroupID(int)
     * @deprecated Use setGroupID() instead
     */
    public void setGroupId(final int groupId)
    {
        groupID = groupId;
    }

    /**
     * Set this entry's group name.
     *
     * @param groupName This entry's new group name.
     */
    public void setGroupName(final String groupName)
    {
        this.groupName = new StringBuffer(groupName);
    }

    /**
     * Set this entry's modification time. The parameter passed to this method
     * is in "Java time".
     *
     * @param time This entry's new modification time.
     */
    public void setModTime(final long time)
    {
        modTime = time / 1000;
    }

    /**
     * Set this entry's modification time.
     *
     * @param time This entry's new modification time.
     */
    public void setModTime(final Date time)
    {
        modTime = time.getTime() / 1000;
    }

    /**
     * Set the mode for this entry
     *
     * @param mode The new Mode value
     */
    public void setMode(final int mode)
    {
        this.mode = mode;
    }

    /**
     * Set this entry's name.
     *
     * @param name This entry's new name.
     */
    public void setName(final String name)
    {
        this.name = new StringBuffer(name);
    }

    /**
     * Set this entry's file size.
     *
     * @param size This entry's new file size.
     */
    public void setSize(final long size)
    {
        this.size = size;
    }

    /**
     * Set this entry's user id.
     *
     * @param userId This entry's new user id.
     */
    public void setUserID(final int userId)
    {
        userID = userId;
    }

    /**
     * Set this entry's user id.
     *
     * @param userId This entry's new user id.
     * @see #setUserID(int)
     * @deprecated Use setUserID() instead
     */
    public void setUserId(final int userId)
    {
        userID = userId;
    }

    /**
     * Set this entry's user name.
     *
     * @param userName This entry's new user name.
     */
    public void setUserName(final String userName)
    {
        this.userName = new StringBuffer(userName);
    }

    /**
     * If this entry represents a file, and the file is a directory, return an
     * array of TarEntries for this entry's children.
     *
     * @return An array of TarEntry's for this entry's children.
     */
    public TarEntry[] getDirectoryEntries()
    {
        if (null == file || !file.isDirectory())
        {
            return new TarEntry[0];
        }

        final String[] list = file.list();
        final TarEntry[] result = new TarEntry[list.length];

        for (int i = 0; i < list.length; ++i)
        {
            result[i] = new TarEntry(new File(file, list[i]));
        }

        return result;
    }

    /**
     * Get this entry's file.
     *
     * @return This entry's file.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * Get this entry's group id.
     *
     * @return This entry's group id.
     * @see #getGroupID()
     * @deprecated Use getGroupID() instead
     */
    public int getGroupId()
    {
        return groupID;
    }

    /**
     * Get this entry's group id.
     *
     * @return This entry's group id.
     */
    public int getGroupID()
    {
        return groupID;
    }

    /**
     * Get this entry's group name.
     *
     * @return This entry's group name.
     */
    public String getGroupName()
    {
        return groupName.toString();
    }

    /**
     * Set this entry's modification time.
     *
     * @return The ModTime value
     */
    public Date getModTime()
    {
        return new Date(modTime * 1000);
    }

    /**
     * Get this entry's mode.
     *
     * @return This entry's mode.
     */
    public int getMode()
    {
        return mode;
    }

    /**
     * Get this entry's name.
     *
     * @return This entry's name.
     */
    public String getName()
    {
        return name.toString();
    }

    /**
     * Get this entry's file size.
     *
     * @return This entry's file size.
     */
    public long getSize()
    {
        return size;
    }

    /**
     * Get this entry's checksum.
     *
     * @return This entry's checksum.
     */
    public int getCheckSum()
    {
        return checkSum;
    }

    /**
     * Get this entry's user id.
     *
     * @return This entry's user id.
     * @see #getUserID()
     * @deprecated Use getUserID() instead
     */
    public int getUserId()
    {
        return userID;
    }

    /**
     * Get this entry's user id.
     *
     * @return This entry's user id.
     */
    public int getUserID()
    {
        return userID;
    }

    /**
     * Get this entry's user name.
     *
     * @return This entry's user name.
     */
    public String getUserName()
    {
        return userName.toString();
    }

    /**
     * Determine if the given entry is a descendant of this entry. Descendancy
     * is determined by the name of the descendant starting with this entry's
     * name.
     *
     * @param desc Entry to be checked as a descendent of
     * @return True if entry is a descendant of
     */
    public boolean isDescendent(final TarEntry desc)
    {
        return desc.getName().startsWith(getName());
    }

    /**
     * Return whether or not this entry represents a directory.
     *
     * @return True if this entry is a directory.
     */
    public boolean isDirectory()
    {
        if (file != null)
        {
            return file.isDirectory();
        }

        if (linkFlag == TarConstants.LF_DIR)
        {
            return true;
        }

        if (getName().endsWith("/"))
        {
            return true;
        }

        return false;
    }

    /**
     * Indicate if this entry is a GNU long name block
     *
     * @return true if this is a long name extension provided by GNU tar
     */
    public boolean isGNULongNameEntry()
    {
        return linkFlag == TarConstants.LF_GNUTYPE_LONGNAME &&
                name.toString().equals(TarConstants.GNU_LONGLINK);
    }

    /**
     * Determine if the two entries are equal. Equality is determined by the
     * header names being equal.
     *
     * @param other Entry to be checked for equality.
     * @return True if the entries are equal.
     */
    public boolean equals(final Object other)
    {
        if (!(other instanceof TarEntry))
        {
            return false;
        }
        TarEntry entry = (TarEntry) other;
        return getName().equals(entry.getName());
    }

    public int hashCode()
    {
        return getName().hashCode();
    }

    /**
     * Parse an entry's header information from a header buffer.
     *
     * @param header The tar entry header buffer to get information from.
     */
    private void parseTarHeader(final byte[] header)
    {
        int offset = 0;

        name = TarUtils.parseName(header, offset, NAMELEN);
        offset += NAMELEN;
        mode = (int) TarUtils.parseOctal(header, offset, TarConstants.MODELEN);
        offset += TarConstants.MODELEN;
        userID = (int) TarUtils.parseOctal(header, offset, TarConstants.UIDLEN);
        offset += TarConstants.UIDLEN;
        groupID = (int) TarUtils.parseOctal(header, offset, TarConstants.GIDLEN);
        offset += TarConstants.GIDLEN;
        size = TarUtils.parseOctal(header, offset, TarConstants.SIZELEN);
        offset += TarConstants.SIZELEN;
        modTime = TarUtils.parseOctal(header, offset, TarConstants.MODTIMELEN);
        offset += TarConstants.MODTIMELEN;
        checkSum = (int) TarUtils.parseOctal(header, offset, TarConstants.CHKSUMLEN);
        offset += TarConstants.CHKSUMLEN;
        linkFlag = header[offset++];
        linkName = TarUtils.parseName(header, offset, NAMELEN);
        offset += NAMELEN;
        magic = TarUtils.parseName(header, offset, TarConstants.MAGICLEN);
        offset += TarConstants.MAGICLEN;
        userName = TarUtils.parseName(header, offset, TarConstants.UNAMELEN);
        offset += TarConstants.UNAMELEN;
        groupName = TarUtils.parseName(header, offset, TarConstants.GNAMELEN);
        offset += TarConstants.GNAMELEN;
        devMajor = (int) TarUtils.parseOctal(header, offset, TarConstants.DEVLEN);
        offset += TarConstants.DEVLEN;
        devMinor = (int) TarUtils.parseOctal(header, offset, TarConstants.DEVLEN);
    }

    /**
     * Write an entry's header information to a header buffer.
     *
     * @param buffer The tar entry header buffer to fill in.
     */
    public void writeEntryHeader(final byte[] buffer)
    {
        int offset = 0;

        offset = TarUtils.getNameBytes(name, buffer, offset, NAMELEN);
        offset = TarUtils.getOctalBytes(mode, buffer, offset, TarConstants.MODELEN);
        offset = TarUtils.getOctalBytes(userID, buffer, offset, TarConstants.UIDLEN);
        offset = TarUtils.getOctalBytes(groupID, buffer, offset, TarConstants.GIDLEN);
        offset = TarUtils.getLongOctalBytes(size, buffer, offset, TarConstants.SIZELEN);
        offset = TarUtils.getLongOctalBytes(modTime, buffer, offset, TarConstants.MODTIMELEN);

        final int checkSumOffset = offset;
        for (int i = 0; i < TarConstants.CHKSUMLEN; ++i)
        {
            buffer[offset++] = (byte) ' ';
        }

        buffer[offset++] = linkFlag;
        offset = TarUtils.getNameBytes(linkName, buffer, offset, NAMELEN);
        offset = TarUtils.getNameBytes(magic, buffer, offset, TarConstants.MAGICLEN);
        offset = TarUtils.getNameBytes(userName, buffer, offset, TarConstants.UNAMELEN);
        offset = TarUtils.getNameBytes(groupName, buffer, offset, TarConstants.GNAMELEN);
        offset = TarUtils.getOctalBytes(devMajor, buffer, offset, TarConstants.DEVLEN);
        offset = TarUtils.getOctalBytes(devMinor, buffer, offset, TarConstants.DEVLEN);

        while (offset < buffer.length)
        {
            buffer[offset++] = 0;
        }

        final long checkSum = TarUtils.computeCheckSum(buffer);
        TarUtils.getCheckSumOctalBytes(checkSum, buffer, checkSumOffset, TarConstants.CHKSUMLEN);
    }
}
