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
package org.apache.commons.vfs.provider.test;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.GenericFileName;

/**
 * Some GenericFileName test cases.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2003/10/13 08:42:45 $
 */
public class GenericFileNameTestCase
    extends AbstractVfsTestCase
{
    /**
     * Tests parsing a URI into its parts.
     */
    public void testParseUri() throws Exception
    {
        // Simple name
        GenericFileName name = GenericFileName.parseUri( "ftp://hostname/file", 21 );
        assertEquals( "ftp", name.getScheme() );
        assertNull( name.getUserName() );
        assertNull( name.getPassword() );
        assertEquals( "hostname", name.getHostName() );
        assertEquals( 21, name.getPort() );
        assertEquals( name.getDefaultPort(), name.getPort() );
        assertEquals( "/file", name.getPath() );
        assertEquals( "ftp://hostname/", name.getRootURI() );
        assertEquals( "ftp://hostname/file", name.getURI() );

        // Name with port
        name = GenericFileName.parseUri( "ftp://hostname:9090/file", 21 );
        assertEquals( "ftp", name.getScheme() );
        assertNull( name.getUserName() );
        assertNull( name.getPassword() );
        assertEquals( "hostname", name.getHostName() );
        assertEquals( 9090, name.getPort() );
        assertEquals( "/file", name.getPath() );
        assertEquals( "ftp://hostname:9090/", name.getRootURI() );
        assertEquals( "ftp://hostname:9090/file", name.getURI() );

        // Name with no path
        name = GenericFileName.parseUri( "ftp://hostname", 21 );
        assertEquals( "ftp", name.getScheme() );
        assertNull( name.getUserName() );
        assertNull( name.getPassword() );
        assertEquals( "hostname", name.getHostName() );
        assertEquals( 21, name.getPort() );
        assertEquals( "/", name.getPath() );
        assertEquals( "ftp://hostname/", name.getRootURI() );
        assertEquals( "ftp://hostname/", name.getURI() );

        // Name with username
        name = GenericFileName.parseUri( "ftp://user@hostname/file", 21 );
        assertEquals( "ftp", name.getScheme() );
        assertEquals( "user", name.getUserName() );
        assertNull( name.getPassword() );
        assertEquals( "hostname", name.getHostName() );
        assertEquals( 21, name.getPort() );
        assertEquals( "/file", name.getPath() );
        assertEquals( "ftp://user@hostname/", name.getRootURI() );
        assertEquals( "ftp://user@hostname/file", name.getURI() );

        // Name with username and password
        name = GenericFileName.parseUri( "ftp://user:password@hostname/file", 21 );
        assertEquals( "ftp", name.getScheme() );
        assertEquals( "user", name.getUserName() );
        assertEquals( "password", name.getPassword() );
        assertEquals( "hostname", name.getHostName() );
        assertEquals( 21, name.getPort() );
        assertEquals( "/file", name.getPath() );
        assertEquals( "ftp://user:password@hostname/", name.getRootURI() );
        assertEquals( "ftp://user:password@hostname/file", name.getURI() );

        // Encoded username and password
        name = GenericFileName.parseUri( "ftp://%75ser%3A:%40@hostname", 21 );
        assertEquals( "ftp", name.getScheme() );
        assertEquals( "user:", name.getUserName() );
        assertEquals( "@", name.getPassword() );
        assertEquals( "hostname", name.getHostName() );
        assertEquals( 21, name.getPort() );
        assertEquals( "/", name.getPath() );
        assertEquals( "ftp://user%3a:%40@hostname/", name.getRootURI() );
        assertEquals( "ftp://user%3a:%40@hostname/", name.getURI() );
    }

    /**
     * Tests error handling in URI parser.
     */
    public void testBadlyFormedUri() throws Exception
    {
        // Does not start with ftp://
        testBadlyFormedUri( "ftp:", "vfs.provider/missing-double-slashes.error" );
        testBadlyFormedUri( "ftp:/", "vfs.provider/missing-double-slashes.error" );
        testBadlyFormedUri( "ftp:a", "vfs.provider/missing-double-slashes.error" );

        // Missing hostname
        testBadlyFormedUri( "ftp://", "vfs.provider/missing-hostname.error" );
        testBadlyFormedUri( "ftp://:21/file", "vfs.provider/missing-hostname.error" );
        testBadlyFormedUri( "ftp:///file", "vfs.provider/missing-hostname.error" );

        // Empty port
        testBadlyFormedUri( "ftp://host:", "vfs.provider/missing-port.error" );
        testBadlyFormedUri( "ftp://host:/file", "vfs.provider/missing-port.error" );
        testBadlyFormedUri( "ftp://host:port/file", "vfs.provider/missing-port.error" );

        // Missing absolute path
        testBadlyFormedUri( "ftp://host:90a", "vfs.provider/missing-hostname-path-sep.error" );
        testBadlyFormedUri( "ftp://host?a", "vfs.provider/missing-hostname-path-sep.error" );
    }

    /** Tests that parsing a URI fails with the expected error. */
    private void testBadlyFormedUri( final String uri, final String errorMsg )
    {
        try
        {
            GenericFileName.parseUri( uri, 21 );
            fail();
        }
        catch ( final FileSystemException e )
        {
            assertSameMessage( errorMsg, uri, e );
        }
    }
}
