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
package org.apache.commons.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility methods for dealng with FileObjects.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2003/10/13 08:45:23 $
 */
public class FileUtil
{
    private FileUtil()
    {
    }

    /**
     * Returns the content of a file, as a byte array.
     *
     * @param file The file to get the content of.
     */
    public static byte[] getContent( final FileObject file )
        throws IOException
    {
        final FileContent content = file.getContent();
        final int size = (int)content.getSize();
        final byte[] buf = new byte[ size ];

        final InputStream in = content.getInputStream();
        try
        {
            int read = 0;
            for ( int pos = 0; pos < size && read >= 0; pos += read )
            {
                read = in.read( buf, pos, size - pos );
            }
        }
        finally
        {
            in.close();
        }

        return buf;
    }

    /**
     * Writes the content of a file to an OutputStream.
     */
    public static void writeContent( final FileObject file,
                                     final OutputStream outstr )
        throws IOException
    {
        final InputStream instr = file.getContent().getInputStream();
        try
        {
            final byte[] buffer = new byte[ 1024 ];
            while ( true )
            {
                final int nread = instr.read( buffer );
                if ( nread < 0 )
                {
                    break;
                }
                outstr.write( buffer, 0, nread );
            }
        }
        finally
        {
            instr.close();
        }
    }

    /**
     * Copies the content from a source file to a destination file.
     */
    public static void copyContent( final FileObject srcFile,
                                    final FileObject destFile )
        throws IOException
    {
        // Create the output stream via getContent(), to pick up the
        // validation it does
        final OutputStream outstr = destFile.getContent().getOutputStream();
        try
        {
            writeContent( srcFile, outstr );
        }
        finally
        {
            outstr.close();
        }
    }

}
