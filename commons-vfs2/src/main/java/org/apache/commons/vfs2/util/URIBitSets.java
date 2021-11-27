/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.util;

import java.util.BitSet;

import org.apache.commons.vfs2.provider.GenericURLFileName;

/**
 * Internal URI encoding {@link BitSet} definitions.
 * <P>
 * This was forked from the {@link BitSet}s in {@code org.apache.commons.httpclient.URI},
 * in order to not be dependent on HttpClient v3 API, when generating and handling {@link GenericURLFileName}s,
 * but it should work with any different HTTP backend provider implementations.
 * </p>
 */
final class URIBitSets {

    private static final int NBITS = 256;

    // ---------------------- Generous characters for each component validation

    /**
     * The percent "%" character always has the reserved purpose of being the
     * escape indicator, it must be escaped as "%25" in order to be used as
     * data within a URI.
     */
    protected static final BitSet PERCENT = createBitSet();
    // Static initializer for percent
    static {
        PERCENT.set('%');
    }

    /**
     * BitSet for digit.
     * <p><blockquote><pre>
     * digit    = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" |
     *            "8" | "9"
     * </pre></blockquote></p>
     */
    protected static final BitSet DIGIT = createBitSet();
    // Static initializer for digit
    static {
        for (int i = '0'; i <= '9'; i++) {
            DIGIT.set(i);
        }
    }

    /**
     * BitSet for alpha.
     * <p><blockquote><pre>
     * alpha         = lowalpha | upalpha
     * </pre></blockquote></p>
     */
    protected static final BitSet ALPHA = createBitSet();
    // Static initializer for alpha
    static {
        for (int i = 'a'; i <= 'z'; i++) {
            ALPHA.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            ALPHA.set(i);
        }
    }

    /**
     * BitSet for alphanum (join of alpha &amp; digit).
     * <p><blockquote><pre>
     *  alphanum      = alpha | digit
     * </pre></blockquote></p>
     */
    protected static final BitSet ALPHANUM = createBitSet();
    // Static initializer for alphanum
    static {
        ALPHANUM.or(ALPHA);
        ALPHANUM.or(DIGIT);
    }

    /**
     * BitSet for hex.
     * <p><blockquote><pre>
     * hex           = digit | "A" | "B" | "C" | "D" | "E" | "F" |
     *                         "a" | "b" | "c" | "d" | "e" | "f"
     * </pre></blockquote></p>
     */
    protected static final BitSet HEX = createBitSet();
    // Static initializer for hex
    static {
        HEX.or(DIGIT);
        for (int i = 'a'; i <= 'f'; i++) {
            HEX.set(i);
        }
        for (int i = 'A'; i <= 'F'; i++) {
            HEX.set(i);
        }
    }

    /**
     * BitSet for escaped.
     * <p><blockquote><pre>
     * escaped       = "%" hex hex
     * </pre></blockquote></p>
     */
    protected static final BitSet ESCAPED = createBitSet();
    // Static initializer for escaped
    static {
        ESCAPED.or(PERCENT);
        ESCAPED.or(HEX);
    }

    /**
     * BitSet for mark.
     * <p><blockquote><pre>
     * mark          = "-" | "_" | "." | "!" | "~" | "*" | "'" |
     *                 "(" | ")"
     * </pre></blockquote></p>
     */
    protected static final BitSet MARK = createBitSet();
    // Static initializer for mark
    static {
        MARK.set('-');
        MARK.set('_');
        MARK.set('.');
        MARK.set('!');
        MARK.set('~');
        MARK.set('*');
        MARK.set('\'');
        MARK.set('(');
        MARK.set(')');
    }

    /**
     * Data characters that are allowed in a URI but do not have a reserved
     * purpose are called unreserved.
     * <p><blockquote><pre>
     * unreserved    = alphanum | mark
     * </pre></blockquote></p>
     */
    protected static final BitSet UNRESERVED = createBitSet();
    // Static initializer for unreserved
    static {
        UNRESERVED.or(ALPHANUM);
        UNRESERVED.or(MARK);
    }

