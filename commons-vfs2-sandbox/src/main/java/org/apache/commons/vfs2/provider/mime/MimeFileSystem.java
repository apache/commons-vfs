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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.util.SharedRandomContentInputStream;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * An MIME file system.
 */
public class MimeFileSystem extends AbstractFileSystem {
    static final String NULL_BP_NAME = "_body_part_";
    static final String CONTENT_NAME = "_content";
    static final String PREAMBLE_CHARSET = StandardCharsets.UTF_8.name();

    private final Log log = LogFactory.getLog(MimeFileSystem.class);

    private InputStream mimeStream;

    /**
     * Constructs a new instance.
     *
     * @param rootName The root file name of this file system.
     * @param parentLayer The parent layer of this file system.
     * @param fileSystemOptions Options to build this file system.
     */
    protected MimeFileSystem(final FileName rootName, final FileObject parentLayer, final FileSystemOptions fileSystemOptions) {
        super(rootName, parentLayer, fileSystemOptions);
    }

    /**
     * Returns the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps) {
        caps.addAll(MimeFileProvider.capabilities);
    }

    private void closeMimeStream() throws IOException {
        if (mimeStream instanceof SharedRandomContentInputStream) {
            ((SharedRandomContentInputStream) mimeStream).closeAll();
        } else {
            mimeStream.close();
        }
    }

    public Part createCommunicationLink() throws IOException, MessagingException {
        if (mimeStream != null) {
            closeMimeStream();
        }

        final FileObject parentLayer = getParentLayer();
        if (!parentLayer.exists()) {
            return null;
        }

        if (parentLayer.getFileSystem().hasCapability(Capability.RANDOM_ACCESS_READ)) {
            mimeStream = new SharedRandomContentInputStream(parentLayer);
        } else {
            mimeStream = getParentLayer().getContent().getInputStream();
        }
        return new MimeMessage(null, mimeStream);
    }

    /**
     * Creates a file object.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws FileSystemException {
        return new MimeFileObject(name, null, this);
    }

    @Override
    protected void doCloseCommunicationLink() {
        try {
            if (mimeStream == null) {
                return;
            }

            closeMimeStream();
            mimeStream = null;
        } catch (final IOException e) {
            log.warn(e.getLocalizedMessage(), e);
        }
    }
}
