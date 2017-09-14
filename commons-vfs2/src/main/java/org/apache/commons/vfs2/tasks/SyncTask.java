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
package org.apache.commons.vfs2.tasks;

import org.apache.commons.vfs2.FileObject;

/**
 * A task that synchronises the destination folder to look exactly like the source folder (or folders).
 */
public class SyncTask extends CopyTask {
    /**
     * Handles a destination for which there is no corresponding source file.
     */
    @Override
    protected void handleMissingSourceFile(final FileObject destFile) throws Exception {
        log("deleting " + destFile.getPublicURIString());
        // destFile.delete( Selectors.SELECT_SELF );
    }

    /**
     * Check if this task cares about destination files with a missing source file.
     */
    @Override
    protected boolean detectMissingSourceFiles() {
        return true;
    }
}
