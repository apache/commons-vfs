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
package org.apache.commons.vfs.test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import junit.framework.TestSuite;
import org.apache.commons.vfs.impl.test.VfsClassLoaderTests;

/**
 * The suite of tests for a file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/11/23 00:32:12 $
 */
public class ProviderTestSuite
    extends TestSuite
{
    private final ProviderTestConfig providerConfig;
    private final String prefix;

    /**
     * Adds the tests for a file system to this suite.
     */
    public ProviderTestSuite( final ProviderTestConfig providerConfig ) throws Exception
    {
        this( providerConfig, "", false );
    }

    private ProviderTestSuite( final ProviderTestConfig providerConfig,
                               final String prefix,
                               final boolean nested )
        throws Exception
    {
        this.providerConfig = providerConfig;
        this.prefix = prefix;
        addBaseTests();
        if ( !nested )
        {
            // Add nested tests
            // TODO - enable this again
            //addTest( new ProviderTestSuite( new JunctionProviderConfig( providerConfig ), "junction.", true ));
        }
    }

    /**
     * Adds base tests - excludes the nested test cases.
     */
    private void addBaseTests() throws Exception
    {
        addTestClass( ProviderReadTests.class );
        addTestClass( ProviderWriteTests.class );
        addTestClass( UrlTests.class );
        addTestClass( VfsClassLoaderTests.class );
    }

    /**
     * Adds the tests from a class to this suite.  Looks for a no-args constructor
     * which it uses to create instances of the test class.  Adds an instance
     * for each public test method provided by the class.
     */
    private void addTestClass( final Class testClass ) throws Exception
    {
        // Locate the test methods
        final Method[] methods = testClass.getMethods();
        for ( int i = 0; i < methods.length; i++ )
        {
            final Method method = methods[ i ];
            if ( ! method.getName().startsWith( "test")
                || Modifier.isStatic( method.getModifiers() )
                || method.getReturnType() != Void.TYPE
                || method.getParameterTypes().length != 0 )
            {
                continue;
            }

            // Create instance
            final AbstractProviderTestCase testCase = (AbstractProviderTestCase)testClass.newInstance();
            testCase.setConfig( method, providerConfig );
            testCase.setName( prefix + method.getName() );
            addTest( testCase );
        }
    }
}
