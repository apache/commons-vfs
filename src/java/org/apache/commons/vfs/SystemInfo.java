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
package org.apache.commons.vfs;

import java.util.Collection;

/**
 * Provides some runtime-info about the currently running vfs system
 * 
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision: 1.2 $ $Date: 2004/05/21 20:54:34 $
 */
public interface SystemInfo
{
    /**
     * retrieve the currently available schemes
     */
    public String[] getSchemes();

    /**
     * retrieve the capabilities of the provider responsible for the scheme.<br>
     * These are the same as the ones on the filesystem, but available before a filesystem was instantiated.
     *
     * @throws FileSystemException if no provider for the given scheme is available
     */
    public Collection getProviderCapabilities(String scheme) throws FileSystemException;
}
