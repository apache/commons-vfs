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
package org.apache.commons.vfs2.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.apache.commons.vfs2.FileObject;

/**
 * This implementation caches every file as long as it is strongly reachable by the java vm. As soon as the object is no
 * longer reachable it will be discarded. In contrast to the SoftRefFilesCache this implementation might free resources
 * faster as it don't wait until a memory limitation.
 *
 * @see java.lang.ref.WeakReference
 */
public class WeakRefFilesCache extends SoftRefFilesCache {
    @Override
    protected Reference<FileObject> createReference(final FileObject file, final ReferenceQueue<FileObject> refqueue) {
        return new WeakReference<>(file, refqueue);
    }
}