    /**
     * BitSet for reserved.
     * <p><blockquote><pre>
     * reserved      = ";" | "/" | "?" | ":" | "@" | "&amp;" | "=" | "+" |
     *                 "$" | ","
     * </pre></blockquote></p>
     */
    protected static final BitSet RESERVED = createBitSet();
    // Static initializer for reserved
    static {
        RESERVED.set(';');
        RESERVED.set('/');
        RESERVED.set('?');
        RESERVED.set(':');
        RESERVED.set('@');
        RESERVED.set('&');
        RESERVED.set('=');
        RESERVED.set('+');
        RESERVED.set('$');
        RESERVED.set(',');
    }

    /**
     * BitSet for uric.
     * <p><blockquote><pre>
     * uric          = reserved | unreserved | escaped
     * </pre></blockquote></p>
     */
    protected static final BitSet URIC = createBitSet();
    // Static initializer for uric
    static {
        URIC.or(RESERVED);
        URIC.or(UNRESERVED);
        URIC.or(ESCAPED);
    }

    /**
     * BitSet for fragment (alias for uric).
     * <p><blockquote><pre>
     * fragment      = *uric
     * </pre></blockquote></p>
     */
    protected static final BitSet FRAGMENT = URIC;

    /**
     * BitSet for query (alias for uric).
     * <p><blockquote><pre>
     * query         = *uric
     * </pre></blockquote></p>
     */
    protected static final BitSet QUERY = URIC;

    /**
     * BitSet for pchar.
     * <p><blockquote><pre>
     * pchar         = unreserved | escaped |
     *                 ":" | "@" | "&amp;" | "=" | "+" | "$" | ","
     * </pre></blockquote></p>
     */
    protected static final BitSet PCHAR = createBitSet();
    // Static initializer for pchar
    static {
        PCHAR.or(UNRESERVED);
        PCHAR.or(ESCAPED);
        PCHAR.set(':');
        PCHAR.set('@');
        PCHAR.set('&');
        PCHAR.set('=');
        PCHAR.set('+');
        PCHAR.set('$');
        PCHAR.set(',');
    }

    /**
     * BitSet for param (alias for pchar).
     * <p><blockquote><pre>
     * param         = *pchar
     * </pre></blockquote></p>
     */
    protected static final BitSet PARAM = PCHAR;

    /**
     * BitSet for segment.
     * <p><blockquote><pre>
     * segment       = *pchar *( ";" param )
     * </pre></blockquote></p>
     */
    protected static final BitSet SEGMENT = createBitSet();
    // Static initializer for segment
    static {
        SEGMENT.or(PCHAR);
        SEGMENT.set(';');
        SEGMENT.or(PARAM);
    }

    /**
     * BitSet for path segments.
     * <p><blockquote><pre>
     * path_segments = segment *( "/" segment )
     * </pre></blockquote></p>
     */
    protected static final BitSet PATH_SEGMENTS = createBitSet();
    // Static initializer for path_segments
    static {
        PATH_SEGMENTS.set('/');
        PATH_SEGMENTS.or(SEGMENT);
    }

    /**
     * URI absolute path.
     * <p><blockquote><pre>
     * abs_path      = "/"  path_segments
     * </pre></blockquote></p>
     */
    protected static final BitSet ABS_PATH = createBitSet();
    // Static initializer for abs_path
    static {
        ABS_PATH.set('/');
        ABS_PATH.or(PATH_SEGMENTS);
    }

