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

import static org.junit.Assert.*;

import org.junit.Test;

public class UserAuthenticationDataTestCase
{
    @Test
    public void testCharacterBasedDataWithLegacyMethods()
    {
        UserAuthenticationData data = new UserAuthenticationData();
        char[] array = "PMC".toCharArray();
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
        char[] nulls = {0,0,0};
        assertArrayEquals(nulls, array);
    }

    @Test
    public void testCharacterBasedData()
    {
        UserAuthenticationData data = new UserAuthenticationData();
        char[] array = "PMC".toCharArray();
        data.setAuthData(UserAuthenticationData.USERNAME, array);
        data.setAuthData(UserAuthenticationData.DOMAIN, "Apache".toCharArray());
        assertSame(array, data.getAuthData(UserAuthenticationData.USERNAME));
        assertArrayEquals("Apache".toCharArray(), data.<char[]>getAuthData(UserAuthenticationData.DOMAIN));
        data.setAuthData(UserAuthenticationData.DOMAIN, "Apache Commons".toCharArray());
        assertArrayEquals("Apache Commons".toCharArray(), data.<char[]>getAuthData(UserAuthenticationData.DOMAIN));
        assertNull(data.getAuthData(UserAuthenticationData.PASSWORD));
        
        data.cleanup();
        assertNull(data.getAuthData(UserAuthenticationData.USERNAME));
        assertNull(data.getAuthData(UserAuthenticationData.DOMAIN));
        char[] nulls = {0,0,0};
        assertArrayEquals(nulls, array);
    }

    @Test
    public void testCustomTypeWithArray()
    {
        UserAuthenticationData.Type type = new UserAuthenticationData.Type("JUNIT", UserAuthenticationDataTestCase[].class); 
        UserAuthenticationData data = new UserAuthenticationData();
        UserAuthenticationDataTestCase[] array = { this };
        data.setAuthData(type, array);
        assertSame(array, data.getAuthData(type));
        
        data.cleanup();
        UserAuthenticationDataTestCase[] nulls = { null };
        assertArrayEquals(nulls, array);
    }

    @Test
    public void testCustomTypeWithHierarchy()
    {
        UserAuthenticationData.Type type = new UserAuthenticationData.Type("JUNIT", CharSequence.class); 
        UserAuthenticationData data = new UserAuthenticationData();
        assertTrue(data.setAuthData(type, "test"));
        assertEquals("test", data.getAuthData(type));
        assertFalse(data.setAuthData(type, Integer.valueOf(42)));
        assertEquals("test", data.getAuthData(type));
        assertTrue(data.setAuthData(type, null));
        assertNull(data.getAuthData(type));
    }
}
