package org.apache.commons.vfs2.provider.smb3;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.GenericFileName;

public class SMB3FileProvider extends AbstractOriginatingFileProvider
{
	
	/**
     * Authenticator types.
     */
    public static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES = new UserAuthenticationData.Type[] {
            UserAuthenticationData.USERNAME, UserAuthenticationData.PASSWORD };
    
    static final Collection<Capability> capabilities = Collections.unmodifiableCollection(Arrays
            .asList(new Capability[] { Capability.CREATE, Capability.DELETE, Capability.RENAME, Capability.GET_TYPE,
                    Capability.LIST_CHILDREN, Capability.READ_CONTENT, Capability.GET_LAST_MODIFIED, Capability.URI,
                    Capability.WRITE_CONTENT, Capability.APPEND_CONTENT, Capability.RANDOM_ACCESS_READ, }));
    
    public SMB3FileProvider()
    {
    	super();
    	setFileNameParser(SMB3FileNameParser.getInstance());
    }
    
    

	@Override
	public Collection<Capability> getCapabilities()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected FileSystem doCreateFileSystem(FileName name, FileSystemOptions fileSystemOptions)
			throws FileSystemException
	{
		final GenericFileName rootName = (GenericFileName) name;
		
		final SMB3ClientWrapper smbClient = new SMB3ClientWrapper(rootName, fileSystemOptions);
		
		return new SMB3FileSystem(rootName, fileSystemOptions, smbClient);
	}
	
    @Override
    public FileName parseUri(final FileName base, final String uri) throws FileSystemException {
        if (getFileNameParser() != null) {
            
        	if(uri.endsWith("//")) //TODO really parse if share is not in uri
        	{
        		return ((SMB3FileNameParser) getFileNameParser()).parseShareRoot(getContext(), base, uri);
        	}
        	
        	return getFileNameParser().parseUri(getContext(), base, uri);
        }

        throw new FileSystemException("vfs.provider/filename-parser-missing.error");
    }

}
