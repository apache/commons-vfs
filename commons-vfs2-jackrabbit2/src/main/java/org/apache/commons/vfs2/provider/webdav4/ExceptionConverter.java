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
package org.apache.commons.vfs2.provider.webdav4;

import java.lang.reflect.Constructor;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Element;

/**
 * Converts WebDAV exceptions into FileSystemExceptions.
 *
 * @since 2.5.0
 */
public final class ExceptionConverter {

    // avoid instanciation
    private ExceptionConverter() {
    }

    public static FileSystemException generate(final DavException davExc) throws FileSystemException {
        String msg = davExc.getMessage();
        if (davExc.hasErrorCondition()) {
            try {
                final Element error = davExc.toXml(DomUtil.createDocument());
                if (DomUtil.matches(error, DavException.XML_ERROR, DavConstants.NAMESPACE) && DomUtil.hasChildElement(error, "exception", null)) {
                    final Element exc = DomUtil.getChildElement(error, "exception", null);
                    if (DomUtil.hasChildElement(exc, "message", null)) {
                        msg = DomUtil.getChildText(exc, "message", null);
                    }
                    if (DomUtil.hasChildElement(exc, "class", null)) {
                        final Class<?> cl = Class.forName(DomUtil.getChildText(exc, "class", null));
                        final Constructor<?> excConstr = cl.getConstructor(String.class);
                        final Object o = excConstr.newInstance(msg);
                        if (o instanceof FileSystemException) {
                            return (FileSystemException) o;
                        }
                        if (o instanceof Exception) {
                            return new FileSystemException(msg, (Exception) o);
                        }
                    }
                }
            } catch (final Exception e) {
                throw new FileSystemException(e);
            }
        }

        return new FileSystemException(msg);
    }
}