    /**
     * URI bitset for encoding typical non-slash characters.
     * <p><blockquote><pre>
     * uric_no_slash = unreserved | escaped | ";" | "?" | ":" | "@" |
     *                 "&amp;" | "=" | "+" | "$" | ","
     * </pre></blockquote></p>
     */
    protected static final BitSet URIC_NO_SLASH = createBitSet();
    // Static initializer for uric_no_slash
    static {
        URIC_NO_SLASH.or(UNRESERVED);
        URIC_NO_SLASH.or(ESCAPED);
        URIC_NO_SLASH.set(';');
        URIC_NO_SLASH.set('?');
        URIC_NO_SLASH.set(';');
        URIC_NO_SLASH.set('@');
        URIC_NO_SLASH.set('&');
        URIC_NO_SLASH.set('=');
        URIC_NO_SLASH.set('+');
        URIC_NO_SLASH.set('$');
        URIC_NO_SLASH.set(',');
    }

    /**
     * URI bitset that combines uric_no_slash and uric.
     * <p><blockquote><pre>
     * opaque_part   = uric_no_slash *uric
     * </pre></blockquote></p>
     */
    protected static final BitSet OPAQUE_PART = createBitSet();
    // Static initializer for opaque_part
    static {
        // it's generous. because first character must not include a slash
        OPAQUE_PART.or(URIC_NO_SLASH);
        OPAQUE_PART.or(URIC);
    }

    /**
     * URI bitset that combines absolute path and opaque part.
     * <p><blockquote><pre>
     * path          = [ abs_path | opaque_part ]
     * </pre></blockquote></p>
     */
    protected static final BitSet PATH = createBitSet();

    // Static initializer for path
    static {
        PATH.or(ABS_PATH);
        PATH.or(OPAQUE_PART);
    }

    /**
     * Port, a logical alias for digit.
     */
    protected static final BitSet PORT = DIGIT;

    /**
     * Bitset that combines digit and dot fo IPv$address.
     * <p><blockquote><pre>
     * IPv4address   = 1*digit "." 1*digit "." 1*digit "." 1*digit
     * </pre></blockquote></p>
     */
    protected static final BitSet IPV4ADDRESS = createBitSet();
    // Static initializer for IPv4address
    static {
        IPV4ADDRESS.or(DIGIT);
        IPV4ADDRESS.set('.');
    }

    /**
     * RFC 2373.
     * <p><blockquote><pre>
     * IPv6address = hexpart [ ":" IPv4address ]
     * </pre></blockquote></p>
     */
    protected static final BitSet IPV6ADDRESS = createBitSet();
    // Static initializer for IPv6address reference
    static {
        IPV6ADDRESS.or(HEX); // hexpart
        IPV6ADDRESS.set(':');
        IPV6ADDRESS.or(IPV4ADDRESS);
    }

    /**
     * RFC 2732, 2373.
     * <p><blockquote><pre>
     * IPv6reference   = "[" IPv6address "]"
     * </pre></blockquote></p>
     */
    protected static final BitSet IPV6REFERENCE = createBitSet();
    // Static initializer for IPv6reference
    static {
        IPV6REFERENCE.set('[');
        IPV6REFERENCE.or(IPV6ADDRESS);
        IPV6REFERENCE.set(']');
    }

    /**
     * BitSet for toplabel.
     * <p><blockquote><pre>
     * toplabel      = alpha | alpha *( alphanum | "-" ) alphanum
     * </pre></blockquote></p>
     */
    protected static final BitSet TOPLABEL = createBitSet();
    // Static initializer for toplabel
    static {
        TOPLABEL.or(ALPHANUM);
        TOPLABEL.set('-');
    }

    /**
     * BitSet for domainlabel.
     * <p><blockquote><pre>
     * domainlabel   = alphanum | alphanum *( alphanum | "-" ) alphanum
     * </pre></blockquote></p>
     */
    protected static final BitSet DOMAINLABEL = TOPLABEL;

    /**
     * BitSet for hostname.
     * <p><blockquote><pre>
     * hostname      = *( domainlabel "." ) toplabel [ "." ]
     * </pre></blockquote></p>
     */
    protected static final BitSet HOSTNAME = createBitSet();
    // Static initializer for hostname
    static {
        HOSTNAME.or(TOPLABEL);
        // hostname.or(domainlabel);
        HOSTNAME.set('.');
    }

