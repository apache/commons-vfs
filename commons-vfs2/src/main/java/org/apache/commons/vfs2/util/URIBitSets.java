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
class URIBitSets {

    // ---------------------- Generous characters for each component validation

    /**
     * The percent "%" character always has the reserved purpose of being the
     * escape indicator, it must be escaped as "%25" in order to be used as
     * data within a URI.
     */
    protected static final BitSet percent = new BitSet(256);
    // Static initializer for percent
    static {
        percent.set('%');
    }


    /**
     * BitSet for digit.
     * <p><blockquote><pre>
     * digit    = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" |
     *            "8" | "9"
     * </pre></blockquote></p>
     */
    protected static final BitSet digit = new BitSet(256);
    // Static initializer for digit
    static {
        for (int i = '0'; i <= '9'; i++) {
            digit.set(i);
        }
    }


    /**
     * BitSet for alpha.
     * <p><blockquote><pre>
     * alpha         = lowalpha | upalpha
     * </pre></blockquote></p>
     */
    protected static final BitSet alpha = new BitSet(256);
    // Static initializer for alpha
    static {
        for (int i = 'a'; i <= 'z'; i++) {
            alpha.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            alpha.set(i);
        }
    }


    /**
     * BitSet for alphanum (join of alpha &amp; digit).
     * <p><blockquote><pre>
     *  alphanum      = alpha | digit
     * </pre></blockquote></p>
     */
    protected static final BitSet alphanum = new BitSet(256);
    // Static initializer for alphanum
    static {
        alphanum.or(alpha);
        alphanum.or(digit);
    }


    /**
     * BitSet for hex.
     * <p><blockquote><pre>
     * hex           = digit | "A" | "B" | "C" | "D" | "E" | "F" |
     *                         "a" | "b" | "c" | "d" | "e" | "f"
     * </pre></blockquote></p>
     */
    protected static final BitSet hex = new BitSet(256);
    // Static initializer for hex
    static {
        hex.or(digit);
        for (int i = 'a'; i <= 'f'; i++) {
            hex.set(i);
        }
        for (int i = 'A'; i <= 'F'; i++) {
            hex.set(i);
        }
    }


    /**
     * BitSet for escaped.
     * <p><blockquote><pre>
     * escaped       = "%" hex hex
     * </pre></blockquote></p>
     */
    protected static final BitSet escaped = new BitSet(256);
    // Static initializer for escaped
    static {
        escaped.or(percent);
        escaped.or(hex);
    }


    /**
     * BitSet for mark.
     * <p><blockquote><pre>
     * mark          = "-" | "_" | "." | "!" | "~" | "*" | "'" |
     *                 "(" | ")"
     * </pre></blockquote></p>
     */
    protected static final BitSet mark = new BitSet(256);
    // Static initializer for mark
    static {
        mark.set('-');
        mark.set('_');
        mark.set('.');
        mark.set('!');
        mark.set('~');
        mark.set('*');
        mark.set('\'');
        mark.set('(');
        mark.set(')');
    }


    /**
     * Data characters that are allowed in a URI but do not have a reserved
     * purpose are called unreserved.
     * <p><blockquote><pre>
     * unreserved    = alphanum | mark
     * </pre></blockquote></p>
     */
    protected static final BitSet unreserved = new BitSet(256);
    // Static initializer for unreserved
    static {
        unreserved.or(alphanum);
        unreserved.or(mark);
    }


    /**
     * BitSet for reserved.
     * <p><blockquote><pre>
     * reserved      = ";" | "/" | "?" | ":" | "@" | "&amp;" | "=" | "+" |
     *                 "$" | ","
     * </pre></blockquote></p>
     */
    protected static final BitSet reserved = new BitSet(256);
    // Static initializer for reserved
    static {
        reserved.set(';');
        reserved.set('/');
        reserved.set('?');
        reserved.set(':');
        reserved.set('@');
        reserved.set('&');
        reserved.set('=');
        reserved.set('+');
        reserved.set('$');
        reserved.set(',');
    }


