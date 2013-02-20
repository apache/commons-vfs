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
package org.apache.commons.vfs2.provider.ftps.test;

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.ftps.FtpsFileSystemConfigBuilder;

/**
 * Tests VFS-412.
 */
public class FtpsProviderPROTTestCase_Disabled extends FtpsProviderImplicitTestCase_Disabled
{

    @Override
    protected FileSystemOptions getFileSystemOptions()
    {
        if (fileSystemOptions == null)
        {
            fileSystemOptions = new FileSystemOptions();
            final FtpsFileSystemConfigBuilder builder = FtpsFileSystemConfigBuilder.getInstance();
            builder.setPassiveMode(fileSystemOptions, true);
            builder.setDataTimeout(fileSystemOptions, Integer.valueOf(2000));
            builder.setFtpsType(fileSystemOptions, FtpsFileSystemConfigBuilder.FTPS_TYPE_IMPLICIT);
            // For VFS-412
            // builder.setDataChannelProtectionLevel(fileSystemOptions, "P");
        }
        return fileSystemOptions;
    }

}
