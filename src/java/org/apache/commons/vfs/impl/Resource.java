package org.apache.commons.vfs.impl;

import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileSystemException;

/**
 * Helper class for VFSClassLoader. This represents a resource loaded with
 * the classloader.
 *
 * @see VFSClassLoader
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision: 1.1 $ $Date: 2002/08/21 13:10:57 $
 */
class Resource
{
    private FileObject root;
    private FileObject resource;

    /**
     * Creates a new instance.
     *
     * @param root The code source FileObject.
     * @param resource The resource of the FileObject.
     */
    Resource( FileObject root, FileObject resource )
    {
        this.root = root;
        this.resource = resource;
    }
    
    /**
     * Returns the URL of the resource.
     */
    URL getURL() throws MalformedURLException
    {
        return resource.getURL();
    }

    /**
     * Returns the FileObject of the resource.
     */
    FileObject getFileObject()
    {
        return resource;
    }

    /**
     * Returns the code source as an URL.
     */
    URL getCodeSourceURL() throws MalformedURLException
    {
        return root.getURL();
    }

    /**
     * Returns the data for this resource as a byte array.
     */
    byte[] getBytes() throws IOException
    {
        try
        {
            FileContent content = resource.getContent();
            final int size = (int) content.getSize();
            byte[] buf = new byte[ size ];
        
            InputStream in = content.getInputStream();
            int read = in.read( buf );

            while ( read < size )
            {
                read += in.read( buf, read, size - read );
            }

            return buf;
        }
        catch ( FileSystemException fse )
        {
            throw new ProtocolException( fse.getMessage() );
        }
    }
}