    /**
     * BitSet for uric.
     * <p><blockquote><pre>
     * uric          = reserved | unreserved | escaped
     * </pre></blockquote></p>
     */
    protected static final BitSet uric = new BitSet(256);
    // Static initializer for uric
    static {
        uric.or(reserved);
        uric.or(unreserved);
        uric.or(escaped);
    }


    /**
     * BitSet for fragment (alias for uric).
     * <p><blockquote><pre>
     * fragment      = *uric
     * </pre></blockquote></p>
     */
    protected static final BitSet fragment = uric;


    /**
     * BitSet for query (alias for uric).
     * <p><blockquote><pre>
     * query         = *uric
     * </pre></blockquote></p>
     */
    protected static final BitSet query = uric;


    /**
     * BitSet for pchar.
     * <p><blockquote><pre>
     * pchar         = unreserved | escaped |
     *                 ":" | "@" | "&amp;" | "=" | "+" | "$" | ","
     * </pre></blockquote></p>
     */
    protected static final BitSet pchar = new BitSet(256);
    // Static initializer for pchar
    static {
        pchar.or(unreserved);
        pchar.or(escaped);
        pchar.set(':');
        pchar.set('@');
        pchar.set('&');
        pchar.set('=');
        pchar.set('+');
        pchar.set('$');
        pchar.set(',');
    }


    /**
     * BitSet for param (alias for pchar).
     * <p><blockquote><pre>
     * param         = *pchar
     * </pre></blockquote></p>
     */
    protected static final BitSet param = pchar;


    /**
     * BitSet for segment.
     * <p><blockquote><pre>
     * segment       = *pchar *( ";" param )
     * </pre></blockquote></p>
     */
    protected static final BitSet segment = new BitSet(256);
    // Static initializer for segment
    static {
        segment.or(pchar);
        segment.set(';');
        segment.or(param);
    }


    /**
     * BitSet for path segments.
     * <p><blockquote><pre>
     * path_segments = segment *( "/" segment )
     * </pre></blockquote></p>
     */
    protected static final BitSet path_segments = new BitSet(256);
    // Static initializer for path_segments
    static {
        path_segments.set('/');
        path_segments.or(segment);
    }


    /**
     * URI absolute path.
     * <p><blockquote><pre>
     * abs_path      = "/"  path_segments
     * </pre></blockquote></p>
     */
    protected static final BitSet abs_path = new BitSet(256);
    // Static initializer for abs_path
    static {
        abs_path.set('/');
        abs_path.or(path_segments);
    }


    /**
     * URI bitset for encoding typical non-slash characters.
     * <p><blockquote><pre>
     * uric_no_slash = unreserved | escaped | ";" | "?" | ":" | "@" |
     *                 "&amp;" | "=" | "+" | "$" | ","
     * </pre></blockquote></p>
     */
    protected static final BitSet uric_no_slash = new BitSet(256);
    // Static initializer for uric_no_slash
    static {
        uric_no_slash.or(unreserved);
        uric_no_slash.or(escaped);
        uric_no_slash.set(';');
        uric_no_slash.set('?');
        uric_no_slash.set(';');
        uric_no_slash.set('@');
        uric_no_slash.set('&');
        uric_no_slash.set('=');
        uric_no_slash.set('+');
        uric_no_slash.set('$');
        uric_no_slash.set(',');
    }


    /**
     * URI bitset that combines uric_no_slash and uric.
     * <p><blockquote><pre>
     * opaque_part   = uric_no_slash *uric
     * </pre></blockquote></p>
     */
    protected static final BitSet opaque_part = new BitSet(256);
    // Static initializer for opaque_part
    static {
        // it's generous. because first character must not include a slash
        opaque_part.or(uric_no_slash);
        opaque_part.or(uric);
    }


