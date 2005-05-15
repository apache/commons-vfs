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
package org.apache.commons.vfs.provider.jar;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.zip.ZipFileObject;

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * A file in a Jar file system.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision$ $Date$
 */
public class JarFileObject extends ZipFileObject
{
    private Attributes attributes;

    final JarFileSystem fs;

    protected JarFileObject(final FileName name,
                            final ZipEntry entry,
                            final JarFileSystem fs,
                            final boolean zipExists) throws FileSystemException
    {
        super(name, entry, fs, zipExists);
        this.fs = fs;
    }

    /**
     * Returns the Jar manifest.
     */
    Manifest getManifest() throws IOException
    {
        if (fs.getZipFile() == null)
        {
            return null;
        }

        return ((JarFile) fs.getZipFile()).getManifest();
    }

    /**
     * Returns the attributes of this file.
     */
    Attributes getAttributes() throws IOException
    {
        if (attributes == null)
        {
            if (entry == null)
            {
                attributes = new Attributes(1);
            }
            else
            {
                attributes = ((JarEntry) entry).getAttributes();
                if (attributes == null)
                {
                    attributes = new Attributes(1);
                }
            }
        }

        return attributes;
    }

    /**
     * Returns the value of an attribute.
     */
    protected Map doGetAttributes()
        throws Exception
    {
        final Map attrs = new HashMap();

        // Add the file system's attributes first
        final JarFileSystem fs = (JarFileSystem) getFileSystem();
        addAll(fs.getAttributes(), attrs);

        // Add this file's attributes
        addAll(getAttributes(), attrs);

        return attrs;
    }

    /**
     * Adds the source attributes to the destination map.
     */
    private void addAll(final Attributes src, final Map dest)
    {
        for (Iterator iterator = src.entrySet().iterator(); iterator.hasNext();)
        {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final String name = entry.getKey().toString().toLowerCase();
            dest.put(name, entry.getValue());
        }
    }

    /**
     * Return the certificates of this JarEntry.
     */
    protected Certificate[] doGetCertificates()
    {
        if (entry == null)
        {
            return null;
        }

        return ((JarEntry) entry).getCertificates();
    }
}
