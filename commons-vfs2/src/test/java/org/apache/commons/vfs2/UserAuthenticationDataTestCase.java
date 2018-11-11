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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class UserAuthenticationDataTestCase {
    @Test
    public void testCharacterBasedData() {
        final UserAuthenticationData data = new UserAuthenticationData();
        final char[] array = "PMC".toCharArray();
        data.setData(UserAuthenticationData.USERNAME, array);
        data.setData(UserAuthenticationData.DOMAIN, "Apache".toCharArray());
        assertSame(array, data.getData(UserAuthenticationData.USERNAME));
        assertArrayEquals("Apache".toCharArray(), data.getData(UserAuthenticationData.DOMAIN));
        data.setData(UserAuthenticationData.DOMAIN, "Apache Commons".toCharArray());
        assertArrayEquals("Apache Commons".toCharArray(), data.getData(UserAuthenticationData.DOMAIN));
        assertNull(data.getData(UserAuthenticationData.PASSWORD));

        data.cleanup();
        assertNull(data.getData(UserAuthenticationData.USERNAME));
        assertNull(data.getData(UserAuthenticationData.DOMAIN));
        final char[] nulls = { 0, 0, 0 };
        assertArrayEquals(nulls, array);
    }

    @Test
    public void testCustomType() {
        final UserAuthenticationData.Type type = new UserAuthenticationData.Type("JUNIT");
        final UserAuthenticationData data = new UserAuthenticationData();
        final char[] array = "test".toCharArray();
        data.setData(type, array);
        assertSame(array, data.getData(type));
    }
}