    /**
     * URI bitset that combines absolute path and opaque part.
     * <p><blockquote><pre>
     * path          = [ abs_path | opaque_part ]
     * </pre></blockquote></p>
     */
    protected static final BitSet path = new BitSet(256);
    // Static initializer for path
    static {
        path.or(abs_path);
        path.or(opaque_part);
    }


    /**
     * Port, a logical alias for digit.
     */
    protected static final BitSet port = digit;


    /**
     * Bitset that combines digit and dot fo IPv$address.
     * <p><blockquote><pre>
     * IPv4address   = 1*digit "." 1*digit "." 1*digit "." 1*digit
     * </pre></blockquote></p>
     */
    protected static final BitSet IPv4address = new BitSet(256);
    // Static initializer for IPv4address
    static {
        IPv4address.or(digit);
        IPv4address.set('.');
    }


    /**
     * RFC 2373.
     * <p><blockquote><pre>
     * IPv6address = hexpart [ ":" IPv4address ]
     * </pre></blockquote></p>
     */
    protected static final BitSet IPv6address = new BitSet(256);
    // Static initializer for IPv6address reference
    static {
        IPv6address.or(hex); // hexpart
        IPv6address.set(':');
        IPv6address.or(IPv4address);
    }


    /**
     * RFC 2732, 2373.
     * <p><blockquote><pre>
     * IPv6reference   = "[" IPv6address "]"
     * </pre></blockquote></p>
     */
    protected static final BitSet IPv6reference = new BitSet(256);
    // Static initializer for IPv6reference
    static {
        IPv6reference.set('[');
        IPv6reference.or(IPv6address);
        IPv6reference.set(']');
    }


    /**
     * BitSet for toplabel.
     * <p><blockquote><pre>
     * toplabel      = alpha | alpha *( alphanum | "-" ) alphanum
     * </pre></blockquote></p>
     */
    protected static final BitSet toplabel = new BitSet(256);
    // Static initializer for toplabel
    static {
        toplabel.or(alphanum);
        toplabel.set('-');
    }


    /**
     * BitSet for domainlabel.
     * <p><blockquote><pre>
     * domainlabel   = alphanum | alphanum *( alphanum | "-" ) alphanum
     * </pre></blockquote></p>
     */
    protected static final BitSet domainlabel = toplabel;


    /**
     * BitSet for hostname.
     * <p><blockquote><pre>
     * hostname      = *( domainlabel "." ) toplabel [ "." ]
     * </pre></blockquote></p>
     */
    protected static final BitSet hostname = new BitSet(256);
    // Static initializer for hostname
    static {
        hostname.or(toplabel);
        // hostname.or(domainlabel);
        hostname.set('.');
    }


    /**
     * BitSet for host.
     * <p><blockquote><pre>
     * host          = hostname | IPv4address | IPv6reference
     * </pre></blockquote></p>
     */
    protected static final BitSet host = new BitSet(256);
    // Static initializer for host
    static {
        host.or(hostname);
        // host.or(IPv4address);
        host.or(IPv6reference); // IPv4address
    }


    /**
     * BitSet for hostport.
     * <p><blockquote><pre>
     * hostport      = host [ ":" port ]
     * </pre></blockquote></p>
     */
    protected static final BitSet hostport = new BitSet(256);
    // Static initializer for hostport
    static {
        hostport.or(host);
        hostport.set(':');
        hostport.or(port);
    }


    /**
     * Bitset for userinfo.
     * <p><blockquote><pre>
     * userinfo      = *( unreserved | escaped |
     *                    ";" | ":" | "&amp;" | "=" | "+" | "$" | "," )
     * </pre></blockquote></p>
     */
    protected static final BitSet userinfo = new BitSet(256);
    // Static initializer for userinfo
    static {
        userinfo.or(unreserved);
        userinfo.or(escaped);
        userinfo.set(';');
        userinfo.set(':');
        userinfo.set('&');
        userinfo.set('=');
        userinfo.set('+');
        userinfo.set('$');
        userinfo.set(',');
    }


