package org.apache.commons.vfs2.provider.sftp;

import org.apache.commons.vfs2.util.PosixPermissions;

/**
 * Pretends that the current user is always the owner and in the same group.
 */
public class PretendUserIsOwnerPosixPermissions extends PosixPermissions {

    public PretendUserIsOwnerPosixPermissions(final int permissions) {
        super(permissions, true, true);
    }
}
