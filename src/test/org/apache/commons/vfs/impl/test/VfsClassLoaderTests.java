/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
package org.apache.commons.vfs.impl.test;

import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.impl.VFSClassLoader;
import org.apache.commons.vfs.test.AbstractProviderTestCase;

/**
 * VfsClassLoader test cases.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/11/23 00:33:54 $
 */
public class VfsClassLoaderTests
    extends AbstractProviderTestCase
{
    /**
     * Returns the capabilities required by the tests of this test case.
     */
    protected Capability[] getRequiredCaps()
    {
        return new Capability[]
        {
            Capability.READ_CONTENT,
            Capability.URI
        };
    }

    /**
     * Tests VFSClassLoader.
     */
    public void testVFSClassLoader() throws Exception
    {
        final VFSClassLoader loader =
            new VFSClassLoader( getReadFolder(), getManager() );

        final Class testClass = loader.loadClass( "code.ClassToLoad" );
        assertTrue( verifyPackage( testClass.getPackage() ) );

        final Object testObject = testClass.newInstance();
        assertSame( "**PRIVATE**", testObject.toString() );

        final URL resource = loader.getResource( "file1.txt" );
        assertNotNull( resource );
        final URLConnection urlCon = resource.openConnection();
        assertSameURLContent( FILE1_CONTENT, urlCon );
    }

    /**
     * Verify the package loaded with class loader.
     * If the provider supports attributes override this method.
     */
    protected boolean verifyPackage( final Package pack )
    {
        return "code".equals( pack.getName() ) &&
            pack.getImplementationTitle() == null &&
            pack.getImplementationVendor() == null &&
            pack.getImplementationVersion() == null &&
            pack.getSpecificationTitle() == null &&
            pack.getSpecificationVendor() == null &&
            pack.getSpecificationVersion() == null &&
            !pack.isSealed();
    }

}
