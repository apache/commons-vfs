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
package org.apache.commons.vfs2.provider.webdav;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.jackrabbit.webdav.DavException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Tests {@link ExceptionConverter}.
 */
public class ExceptionConverterTest {

    /** Marker class named by the crafted server response; must never be instantiated. */
    public static final class Marker {
        static volatile boolean instantiated;

        public Marker(final String message) {
            instantiated = true;
        }
    }

    /**
     * Builds a {@link DavException} whose error condition carries an {@code <exception><class>} element, as parsed from a
     * malicious WebDAV server error body.
     */
    private DavException davExceptionNaming(final String className) throws Exception {
        final String xml = "<exception><class>" + className + "</class><message>boom</message></exception>";
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        final Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        final Element condition = doc.getDocumentElement();
        return new DavException(500, "err", null, condition);
    }

    @Test
    public void testGenerateDoesNotInstantiateServerNamedClass() throws Exception {
        Marker.instantiated = false;
        final DavException cause = davExceptionNaming(Marker.class.getName());
        final FileSystemException result = ExceptionConverter.generate(cause);
        assertNotNull(result);
        assertFalse(Marker.instantiated, "server-controlled class name must not be instantiated");
    }

    @Test
    public void testGenerateStillWrapsExceptionType() throws Exception {
        final DavException cause = davExceptionNaming(IOException.class.getName());
        final FileSystemException result = ExceptionConverter.generate(cause);
        assertNotNull(result);
        assertInstanceOf(IOException.class, result.getCause());
    }
}