    /**
     * BitSet for within the userinfo component like user and password.
     */
    public static final BitSet within_userinfo = new BitSet(256);
    // Static initializer for within_userinfo
    static {
        within_userinfo.or(userinfo);
        within_userinfo.clear(';'); // reserved within authority
        within_userinfo.clear(':');
        within_userinfo.clear('@');
        within_userinfo.clear('?');
        within_userinfo.clear('/');
    }


    /**
     * Bitset for server.
     * <p><blockquote><pre>
     * server        = [ [ userinfo "@" ] hostport ]
     * </pre></blockquote></p>
     */
    protected static final BitSet server = new BitSet(256);
    // Static initializer for server
    static {
        server.or(userinfo);
        server.set('@');
        server.or(hostport);
    }


    /**
     * BitSet for reg_name.
     * <p><blockquote><pre>
     * reg_name      = 1*( unreserved | escaped | "$" | "," |
     *                     ";" | ":" | "@" | "&amp;" | "=" | "+" )
     * </pre></blockquote></p>
     */
    protected static final BitSet reg_name = new BitSet(256);
    // Static initializer for reg_name
    static {
        reg_name.or(unreserved);
        reg_name.or(escaped);
        reg_name.set('$');
        reg_name.set(',');
        reg_name.set(';');
        reg_name.set(':');
        reg_name.set('@');
        reg_name.set('&');
        reg_name.set('=');
        reg_name.set('+');
    }


    /**
     * BitSet for authority.
     * <p><blockquote><pre>
     * authority     = server | reg_name
     * </pre></blockquote></p>
     */
    protected static final BitSet authority = new BitSet(256);
    // Static initializer for authority
    static {
        authority.or(server);
        authority.or(reg_name);
    }


    /**
     * BitSet for scheme.
     * <p><blockquote><pre>
     * scheme        = alpha *( alpha | digit | "+" | "-" | "." )
     * </pre></blockquote></p>
     */
    protected static final BitSet scheme = new BitSet(256);
    // Static initializer for scheme
    static {
        scheme.or(alpha);
        scheme.or(digit);
        scheme.set('+');
        scheme.set('-');
        scheme.set('.');
    }


    /**
     * BitSet for rel_segment.
     * <p><blockquote><pre>
     * rel_segment   = 1*( unreserved | escaped |
     *                     ";" | "@" | "&amp;" | "=" | "+" | "$" | "," )
     * </pre></blockquote></p>
     */
    protected static final BitSet rel_segment = new BitSet(256);
    // Static initializer for rel_segment
    static {
        rel_segment.or(unreserved);
        rel_segment.or(escaped);
        rel_segment.set(';');
        rel_segment.set('@');
        rel_segment.set('&');
        rel_segment.set('=');
        rel_segment.set('+');
        rel_segment.set('$');
        rel_segment.set(',');
    }


    /**
     * BitSet for rel_path.
     * <p><blockquote><pre>
     * rel_path      = rel_segment [ abs_path ]
     * </pre></blockquote></p>
     */
    protected static final BitSet rel_path = new BitSet(256);
    // Static initializer for rel_path
    static {
        rel_path.or(rel_segment);
        rel_path.or(abs_path);
    }


    /**
     * BitSet for net_path.
     * <p><blockquote><pre>
     * net_path      = "//" authority [ abs_path ]
     * </pre></blockquote></p>
     */
    protected static final BitSet net_path = new BitSet(256);
    // Static initializer for net_path
    static {
        net_path.set('/');
        net_path.or(authority);
        net_path.or(abs_path);
    }


    /**
     * BitSet for hier_part.
     * <p><blockquote><pre>
     * hier_part     = ( net_path | abs_path ) [ "?" query ]
     * </pre></blockquote></p>
     */
    protected static final BitSet hier_part = new BitSet(256);
    // Static initializer for hier_part
    static {
        hier_part.or(net_path);
        hier_part.or(abs_path);
        // hier_part.set('?'); aleady included
        hier_part.or(query);
    }


