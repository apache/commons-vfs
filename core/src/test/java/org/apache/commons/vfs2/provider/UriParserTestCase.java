package org.apache.commons.vfs2.provider;

import junit.framework.Assert;

import org.junit.Test;

public class UriParserTestCase
{

    @Test
    public void testColonInFileName()
    {
        Assert.assertEquals(null, UriParser.extractScheme("some/path/some:file"));
    }

    @Test
    public void testNormalScheme()
    {
        Assert.assertEquals("ftp", UriParser.extractScheme("ftp://user:pass@host/some/path/some:file"));
    }

}
