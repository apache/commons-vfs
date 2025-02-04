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
package org.apache.commons.vfs2.events;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;

/**
 * A change event that knows how to notify a listener.
 */
public abstract class AbstractFileChangeEvent extends FileChangeEvent {

    /**
     * Constructs a new instance for subclasses.
     *
     * @param fileObject the file object.
     */
    public AbstractFileChangeEvent(final FileObject fileObject) {
        super(fileObject);
    }

    /**
     * Notifies the given file listener of this event.
     *
     * @param fileListener The file listener to notify.
     * @throws Exception Anything can happen.
     */
    public abstract void notify(FileListener fileListener) throws Exception;
}
