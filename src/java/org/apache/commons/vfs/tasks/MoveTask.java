/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.tasks;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.Selectors;

/**
 * An Ant task that moves matching files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.7 $ $Date: 2004/02/28 03:35:52 $
 *
 * @todo Delete matching folders
 */
public class MoveTask
    extends CopyTask
{
    /**
     * Handles a single source file.
     */
    protected void handleOutOfDateFile( final FileObject srcFile,
                                        final FileObject destFile )
        throws FileSystemException
    {
        super.handleOutOfDateFile( srcFile, destFile );
        log( "Deleting " + srcFile );
        srcFile.delete( Selectors.SELECT_SELF );
    }
}