    /**
     * BitSet for host.
     * <p><blockquote><pre>
     * host          = hostname | IPv4address | IPv6reference
     * </pre></blockquote></p>
     */
    protected static final BitSet HOST = createBitSet();
    // Static initializer for host
    static {
        HOST.or(HOSTNAME);
        // host.or(IPv4address);
        HOST.or(IPV6REFERENCE); // IPv4address
    }

    /**
     * BitSet for hostport.
     * <p><blockquote><pre>
     * hostport      = host [ ":" port ]
     * </pre></blockquote></p>
     */
    protected static final BitSet HOSTPORT = createBitSet();
    // Static initializer for hostport
    static {
        HOSTPORT.or(HOST);
        HOSTPORT.set(':');
        HOSTPORT.or(PORT);
    }

    /**
     * Bitset for userinfo.
     * <p><blockquote><pre>
     * userinfo      = *( unreserved | escaped |
     *                    ";" | ":" | "&amp;" | "=" | "+" | "$" | "," )
     * </pre></blockquote></p>
     */
    protected static final BitSet USERINFO = createBitSet();
    // Static initializer for userinfo
    static {
        USERINFO.or(UNRESERVED);
        USERINFO.or(ESCAPED);
        USERINFO.set(';');
        USERINFO.set(':');
        USERINFO.set('&');
        USERINFO.set('=');
        USERINFO.set('+');
        USERINFO.set('$');
        USERINFO.set(',');
    }

    /**
     * BitSet for within the userinfo component like user and password.
     */
    public static final BitSet WITHIN_USERRINFO = createBitSet();
    // Static initializer for within_userinfo
    static {
        WITHIN_USERRINFO.or(USERINFO);
        WITHIN_USERRINFO.clear(';'); // reserved within authority
        WITHIN_USERRINFO.clear(':');
        WITHIN_USERRINFO.clear('@');
        WITHIN_USERRINFO.clear('?');
        WITHIN_USERRINFO.clear('/');
    }

    /**
     * Bitset for server.
     * <p><blockquote><pre>
     * server        = [ [ userinfo "@" ] hostport ]
     * </pre></blockquote></p>
     */
    protected static final BitSet SERVER = createBitSet();
    // Static initializer for server
    static {
        SERVER.or(USERINFO);
        SERVER.set('@');
        SERVER.or(HOSTPORT);
    }

    /**
     * BitSet for reg_name.
     * <p><blockquote><pre>
     * reg_name      = 1*( unreserved | escaped | "$" | "," |
     *                     ";" | ":" | "@" | "&amp;" | "=" | "+" )
     * </pre></blockquote></p>
     */
    protected static final BitSet REG_NAME = createBitSet();
    // Static initializer for reg_name
    static {
        REG_NAME.or(UNRESERVED);
        REG_NAME.or(ESCAPED);
        REG_NAME.set('$');
        REG_NAME.set(',');
        REG_NAME.set(';');
        REG_NAME.set(':');
        REG_NAME.set('@');
        REG_NAME.set('&');
        REG_NAME.set('=');
        REG_NAME.set('+');
    }

    /**
     * BitSet for authority.
     * <p><blockquote><pre>
     * authority     = server | reg_name
     * </pre></blockquote></p>
     */
    protected static final BitSet AUTHORITY = createBitSet();
    // Static initializer for authority
    static {
        AUTHORITY.or(SERVER);
        AUTHORITY.or(REG_NAME);
    }

    /**
     * BitSet for scheme.
     * <p><blockquote><pre>
     * scheme        = alpha *( alpha | digit | "+" | "-" | "." )
     * </pre></blockquote></p>
     */
    protected static final BitSet SCHEME = createBitSet();
    // Static initializer for scheme
    static {
        SCHEME.or(ALPHA);
        SCHEME.or(DIGIT);
        SCHEME.set('+');
        SCHEME.set('-');
        SCHEME.set('.');
    }

