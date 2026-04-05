/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class UserAuthenticationDataTest {

    private static final char[] DATA = "PMC".toCharArray();
    
    @Test
    public void testCharacterBasedData() {
        final UserAuthenticationData data = new UserAuthenticationData();
        final char[] array = DATA.clone();
        data.setData(UserAuthenticationData.USERNAME, array);
        data.setData(UserAuthenticationData.DOMAIN, "Apache".toCharArray());
        assertArrayEquals(array, data.getData(UserAuthenticationData.USERNAME));
        assertArrayEquals("Apache".toCharArray(), data.getData(UserAuthenticationData.DOMAIN));
        data.setData(UserAuthenticationData.DOMAIN, "Apache Commons".toCharArray());
        assertArrayEquals("Apache Commons".toCharArray(), data.getData(UserAuthenticationData.DOMAIN));
        assertNull(data.getData(UserAuthenticationData.PASSWORD));

        data.cleanup();
        assertNull(data.getData(UserAuthenticationData.USERNAME));
        assertNull(data.getData(UserAuthenticationData.DOMAIN));
        assertArrayEquals(DATA, array);
    }

    @Test
    public void testCustomType() {
        final UserAuthenticationData.Type type = new UserAuthenticationData.Type("JUNIT");
        final UserAuthenticationData data = new UserAuthenticationData();
        final char[] array = DATA.clone();
        data.setData(type, array);
        assertArrayEquals(array, data.getData(type));
    }

}
