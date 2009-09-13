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
package org.apache.commons.vfs.provider.sftp;

import java.io.Serializable;

/**
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @version $Revision:  $
 */
public class ProxyType implements Serializable, Comparable
{
    private final String proxyType;

    ProxyType(final String proxyType)
    {
        this.proxyType = proxyType;
    }

    public int compareTo(Object o)
    {
        return proxyType.compareTo(((ProxyType) o).proxyType);
    }


    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ProxyType proxyType1 = (ProxyType) o;

        if (proxyType != null ? !proxyType.equals(proxyType1.proxyType) : proxyType1.proxyType != null)
        {
            return false;
        }

        return true;
    }
}
