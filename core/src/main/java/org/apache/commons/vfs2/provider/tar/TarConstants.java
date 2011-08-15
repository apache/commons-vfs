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
package org.apache.commons.vfs2.provider.tar;

/**
 * This class contains all the definitions used in the package.
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
final class TarConstants
{
    /**
     * The length of the mode field in a header buffer.
     */
    public static final int MODELEN = 8;

    /**
     * The length of the user id field in a header buffer.
     */
    public static final int UIDLEN = 8;

    /**
     * The length of the group id field in a header buffer.
     */
    public static final int GIDLEN = 8;

    /**
     * The length of the checksum field in a header buffer.
     */
    public static final int CHKSUMLEN = 8;

    /**
     * The length of the size field in a header buffer.
     */
    public static final int SIZELEN = 12;

    /**
     * The length of the magic field in a header buffer.
     */
    public static final int MAGICLEN = 8;

    /**
     * The length of the modification time field in a header buffer.
     */
    public static final int MODTIMELEN = 12;

    /**
     * The length of the user name field in a header buffer.
     */
    public static final int UNAMELEN = 32;

    /**
     * The length of the group name field in a header buffer.
     */
    public static final int GNAMELEN = 32;

    /**
     * The length of the devices field in a header buffer.
     */
    public static final int DEVLEN = 8;

    /**
     * LF_ constants represent the "link flag" of an entry, or more commonly,
     * the "entry type". This is the "old way" of indicating a normal file.
     */
    public static final byte LF_OLDNORM = 0;

    /**
     * Normal file type.
     */
    public static final byte LF_NORMAL = (byte) '0';

    /**
     * Link file type.
     */
    public static final byte LF_LINK = (byte) '1';

    /**
     * Symbolic link file type.
     */
    public static final byte LF_SYMLINK = (byte) '2';

    /**
     * Character device file type.
     */
    public static final byte LF_CHR = (byte) '3';

    /**
     * Block device file type.
     */
    public static final byte LF_BLK = (byte) '4';

    /**
     * Directory file type.
     */
    public static final byte LF_DIR = (byte) '5';

    /**
     * FIFO (pipe) file type.
     */
    public static final byte LF_FIFO = (byte) '6';

    /**
     * Contiguous file type.
     */
    public static final byte LF_CONTIG = (byte) '7';

    /**
     * The magic tag representing a POSIX tar archive.
     */
    public static final String TMAGIC = "ustar";

    /**
     * The magic tag representing a GNU tar archive.
     */
    public static final String GNU_TMAGIC = "ustar  ";

    /**
     * The namr of the GNU tar entry which contains a long name.
     */
    public static final String GNU_LONGLINK = "././@LongLink";

    /**
     * Identifies the *next* file on the tape as having a long name.
     */
    public static final byte LF_GNUTYPE_LONGNAME = (byte) 'L';

    /**
     * Prevent instantiation
     */
    private TarConstants()
    {
    }
}
