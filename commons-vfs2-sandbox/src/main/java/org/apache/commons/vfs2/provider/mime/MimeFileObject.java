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
package org.apache.commons.vfs2.provider.mime;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.vfs2.FileContentInfoFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.FileObjectUtils;

/**
 * A part of a MIME message.
 */
public class MimeFileObject extends AbstractFileObject<MimeFileSystem> implements FileObject {
    private Part part;
    private Map<String, Object> attributeMap;

    protected MimeFileObject(final AbstractFileName name, final Part part, final MimeFileSystem fileSystem)
            throws FileSystemException {
        super(name, fileSystem);
        setPart(part);
    }

    /**
     * Attaches this file object to its file resource.
     */
    @Override
    protected void doAttach() throws Exception {
        if (part == null) {
            if (!getName().equals(getFileSystem().getRootName())) {
                final MimeFileObject foParent = (MimeFileObject) FileObjectUtils.getAbstractFileObject(getParent());
                setPart(foParent.findPart(getName().getBaseName()));
                return;
            }

            setPart(((MimeFileSystem) getFileSystem()).createCommunicationLink());
        }
    }

    private Part findPart(final String partName) throws Exception {
        if (getType() == FileType.IMAGINARY) {
            // not existent
            return null;
        }

        if (isMultipart()) {
            final Multipart multipart = (Multipart) part.getContent();
            if (partName.startsWith(MimeFileSystem.NULL_BP_NAME)) {
                final int partNumber = Integer.parseInt(partName.substring(MimeFileSystem.NULL_BP_NAME.length()), 10);
                if (partNumber < 0 || partNumber + 1 > multipart.getCount()) {
                    // non existent
                    return null;
                }

                return multipart.getBodyPart(partNumber);
            }

            for (int i = 0; i < multipart.getCount(); i++) {
                final Part childPart = multipart.getBodyPart(i);
                if (partName.equals(childPart.getFileName())) {
                    return childPart;
                }
            }
        }

        return null;
    }

    @Override
    protected void doDetach() throws Exception {
    }

    /**
     * Determines the type of the file, returns null if the file does not exist.
     */
    @Override
    protected FileType doGetType() throws Exception {
        if (part == null) {
            return FileType.IMAGINARY;
        }

        if (isMultipart()) {
            // we cant have children ...
            return FileType.FILE_OR_FOLDER;
        }

        return FileType.FILE;
    }

    @Override
    protected String[] doListChildren() throws Exception {
        return null;
    }

    /**
     * Lists the children of the file. Is only called if {@link #doGetType} returns
     * {@link org.apache.commons.vfs2.FileType#FOLDER}.
     */
    @Override
    protected FileObject[] doListChildrenResolved() throws Exception {
        if (part == null) {
            return null;
        }

        final List<MimeFileObject> vfs = new ArrayList<>();
        if (isMultipart()) {
            final Object container = part.getContent();
            if (container instanceof Multipart) {
                final Multipart multipart = (Multipart) container;

                for (int i = 0; i < multipart.getCount(); i++) {
                    final Part part = multipart.getBodyPart(i);

                    String filename = UriParser.encode(part.getFileName());
                    if (filename == null) {
                        filename = MimeFileSystem.NULL_BP_NAME + i;
                    }

                    final MimeFileObject fo = (MimeFileObject) FileObjectUtils
                            .getAbstractFileObject(getFileSystem().resolveFile(getFileSystem().getFileSystemManager()
                                    .resolveName(getName(), filename, NameScope.CHILD)));
                    fo.setPart(part);
                    vfs.add(fo);
                }
            }
        }

        return vfs.toArray(new MimeFileObject[vfs.size()]);
    }

    private void setPart(final Part part) {
        this.part = part;
        this.attributeMap = null;
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    @Override
    protected long doGetContentSize() throws Exception {
        return part.getSize();
    }

    /**
     * Returns the last modified time of this file.
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        final Message mm = getMessage();
        if (mm == null) {
            return -1;
        }
        if (mm.getSentDate() != null) {
            return mm.getSentDate().getTime();
        }
        if (mm.getReceivedDate() != null) {
            mm.getReceivedDate();
        }
        return 0;
    }

    private Message getMessage() throws FileSystemException {
        if (part instanceof Message) {
            return (Message) part;
        }

        return ((MimeFileObject) FileObjectUtils.getAbstractFileObject(getParent())).getMessage();
    }

    /**
     * Creates an input stream to read the file content from.
     */
    @Override
    protected InputStream doGetInputStream() throws Exception {
        if (isMultipart()) {
            // deliver the preamble as the only content

            final String preamble = ((MimeMultipart) part.getContent()).getPreamble();
            if (preamble == null) {
                return new ByteArrayInputStream(new byte[] {});
            }
            return new ByteArrayInputStream(preamble.getBytes(MimeFileSystem.PREAMBLE_CHARSET));
        }

        return part.getInputStream();
    }

    boolean isMultipart() throws MessagingException {
        return part.getContentType() != null && part.getContentType().startsWith("multipart/");
    }

    @Override
    protected FileContentInfoFactory getFileContentInfoFactory() {
        return new MimeFileContentInfoFactory();
    }

    protected Part getPart() {
        return part;
    }

    /**
     * Returns all headers of this part.
     * <p>
     * The map key is a java.lang.String and the value is a:
     * <ul>
     * <li>{@code java.lang.Strings} for single entries or a</li>
     * <li>{@code java.utils.List<java.lang.Strings>} for entries with multiple values</li>
     * </ul>
     */
    @Override
    protected Map<String, Object> doGetAttributes() throws Exception {
        if (attributeMap == null) {
            if (part != null) {
                attributeMap = new MimeAttributesMap(part);
            } else {
                attributeMap = Collections.emptyMap();
            }
        }

        return attributeMap;
    }

    @SuppressWarnings("unchecked") // Javadoc says Part returns Header
    protected Enumeration<Header> getAllHeaders() throws MessagingException {
        return part.getAllHeaders();
    }
}