    /**
     * BitSet for rel_segment.
     * <p><blockquote><pre>
     * rel_segment   = 1*( unreserved | escaped |
     *                     ";" | "@" | "&amp;" | "=" | "+" | "$" | "," )
     * </pre></blockquote></p>
     */
    protected static final BitSet REL_SEGMENT = createBitSet();
    // Static initializer for rel_segment
    static {
        REL_SEGMENT.or(UNRESERVED);
        REL_SEGMENT.or(ESCAPED);
        REL_SEGMENT.set(';');
        REL_SEGMENT.set('@');
        REL_SEGMENT.set('&');
        REL_SEGMENT.set('=');
        REL_SEGMENT.set('+');
        REL_SEGMENT.set('$');
        REL_SEGMENT.set(',');
    }

    /**
     * BitSet for rel_path.
     * <p><blockquote><pre>
     * rel_path      = rel_segment [ abs_path ]
     * </pre></blockquote></p>
     */
    protected static final BitSet REL_PATH = createBitSet();
    // Static initializer for rel_path
    static {
        REL_PATH.or(REL_SEGMENT);
        REL_PATH.or(ABS_PATH);
    }

    /**
     * BitSet for net_path.
     * <p><blockquote><pre>
     * net_path      = "//" authority [ abs_path ]
     * </pre></blockquote></p>
     */
    protected static final BitSet NET_PATH = createBitSet();
    // Static initializer for net_path
    static {
        NET_PATH.set('/');
        NET_PATH.or(AUTHORITY);
        NET_PATH.or(ABS_PATH);
    }

    /**
     * BitSet for hier_part.
     * <p><blockquote><pre>
     * hier_part     = ( net_path | abs_path ) [ "?" query ]
     * </pre></blockquote></p>
     */
    protected static final BitSet HIER_PART = createBitSet();
    // Static initializer for hier_part
    static {
        HIER_PART.or(NET_PATH);
        HIER_PART.or(ABS_PATH);
        // hier_part.set('?'); aleady included
        HIER_PART.or(QUERY);
    }

    /**
     * BitSet for relativeURI.
     * <p><blockquote><pre>
     * relativeURI   = ( net_path | abs_path | rel_path ) [ "?" query ]
     * </pre></blockquote></p>
     */
    protected static final BitSet RELATIVEURI = createBitSet();
    // Static initializer for relativeURI
    static {
        RELATIVEURI.or(NET_PATH);
        RELATIVEURI.or(ABS_PATH);
        RELATIVEURI.or(REL_PATH);
        // relativeURI.set('?'); aleady included
        RELATIVEURI.or(QUERY);
    }

    /**
     * BitSet for absoluteURI.
     * <p><blockquote><pre>
     * absoluteURI   = scheme ":" ( hier_part | opaque_part )
     * </pre></blockquote></p>
     */
    protected static final BitSet ABSOLUTEURI = createBitSet();
    // Static initializer for absoluteURI
    static {
        ABSOLUTEURI.or(SCHEME);
        ABSOLUTEURI.set(':');
        ABSOLUTEURI.or(HIER_PART);
        ABSOLUTEURI.or(OPAQUE_PART);
    }

    /**
     * BitSet for URI-reference.
     * <p><blockquote><pre>
     * URI-reference = [ absoluteURI | relativeURI ] [ "#" fragment ]
     * </pre></blockquote></p>
     */
    protected static final BitSet URI_REFERENCE = createBitSet();
    // Static initializer for URI_reference
    static {
        URI_REFERENCE.or(ABSOLUTEURI);
        URI_REFERENCE.or(RELATIVEURI);
        URI_REFERENCE.set('#');
        URI_REFERENCE.or(FRAGMENT);
    }

