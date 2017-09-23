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

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.vfs2.FileObject;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This SAX resolver opens VFS objects (FileObject, FileContent, InputStream) but does not close them.
 */
public class TestEntityResolver implements EntityResolver {

    private final FileObject containerFile;
    private final FileObject sourceFile;

    public TestEntityResolver(final FileObject containerFile, FileObject sourceFile) {
        this.containerFile = containerFile;
        this.sourceFile = sourceFile;
    }

    @Override
    public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
        // System.out.println("resolving publicId=" + publicId + ", systemId=" + systemId);
        final String fileName = new File(URI.create(systemId).getPath()).getName();
        if (/* fileName.equals("person.xsd") || */fileName.equals("name.xsd") || fileName.equals("address.xsd")) {
            final String path = "/read-xml-tests/" + fileName;
            final FileObject xsdFileObject = sourceFile.resolveFile(path);
            if (!xsdFileObject.exists()) {
                System.err.println("File does not exist: " + xsdFileObject);
                throw new IllegalStateException(
                        "Schema " + path + " not found in file " + containerFile + " parsing " + sourceFile);
            }
            // System.out.println("Opening input stream on " + xsdFileObject);
            return new InputSource(xsdFileObject.getContent().getInputStream());
        }
        return null;
    }

}
