/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.vfs;

/**
 * Several standard file selectors.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/10/23 11:59:39 $
 */
public interface Selectors
{
    /**
     * A {@link FileSelector} that selects only the base file/folder.
     */
    FileSelector SELECT_SELF = new FileDepthSelector( 0, 0 );

    /**
     * A {@link FileSelector} that selects the base file/folder and its
     * direct children.
     */
    FileSelector SELECT_SELF_AND_CHILDREN = new FileDepthSelector( 0, 1 );

    /**
     * A {@link FileSelector} that selects only the direct children
     * of the base folder.
     */
    FileSelector SELECT_CHILDREN = new FileDepthSelector( 1, 1 );

    /**
     * A {@link FileSelector} that selects all the descendents of the
     * base folder, but does not select the base folder itself.
     */
    FileSelector EXCLUDE_SELF = new FileDepthSelector( 1, Integer.MAX_VALUE );

    /**
     * A {@link FileSelector} that only files (not folders).
     */
    FileSelector SELECT_FILES = new FileTypeSelector( FileType.FILE );

    /**
     * A {@link FileSelector} that only folders (not files).
     */
    FileSelector SELECT_FOLDERS = new FileTypeSelector( FileType.FOLDER );

    /**
     * A {@link FileSelector} that selects the base file/folder, plus all
     * its descendents.
     */
    FileSelector SELECT_ALL = new AllFileSelector();
}
