/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
package org.apache.commons.vfs.provider.smb.test;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.smb.SmbFileName;
import org.apache.commons.vfs.test.AbstractProviderTestCase;

/**
 * Some additional SMB file name test cases.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2003/02/12 07:44:56 $
 */
public class FileNameTests
    extends AbstractProviderTestCase
{
    /**
     * Tests parsing a URI into its parts.
     */
    public void testParseUri() throws Exception
    {
        // Simple name
        SmbFileName name = SmbFileName.parseUri( "smb://hostname/share/file" );
        assertEquals( "smb", name.getScheme() );
        assertNull( name.getUserInfo() );
        assertEquals( "hostname", name.getHostName() );
        assertEquals( 139, name.getPort() );
        assertEquals( name.getDefaultPort(), name.getPort() );
        assertEquals( "share", name.getShare() );
        assertEquals( "/file", name.getPath() );
        assertEquals( "smb://hostname/share/", name.getRootURI() );
        assertEquals( "smb://hostname/share/file", name.getURI() );

        // Name with port
        name = SmbFileName.parseUri( "smb://hostname:9090/share/file" );
        assertEquals( "smb", name.getScheme() );
        assertNull( name.getUserInfo() );
        assertEquals( "hostname", name.getHostName() );
        assertEquals( 9090, name.getPort() );
        assertEquals( "share", name.getShare() );
        assertEquals( "/file", name.getPath() );
        assertEquals( "smb://hostname:9090/share/", name.getRootURI() );
        assertEquals( "smb://hostname:9090/share/file", name.getURI() );

        // Name with no path
        name = SmbFileName.parseUri( "smb://hostname/share" );
        assertEquals( "smb", name.getScheme() );
        assertNull( name.getUserInfo() );
        assertEquals( "hostname", name.getHostName() );
        assertEquals( 139, name.getPort() );
        assertEquals( "share", name.getShare() );
        assertEquals( "/", name.getPath() );
        assertEquals( "smb://hostname/share/", name.getRootURI() );
        assertEquals( "smb://hostname/share/", name.getURI() );

        // Name with username
        name = SmbFileName.parseUri( "smb://user@hostname/share/file" );
        assertEquals( "smb", name.getScheme() );
        assertEquals( "user", name.getUserInfo() );
        assertEquals( "hostname", name.getHostName() );
        assertEquals( 139, name.getPort() );
        assertEquals( "share", name.getShare() );
        assertEquals( "/file", name.getPath() );
        assertEquals( "smb://user@hostname/share/", name.getRootURI() );
        assertEquals( "smb://user@hostname/share/file", name.getURI() );
    }

    /**
     * Tests error handling in URI parser.
     */
    public void testBadlyFormedUri() throws Exception
    {
        // Does not start with smb://
        testBadlyFormedUri( "smb:", "vfs.provider/missing-double-slashes.error" );
        testBadlyFormedUri( "smb:/", "vfs.provider/missing-double-slashes.error" );
        testBadlyFormedUri( "smb:a", "vfs.provider/missing-double-slashes.error" );

        // Missing hostname
        testBadlyFormedUri( "smb://", "vfs.provider/missing-hostname.error" );
        testBadlyFormedUri( "smb://:21/share", "vfs.provider/missing-hostname.error" );
        testBadlyFormedUri( "smb:///share", "vfs.provider/missing-hostname.error" );

        // Empty port
        testBadlyFormedUri( "smb://host:", "vfs.provider/missing-port.error" );
        testBadlyFormedUri( "smb://host:/share", "vfs.provider/missing-port.error" );
        testBadlyFormedUri( "smb://host:port/share/file", "vfs.provider/missing-port.error" );

        // Missing absolute path
        testBadlyFormedUri( "smb://host:90a", "vfs.provider/missing-hostname-path-sep.error" );
        testBadlyFormedUri( "smb://host?a", "vfs.provider/missing-hostname-path-sep.error" );

        // Missing share name
        testBadlyFormedUri( "smb://host", "vfs.provider.smb/missing-share-name.error" );
        testBadlyFormedUri( "smb://host/", "vfs.provider.smb/missing-share-name.error" );
        testBadlyFormedUri( "smb://host:9090/", "vfs.provider.smb/missing-share-name.error" );
    }

    /** Tests that parsing a URI fails with the expected error. */
    private void testBadlyFormedUri( final String uri, final String errorMsg )
    {
        try
        {
            SmbFileName.parseUri( uri );
            fail();
        }
        catch ( final FileSystemException e )
        {
            assertSameMessage( errorMsg, uri, e );
        }
    }
}
