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
package org.apache.commons.vfs2.provider.ftps;

/**
 * Mode of the FTPS connection.
 *
 * <p>
 * Note, that 'implicit' mode is not standardized and considered as deprecated. Some unit tests for VFS fail with
 * 'implicit' mode and it is not yet clear if its a problem with Commons VFS/Commons Net or our test server Apache
 * FTP/SSHD.
 * </p>
 *
 * @see <a href="http://en.wikipedia.org/wiki/FTPS#Implicit">Wikipedia: FTPS/Implicit</a>
 * @since 2.1
 */
public enum FtpsMode {
    IMPLICIT, EXPLICIT
}