    // ---------------------------- Characters disallowed within the URI syntax
    // Excluded US-ASCII Characters are like control, space, delims and unwise

    /**
     * BitSet for control.
     */
    public static final BitSet CONTROL = createBitSet();
    // Static initializer for control
    static {
        for (int i = 0; i <= 0x1F; i++) {
            CONTROL.set(i);
        }
        CONTROL.set(0x7F);
    }

    /**
     * BitSet for space.
     */
    public static final BitSet SPACE = createBitSet();
    // Static initializer for space
    static {
        SPACE.set(0x20);
    }

    /**
     * BitSet for delims.
     */
    public static final BitSet DELIMS = createBitSet();
    // Static initializer for delims
    static {
        DELIMS.set('<');
        DELIMS.set('>');
        DELIMS.set('#');
        DELIMS.set('%');
        DELIMS.set('"');
    }

    /**
     * BitSet for unwise.
     */
    public static final BitSet UNWISE = createBitSet();
    // Static initializer for unwise
    static {
        UNWISE.set('{');
        UNWISE.set('}');
        UNWISE.set('|');
        UNWISE.set('\\');
        UNWISE.set('^');
        UNWISE.set('[');
        UNWISE.set(']');
        UNWISE.set('`');
    }

    /**
     * Disallowed rel_path before escaping.
     */
    public static final BitSet DISALLOWED_REL_PATH = createBitSet();
    // Static initializer for disallowed_rel_path
    static {
        DISALLOWED_REL_PATH.or(URIC);
        DISALLOWED_REL_PATH.andNot(REL_PATH);
    }

    /**
     * Disallowed opaque_part before escaping.
     */
    public static final BitSet DISALLOWED_OPAQUE_PART = createBitSet();
    // Static initializer for disallowed_opaque_part
    static {
        DISALLOWED_OPAQUE_PART.or(URIC);
        DISALLOWED_OPAQUE_PART.andNot(OPAQUE_PART);
    }

    // ----------------------- Characters allowed within and for each component

    /**
     * Those characters that are allowed for the authority component.
     */
    public static final BitSet ALLOWED_AUTHORITY = createBitSet();
    // Static initializer for allowed_authority
    static {
        ALLOWED_AUTHORITY.or(AUTHORITY);
        ALLOWED_AUTHORITY.clear('%');
    }

    /**
     * Those characters that are allowed for the opaque_part.
     */
    public static final BitSet ALLOWED_OPAQUE_PART = createBitSet();
    // Static initializer for allowed_opaque_part
    static {
        ALLOWED_OPAQUE_PART.or(OPAQUE_PART);
        ALLOWED_OPAQUE_PART.clear('%');
    }

    /**
     * Those characters that are allowed for the reg_name.
     */
    public static final BitSet ALLOWED_REG_NAME = createBitSet();
    // Static initializer for allowed_reg_name
    static {
        ALLOWED_REG_NAME.or(REG_NAME);
        // allowed_reg_name.andNot(percent);
        ALLOWED_REG_NAME.clear('%');
    }

    /**
     * Those characters that are allowed for the userinfo component.
     */
    public static final BitSet ALLOWED_USER_INFO = createBitSet();
    // Static initializer for allowed_userinfo
    static {
        ALLOWED_USER_INFO.or(USERINFO);
        // allowed_userinfo.andNot(percent);
        ALLOWED_USER_INFO.clear('%');
    }

    /**
     * Those characters that are allowed for within the userinfo component.
     */
    public static final BitSet ALLOWED_WITHIN_USERINFO = createBitSet();
    // Static initializer for allowed_within_userinfo
    static {
        ALLOWED_WITHIN_USERINFO.or(WITHIN_USERRINFO);
        ALLOWED_WITHIN_USERINFO.clear('%');
    }

