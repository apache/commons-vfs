package org.apache.commons.vfs2.filter;

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileSelectInfo;

/**
 * Always FALSE.
 */
class TestFalseFileFilter implements FileFilter {

    @Override
    public boolean accept(final FileSelectInfo fileSelectInfo) {
        return false;
    }

}