/*
 * Copyright 2002-2005 The Apache Software Foundation.
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
package org.apache.commons.vfs.provider.mime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Collections;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * A map which tries to avoid building the real content as long as possible.
 * This makes quick lookups with known keys faster
 */
public class MimeLazyMap implements Map
{
	private Log log = LogFactory.getLog(MimeLazyMap.class);

	private final static String OBJECT_PREFIX = "obj.";

	private final Part part;
	private Map backingMap;

	private final static Map mimeMessageGetters = new TreeMap();

	static
	{
		Method[] methods = MimeMessage.class.getMethods();
		for (int i = 0; i<methods.length; i++)
		{
			Method method = methods[i];
			addMimeMessageMethod(method);
		}
		methods = MimeMessage.class.getDeclaredMethods();
		for (int i = 0; i<methods.length; i++)
		{
			Method method = methods[i];
			addMimeMessageMethod(method);
		}
	}

	private static void addMimeMessageMethod(Method method)
	{
		if (method.getName().startsWith("get"))
		{
			mimeMessageGetters.put(method.getName().substring(3), method);
		}
		else if (method.getName().startsWith("is"))
		{
			mimeMessageGetters.put(method.getName().substring(2), method);
		}
	}

	public MimeLazyMap(Part part)
	{
		this.part = part;
	}

	private Map getMap()
	{
		if (backingMap == null)
		{
			backingMap = createMap();
		}

		return backingMap;
	}

	private Map createMap()
	{
		Map ret = new TreeMap();

		Enumeration headers = null;
		try
		{
			headers = part.getAllHeaders();
		}
		catch (MessagingException e)
		{
			throw (RuntimeException) new RuntimeException(e);
		}
		while (headers.hasMoreElements())
		{
			Header header = (Header) headers.nextElement();
			String headerName = header.getName();

			Object values = ret.get(headerName);

			if (values == null)
			{
				ret.put(headerName, header.getValue());
			}
			else if (values instanceof String)
			{
				List newValues = new ArrayList();
				newValues.add(values);
				newValues.add(header.getValue());
				ret.put(headerName, newValues);
			}
			else if (values instanceof List)
			{
				((List) values).add(header.getValue());
			}
		}

		Iterator iterEntries = mimeMessageGetters.entrySet().iterator();
		while (iterEntries.hasNext())
		{
			Map.Entry entry = (Map.Entry) iterEntries.next();
			String name = (String) entry.getKey();
			Method method = (Method) entry.getValue();

			Object value;
			try
			{
				value = method.invoke(part, null);
			}
			catch (IllegalAccessException e)
			{
				log.warn(e.getLocalizedMessage(), e);
				continue;
			}
			catch (InvocationTargetException e)
			{
				log.warn(e.getLocalizedMessage(), e);
				continue;
			}

			ret.put(OBJECT_PREFIX+name, value);
		}

		return ret;
	}

	public int size()
	{
		return getMap().size();
	}

	public boolean isEmpty()
	{
		return getMap().size() < 1;
	}

	public boolean containsKey(Object key)
	{
		return getMap().containsKey(key);
	}

	public boolean containsValue(Object value)
	{
		return getMap().containsValue(value);
	}

	public Object get(Object key)
	{
		if (backingMap != null)
		{
			return backingMap.get(key);
		}

		return null;
	}

	public Object put(Object key, Object value)
	{
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key)
	{
		throw new UnsupportedOperationException();
	}

	public void putAll(Map t)
	{
		throw new UnsupportedOperationException();
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public Set keySet()
	{
		return Collections.unmodifiableSet(getMap().keySet());
	}

	public Collection values()
	{
		return Collections.unmodifiableCollection(getMap().values());
	}

	public Set entrySet()
	{
		return Collections.unmodifiableSet(getMap().entrySet());
	}
}
