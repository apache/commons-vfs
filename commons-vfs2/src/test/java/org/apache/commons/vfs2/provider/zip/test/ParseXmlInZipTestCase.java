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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Tests that we can use JAXP to parse an input stream living inside a Zip file.
 */
public class ParseXmlInZipTestCase {

    private File createTempFile() throws IOException {
        final File zipFile = new File("src/test/resources/test-data/read-xml-tests.zip");
        final File newZipFile = File.createTempFile(getClass().getSimpleName(), ".zip");
        newZipFile.deleteOnExit();
        FileUtils.copyFile(zipFile, newZipFile);
        return newZipFile;
    }

    private DocumentBuilder newDocumentBuilder(final FileObject containerFile, final FileObject sourceFile,
            final String pathToXsdInZip) throws IOException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final boolean validate = pathToXsdInZip != null;
        documentBuilderFactory.setValidating(validate);
        documentBuilderFactory.setNamespaceAware(true);
        if (validate) {
            documentBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                    "http://www.w3.org/2001/XMLSchema");
            @SuppressWarnings("resource")
            final FileObject schema = containerFile.resolveFile(pathToXsdInZip);
            if (schema.exists()) {
                documentBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
                        schema.getContent().getInputStream());
            } else {
                schema.close();
                throw new FileNotFoundException(schema.toString());
            }
        }
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver(new TestEntityResolver(containerFile, sourceFile));
        } catch (final ParserConfigurationException e) {
            throw new IOException("Cannot read Java Connector configuration: " + e, e);
        }
        documentBuilder.setErrorHandler(new TestErrorHandler(containerFile + " - " + sourceFile));
        return documentBuilder;
    }

    @Test
    public void testParseXmlInZip() throws IOException, SAXException {
        final File newZipFile = createTempFile();
        final String xmlFilePath = "zip:file:" + newZipFile.getAbsolutePath() + "!/read-xml-tests/file1.xml";
        final FileSystemManager manager = VFS.getManager();
        try (final FileObject zipFileObject = manager.resolveFile(xmlFilePath)) {
            try (final InputStream inputStream = zipFileObject.getContent().getInputStream()) {
                final Document document = newDocumentBuilder(zipFileObject, zipFileObject, null).parse(inputStream);
                Assert.assertNotNull(document);
            }
        }
    }

    @Test
    public void testResolveAndParseBiggerXmlInZip() throws IOException, SAXException {
        // File is > 64 bytes
        // In this case, we want to make sure that the XML document does NOT fit in the internal buffer used to parse
        // the XML declaration and see if that affects JAXP when it uses its "rewind" input stream.
        testResolveAndParseXmlInZip("read-xml-tests/file3-bigger.xml", null);
    }

    @Test
    public void testResolveAndParseInvalidXml() throws IOException, SAXException {
        try {
            testResolveAndParseXmlInZip("read-xml-tests/name-invalid.xml", "/read-xml-tests/name.xsd");
        } catch (final SAXException e) {
            Assert.assertTrue(e.toString().contains("Invalid content was found starting with element 'FOO'."));
        }
    }

    @Test
    public void testResolveAndParseNotWellFormedXml() throws IOException {
        try {
            testResolveAndParseXmlInZip("read-xml-tests/name-not-well-formed.xml", "/read-xml-tests/name.xsd");
        } catch (final SAXException e) {
            Assert.assertTrue(
                    e.toString().contains("XML document structures must start and end within the same entity."));
        }
    }

    @Test
    public void testResolveAndParseXmlInZip() throws IOException, SAXException {
        // File is < 64 bytes
        // In this case, we want to make sure that the XML document DOES fit in the internal buffer used to parse
        // the XML declaration and see if that affects JAXP when it uses its "rewind" input stream.
        testResolveAndParseXmlInZip("read-xml-tests/file1.xml", null);
    }

    private void testResolveAndParseXmlInZip(final String xmlPathInZip, final String xsdPathInZip)
            throws IOException, FileSystemException, SAXException {
        final File newZipFile = createTempFile();
        final String zipFilePath = "zip:file:" + newZipFile.getAbsolutePath();
        final FileSystemManager manager = VFS.getManager();
        try (final FileObject zipFileObject = manager.resolveFile(zipFilePath)) {
            try (final FileObject xmlFileObject = zipFileObject.resolveFile(xmlPathInZip)) {
                try (final InputStream inputStream = xmlFileObject.getContent().getInputStream()) {
                    final Document document = newDocumentBuilder(zipFileObject, xmlFileObject, xsdPathInZip)
                            .parse(inputStream);
                    Assert.assertNotNull(document);
                }
            }
        }
    }

    @Test
    public void testResolveAndParseXmlInZipWithOneXmlSchema() throws IOException, SAXException {
        testResolveAndParseXmlInZip("read-xml-tests/name-with-xsd-ref.xml", "/read-xml-tests/name.xsd");
    }

    @Test
    public void testResolveAndParseXmlInZipWithTwoXmlSchema() throws IOException, SAXException {
        testResolveAndParseXmlInZip("read-xml-tests/person.xml", "/read-xml-tests/person.xsd");
    }

}
