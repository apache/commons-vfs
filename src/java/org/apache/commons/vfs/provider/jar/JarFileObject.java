/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002, 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.vfs.provider.jar;

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
import java.util.zip.ZipFile;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.provider.zip.ZipFileObject;

/**
 * A file in a Jar file system.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision: 1.9 $ $Date: 2003/10/13 08:44:27 $
 */
class JarFileObject extends ZipFileObject
{
    private Attributes attributes;

    public JarFileObject( final FileName name,
                          final ZipEntry entry,
                          final ZipFile zipFile,
                          final JarFileSystem fs )
    {
        super( name, entry, zipFile, fs );
    }

    /**
     * Returns the Jar manifest.
     */
    Manifest getManifest() throws IOException
    {
        if ( file == null )
        {
            return null;
        }

        return ( (JarFile)file ).getManifest();
    }

    /**
     * Returns the attributes of this file.
     */
    Attributes getAttributes() throws IOException
    {
        if ( attributes == null )
        {
            if ( entry == null )
            {
                attributes = new Attributes( 1 );
            }
            else
            {
                attributes = ( (JarEntry)entry ).getAttributes();
                if ( attributes == null )
                {
                    attributes = new Attributes( 1 );
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
        final JarFileSystem fs = (JarFileSystem)getFileSystem();
        addAll( fs.getAttributes(), attrs );

        // Add this file's attributes
        addAll( getAttributes(), attrs );

        return attrs;
    }

    /** Adds the source attributes to the destination map. */
    private void addAll( final Attributes src, final Map dest )
    {
        for ( Iterator iterator = src.entrySet().iterator(); iterator.hasNext(); )
        {
            final Map.Entry entry = (Map.Entry)iterator.next();
            final String name = entry.getKey().toString().toLowerCase();
            dest.put( name, entry.getValue() );
        }
    }

    /**
     * Return the certificates of this JarEntry.
     */
    protected Certificate[] doGetCertificates()
    {
        if ( entry == null )
        {
            return null;
        }

        return ( (JarEntry)entry ).getCertificates();
    }
}