    /**
     * BitSet for relativeURI.
     * <p><blockquote><pre>
     * relativeURI   = ( net_path | abs_path | rel_path ) [ "?" query ]
     * </pre></blockquote></p>
     */
    protected static final BitSet relativeURI = new BitSet(256);
    // Static initializer for relativeURI
    static {
        relativeURI.or(net_path);
        relativeURI.or(abs_path);
        relativeURI.or(rel_path);
        // relativeURI.set('?'); aleady included
        relativeURI.or(query);
    }


    /**
     * BitSet for absoluteURI.
     * <p><blockquote><pre>
     * absoluteURI   = scheme ":" ( hier_part | opaque_part )
     * </pre></blockquote></p>
     */
    protected static final BitSet absoluteURI = new BitSet(256);
    // Static initializer for absoluteURI
    static {
        absoluteURI.or(scheme);
        absoluteURI.set(':');
        absoluteURI.or(hier_part);
        absoluteURI.or(opaque_part);
    }


    /**
     * BitSet for URI-reference.
     * <p><blockquote><pre>
     * URI-reference = [ absoluteURI | relativeURI ] [ "#" fragment ]
     * </pre></blockquote></p>
     */
    protected static final BitSet URI_reference = new BitSet(256);
    // Static initializer for URI_reference
    static {
        URI_reference.or(absoluteURI);
        URI_reference.or(relativeURI);
        URI_reference.set('#');
        URI_reference.or(fragment);
    }

    // ---------------------------- Characters disallowed within the URI syntax
    // Excluded US-ASCII Characters are like control, space, delims and unwise

    /**
     * BitSet for control.
     */
    public static final BitSet control = new BitSet(256);
    // Static initializer for control
    static {
        for (int i = 0; i <= 0x1F; i++) {
            control.set(i);
        }
        control.set(0x7F);
    }

    /**
     * BitSet for space.
     */
    public static final BitSet space = new BitSet(256);
    // Static initializer for space
    static {
        space.set(0x20);
    }


    /**
     * BitSet for delims.
     */
    public static final BitSet delims = new BitSet(256);
    // Static initializer for delims
    static {
        delims.set('<');
        delims.set('>');
        delims.set('#');
        delims.set('%');
        delims.set('"');
    }


    /**
     * BitSet for unwise.
     */
    public static final BitSet unwise = new BitSet(256);
    // Static initializer for unwise
    static {
        unwise.set('{');
        unwise.set('}');
        unwise.set('|');
        unwise.set('\\');
        unwise.set('^');
        unwise.set('[');
        unwise.set(']');
        unwise.set('`');
    }


    /**
     * Disallowed rel_path before escaping.
     */
    public static final BitSet disallowed_rel_path = new BitSet(256);
    // Static initializer for disallowed_rel_path
    static {
        disallowed_rel_path.or(uric);
        disallowed_rel_path.andNot(rel_path);
    }


    /**
     * Disallowed opaque_part before escaping.
     */
    public static final BitSet disallowed_opaque_part = new BitSet(256);
    // Static initializer for disallowed_opaque_part
    static {
        disallowed_opaque_part.or(uric);
        disallowed_opaque_part.andNot(opaque_part);
    }

    // ----------------------- Characters allowed within and for each component

    /**
     * Those characters that are allowed for the authority component.
     */
    public static final BitSet allowed_authority = new BitSet(256);
    // Static initializer for allowed_authority
    static {
        allowed_authority.or(authority);
        allowed_authority.clear('%');
    }


    /**
     * Those characters that are allowed for the opaque_part.
     */
    public static final BitSet allowed_opaque_part = new BitSet(256);
    // Static initializer for allowed_opaque_part
    static {
        allowed_opaque_part.or(opaque_part);
        allowed_opaque_part.clear('%');
    }


    /**
     * Those characters that are allowed for the reg_name.
     */
    public static final BitSet allowed_reg_name = new BitSet(256);
    // Static initializer for allowed_reg_name
    static {
        allowed_reg_name.or(reg_name);
        // allowed_reg_name.andNot(percent);
        allowed_reg_name.clear('%');
    }


