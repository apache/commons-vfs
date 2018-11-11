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

package org.apache.commons.vfs2.provider.zip.test;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A simple SAX {@link ErrorHandler} for unit testing, just throws exceptions on all errors. Logs warnings to the
 * standard error console.
 */
public class TestErrorHandler implements ErrorHandler {

    private final String header;

    public TestErrorHandler(final String header) {
        super();
        this.header = header;
    }

    @Override
    public void error(final SAXParseException exception) throws SAXException {
        throw new SAXException(header, exception);
    }

    @Override
    public void fatalError(final SAXParseException exception) throws SAXException {
        throw new SAXException(header, exception);
    }

    @Override
    public void warning(final SAXParseException exception) throws SAXException {
        System.err.println(header);
        exception.printStackTrace(System.err);

    }

}
