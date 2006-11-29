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
package org.apache.commons.vfs.provider.local;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileName;

/**
 * A local file URI.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class LocalFileName extends AbstractFileName
{
    private final String rootFile;

    protected LocalFileName(final String scheme,
                            final String rootFile,
                            final String path,
                            final FileType type)
    {
        super(scheme, path, type);
        this.rootFile = rootFile;
    }

    /**
     * Returns the root file for this file.
     */
    public String getRootFile()
    {
        return rootFile;
    }

    /**
     * Factory method for creating name instances.
     */
    public FileName createName(final String path, FileType type)
    {
        return new LocalFileName(getScheme(), rootFile, path, type);
    }

    /**
     * Builds the root URI for this file name.
     */
    protected void appendRootUri(final StringBuffer buffer, boolean addPassword)
    {
        buffer.append(getScheme());
        buffer.append("://");
        buffer.append(rootFile);
    }
}
