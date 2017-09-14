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
package org.apache.commons.vfs2;

/**
 * Creates {@link FileContentInfo} instances to determine the content-info for given file contents.
 */
public interface FileContentInfoFactory {
    /**
     * Creates a FileContentInfo for a the given FileContent.
     *
     * @param fileContent Use this FileContent to create a matching FileContentInfo
     * @return a FileContentInfo for the given FileContent.
     * @throws FileSystemException when a problem occurs creating the FileContentInfo.
     */
    FileContentInfo create(FileContent fileContent) throws FileSystemException;
}
