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
package org.apache.commons.vfs;

import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

/**
 * Container for various authentication data
 */
public class UserAuthenticationData
{
	public static class Type implements Comparable
	{
		private final String type;

		public Type(String type)
		{
			this.type = type;
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

			Type type1 = (Type) o;

			if (type != null ? !type.equals(type1.type) : type1.type != null)
			{
				return false;
			}

			return true;
		}

		public int compareTo(Object o)
		{
			Type t = (Type) o;

			return type.compareTo(t.type);
		}
	}

	public static final Type USERNAME = new Type("username");
	public static final Type PASSWORD = new Type("password");
	public static final Type DOMAIN = new Type("domain");

	private Map authenticationData = new TreeMap();

	public UserAuthenticationData()
	{
	}

	/**
	 * set a data to this collection
	 */
	public void setData(Type type, char[] data)
	{
		authenticationData.put(type, data);
	}

	/**
	 * get a data from the collection
	 */
	public char[] getData(Type type)
	{
		return (char[]) authenticationData.get(type);
	}

	/**
	 * deleted all data stored within this authenticator
	 */
	public void cleanup()
	{
		// step 1: nullify character buffers
		Iterator iterAuthenticationData = authenticationData.values().iterator();
		while (iterAuthenticationData.hasNext())
		{
			char[] data = (char[]) iterAuthenticationData.next();
			if (data == null || data.length < 0)
			{
				continue;
			}

			for (int i = 0; i<data.length; i++)
			{
				data[i]=0;
			}
		}
		// step 2: allow data itself to gc
		authenticationData.clear();
	}
}