    /**
     * Those characters that are allowed for the userinfo component.
     */
    public static final BitSet allowed_userinfo = new BitSet(256);
    // Static initializer for allowed_userinfo
    static {
        allowed_userinfo.or(userinfo);
        // allowed_userinfo.andNot(percent);
        allowed_userinfo.clear('%');
    }


    /**
     * Those characters that are allowed for within the userinfo component.
     */
    public static final BitSet allowed_within_userinfo = new BitSet(256);
    // Static initializer for allowed_within_userinfo
    static {
        allowed_within_userinfo.or(within_userinfo);
        allowed_within_userinfo.clear('%');
    }


    /**
     * Those characters that are allowed for the IPv6reference component.
     * The characters '[', ']' in IPv6reference should be excluded.
     */
    public static final BitSet allowed_IPv6reference = new BitSet(256);
    // Static initializer for allowed_IPv6reference
    static {
        allowed_IPv6reference.or(IPv6reference);
        // allowed_IPv6reference.andNot(unwise);
        allowed_IPv6reference.clear('[');
        allowed_IPv6reference.clear(']');
    }


    /**
     * Those characters that are allowed for the host component.
     * The characters '[', ']' in IPv6reference should be excluded.
     */
    public static final BitSet allowed_host = new BitSet(256);
    // Static initializer for allowed_host
    static {
        allowed_host.or(hostname);
        allowed_host.or(allowed_IPv6reference);
    }


    /**
     * Those characters that are allowed for the authority component.
     */
    public static final BitSet allowed_within_authority = new BitSet(256);
    // Static initializer for allowed_within_authority
    static {
        allowed_within_authority.or(server);
        allowed_within_authority.or(reg_name);
        allowed_within_authority.clear(';');
        allowed_within_authority.clear(':');
        allowed_within_authority.clear('@');
        allowed_within_authority.clear('?');
        allowed_within_authority.clear('/');
    }


    /**
     * Those characters that are allowed for the abs_path.
     */
    public static final BitSet allowed_abs_path = new BitSet(256);
    // Static initializer for allowed_abs_path
    static {
        allowed_abs_path.or(abs_path);
        // allowed_abs_path.set('/');  // aleady included
        allowed_abs_path.andNot(percent);
        allowed_abs_path.clear('+');
    }


    /**
     * Those characters that are allowed for the rel_path.
     */
    public static final BitSet allowed_rel_path = new BitSet(256);
    // Static initializer for allowed_rel_path
    static {
        allowed_rel_path.or(rel_path);
        allowed_rel_path.clear('%');
        allowed_rel_path.clear('+');
    }


    /**
     * Those characters that are allowed within the path.
     */
    public static final BitSet allowed_within_path = new BitSet(256);
    // Static initializer for allowed_within_path
    static {
        allowed_within_path.or(abs_path);
        allowed_within_path.clear('/');
        allowed_within_path.clear(';');
        allowed_within_path.clear('=');
        allowed_within_path.clear('?');
    }


    /**
     * Those characters that are allowed for the query component.
     */
    public static final BitSet allowed_query = new BitSet(256);
    // Static initializer for allowed_query
    static {
        allowed_query.or(uric);
        allowed_query.clear('%');
    }


    /**
     * Those characters that are allowed within the query component.
     */
    public static final BitSet allowed_within_query = new BitSet(256);
    // Static initializer for allowed_within_query
    static {
        allowed_within_query.or(allowed_query);
        allowed_within_query.andNot(reserved); // excluded 'reserved'
    }


    /**
     * Those characters that are allowed for the fragment component.
     */
    public static final BitSet allowed_fragment = new BitSet(256);
    // Static initializer for allowed_fragment
    static {
        allowed_fragment.or(uric);
        allowed_fragment.clear('%');
    }

    private URIBitSets() {
    }

}