    /**
     * Those characters that are allowed for the IPv6reference component.
     * The characters '[', ']' in IPv6reference should be excluded.
     */
    public static final BitSet ALLOWED_IPV6REFERENCE = createBitSet();
    // Static initializer for allowed_IPv6reference
    static {
        ALLOWED_IPV6REFERENCE.or(IPV6REFERENCE);
        // allowed_IPv6reference.andNot(unwise);
        ALLOWED_IPV6REFERENCE.clear('[');
        ALLOWED_IPV6REFERENCE.clear(']');
    }

    /**
     * Those characters that are allowed for the host component.
     * The characters '[', ']' in IPv6reference should be excluded.
     */
    public static final BitSet ALLOWED_HOST = createBitSet();
    // Static initializer for allowed_host
    static {
        ALLOWED_HOST.or(HOSTNAME);
        ALLOWED_HOST.or(ALLOWED_IPV6REFERENCE);
    }

    /**
     * Those characters that are allowed for the authority component.
     */
    public static final BitSet ALLOWED_WITHIN_AUTHORITY = createBitSet();
    // Static initializer for allowed_within_authority
    static {
        ALLOWED_WITHIN_AUTHORITY.or(SERVER);
        ALLOWED_WITHIN_AUTHORITY.or(REG_NAME);
        ALLOWED_WITHIN_AUTHORITY.clear(';');
        ALLOWED_WITHIN_AUTHORITY.clear(':');
        ALLOWED_WITHIN_AUTHORITY.clear('@');
        ALLOWED_WITHIN_AUTHORITY.clear('?');
        ALLOWED_WITHIN_AUTHORITY.clear('/');
    }

    /**
     * Those characters that are allowed for the abs_path.
     */
    public static final BitSet ALLOWED_ABS_PATH = createBitSet();
    // Static initializer for allowed_abs_path
    static {
        ALLOWED_ABS_PATH.or(ABS_PATH);
        // allowed_abs_path.set('/');  // aleady included
        ALLOWED_ABS_PATH.andNot(PERCENT);
        ALLOWED_ABS_PATH.clear('+');
    }

    /**
     * Those characters that are allowed for the rel_path.
     */
    public static final BitSet ALLOWED_REL_PATH = createBitSet();
    // Static initializer for allowed_rel_path
    static {
        ALLOWED_REL_PATH.or(REL_PATH);
        ALLOWED_REL_PATH.clear('%');
        ALLOWED_REL_PATH.clear('+');
    }

    /**
     * Those characters that are allowed within the path.
     */
    public static final BitSet ALLOWED_WITHIN_PATH = createBitSet();
    // Static initializer for allowed_within_path
    static {
        ALLOWED_WITHIN_PATH.or(ABS_PATH);
        ALLOWED_WITHIN_PATH.clear('/');
        ALLOWED_WITHIN_PATH.clear(';');
        ALLOWED_WITHIN_PATH.clear('=');
        ALLOWED_WITHIN_PATH.clear('?');
    }

    /**
     * Those characters that are allowed for the query component.
     */
    public static final BitSet ALLOWED_QUERY = createBitSet();
    // Static initializer for allowed_query
    static {
        ALLOWED_QUERY.or(URIC);
        ALLOWED_QUERY.clear('%');
    }

    /**
     * Those characters that are allowed within the query component.
     */
    public static final BitSet ALLOWED_WITHIN_QUERY = createBitSet();
    // Static initializer for allowed_within_query
    static {
        ALLOWED_WITHIN_QUERY.or(ALLOWED_QUERY);
        ALLOWED_WITHIN_QUERY.andNot(RESERVED); // excluded 'reserved'
    }

    /**
     * Those characters that are allowed for the fragment component.
     */
    public static final BitSet ALLOWED_FRAGMENT = createBitSet();
    // Static initializer for allowed_fragment
    static {
        ALLOWED_FRAGMENT.or(URIC);
        ALLOWED_FRAGMENT.clear('%');
    }

    private static BitSet createBitSet() {
        return new BitSet(NBITS);
    }

    private URIBitSets() {
    }

}
