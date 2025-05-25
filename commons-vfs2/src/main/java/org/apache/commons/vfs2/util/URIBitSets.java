/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.util;

import java.util.BitSet;

import org.apache.commons.lang3.util.FluentBitSet;
import org.apache.commons.vfs2.provider.GenericURLFileName;

/**
 * Internal URI encoding {@link BitSet} definitions.
 * <p>
 * This was forked from the {@link BitSet}s in {@code org.apache.commons.httpclient.URI}, in order to not be dependent
 * on HttpClient v3 API, when generating and handling {@link GenericURLFileName}s, but it should work with any different
 * HTTP backend provider implementations.
 * </p>
 */
final class URIBitSets {

    /**
     * The percent "%" character always has the reserved purpose of being the escape indicator, it must be escaped as "%25"
     * in order to be used as data within a URI.
     */
    static final FluentBitSet PERCENT = bitSet('%');

    /**
     * BitSet for digit.
     * <p>
     * <blockquote>
     *
     * <pre>
     * digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet DIGIT = bitSet().setInclusive('0', '9');

    /**
     * BitSet for alpha.
     * <p>
     * <blockquote>
     *
     * <pre>
     * alpha = lowalpha | upalpha
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet ALPHA = bitSet().setInclusive('a', 'z').setInclusive('A', 'Z');

    /**
     * BitSet for alphanum (join of alpha &amp; digit).
     * <p>
     * <blockquote>
     *
     * <pre>
     * alphanum = alpha | digit
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet ALPHANUM = bitSet().or(ALPHA, DIGIT);

    /**
     * BitSet for hex.
     * <p>
     * <blockquote>
     *
     * <pre>
     * hex = digit | "A" | "B" | "C" | "D" | "E" | "F" | "a" | "b" | "c" | "d" | "e" | "f"
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet HEX = bitSet().or(DIGIT).setInclusive('a', 'f').setInclusive('A', 'F');

    /**
     * BitSet for escaped.
     * <p>
     * <blockquote>
     *
     * <pre>
     * escaped       = "%" hex hex
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet ESCAPED = bitSet().or(PERCENT, HEX);

    /**
     * BitSet for mark.
     * <p>
     * <blockquote>
     *
     * <pre>
     * mark = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet MARK = bitSet('-', '_', '.', '!', '~', '*', '\'', '(', ')');

    /**
     * Data characters that are allowed in a URI but do not have a reserved purpose are called unreserved.
     * <p>
     * <blockquote>
     *
     * <pre>
     * unreserved = alphanum | mark
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet UNRESERVED = bitSet().or(ALPHANUM, MARK);

    /**
     * BitSet for reserved.
     * <p>
     * <blockquote>
     *
     * <pre>
     * reserved = ";" | "/" | "?" | ":" | "@" | "&amp;" | "=" | "+" | "$" | ","
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet RESERVED = bitSet(';', '/', '?', ':', '@', '&', '=', '+', '$', ',');

    /**
     * BitSet for uric.
     * <p>
     * <blockquote>
     *
     * <pre>
     * uric = reserved | unreserved | escaped
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet URIC = bitSet().or(RESERVED, UNRESERVED, ESCAPED);

    /**
     * BitSet for fragment (alias for uric).
     * <p>
     * <blockquote>
     *
     * <pre>
     * fragment      = *uric
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet FRAGMENT = URIC;

    /**
     * BitSet for query (alias for uric).
     * <p>
     * <blockquote>
     *
     * <pre>
     * query         = *uric
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet QUERY = URIC;

    /**
     * BitSet for pchar.
     * <p>
     * <blockquote>
     *
     * <pre>
     * pchar = unreserved | escaped | ":" | "@" | "&amp;" | "=" | "+" | "$" | ","
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet PCHAR = bitSet(':', '@', '&', '=', '+', '$', ',').or(UNRESERVED, ESCAPED);

    /**
     * BitSet for param (alias for pchar).
     * <p>
     * <blockquote>
     *
     * <pre>
     * param         = *pchar
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet PARAM = PCHAR;

    /**
     * BitSet for segment.
     * <p>
     * <blockquote>
     *
     * <pre>
     * segment       = *pchar *( ";" param )
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet SEGMENT = bitSet(';').or(PCHAR, PARAM);

    /**
     * BitSet for path segments.
     * <p>
     * <blockquote>
     *
     * <pre>
     * path_segments = segment *( "/" segment )
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet PATH_SEGMENTS = bitSet('/').or(SEGMENT);

    /**
     * URI absolute path.
     * <p>
     * <blockquote>
     *
     * <pre>
     * abs_path      = "/"  path_segments
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet ABS_PATH = bitSet('/').or(PATH_SEGMENTS);

    /**
     * URI bitset for encoding typical non-slash characters.
     * <p>
     * <blockquote>
     *
     * <pre>
     * uric_no_slash = unreserved | escaped | ";" | "?" | ":" | "@" | "&amp;" | "=" | "+" | "$" | ","
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet URIC_NO_SLASH = bitSet(';', '?', ';', '@', '&', '=', '+', '$', ',').or(UNRESERVED, ESCAPED);

    /**
     * URI bitset that combines uric_no_slash and uric.
     * <p>
     * <blockquote>
     *
     * <pre>
     * opaque_part = uric_no_slash * uric
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet OPAQUE_PART = bitSet().or(URIC_NO_SLASH, URIC);

    /**
     * URI bitset that combines absolute path and opaque part.
     * <p>
     * <blockquote>
     *
     * <pre>
     * path          = [ abs_path | opaque_part ]
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet PATH = bitSet().or(ABS_PATH, OPAQUE_PART);

    /**
     * Port, a logical alias for digit.
     */
    static final FluentBitSet PORT = DIGIT;

    /**
     * Bitset that combines digit and dot fo IPv$address.
     * <p>
     * <blockquote>
     *
     * <pre>
     * IPv4address   = 1*digit "." 1*digit "." 1*digit "." 1*digit
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet IPV4ADDRESS = bitSet('.').or(DIGIT);

    /**
     * RFC 2373.
     * <p>
     * <blockquote>
     *
     * <pre>
     * IPv6address = hexpart [ ":" IPv4address ]
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet IPV6ADDRESS = bitSet(':').or(HEX, IPV4ADDRESS);

    /**
     * RFC 2732, 2373.
     * <p>
     * <blockquote>
     *
     * <pre>
     * IPv6reference   = "[" IPv6address "]"
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet IPV6REFERENCE = bitSet('[', ']').or(IPV6ADDRESS);

    /**
     * BitSet for toplabel.
     * <p>
     * <blockquote>
     *
     * <pre>
     * toplabel      = alpha | alpha *( alphanum | "-" ) alphanum
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet TOPLABEL = bitSet('-').or(ALPHANUM);

    /**
     * BitSet for domainlabel.
     * <p>
     * <blockquote>
     *
     * <pre>
     * domainlabel   = alphanum | alphanum *( alphanum | "-" ) alphanum
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet DOMAINLABEL = TOPLABEL;

    /**
     * BitSet for hostname.
     * <p>
     * <blockquote>
     *
     * <pre>
     * hostname      = *( domainlabel "." ) toplabel [ "." ]
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet HOSTNAME = bitSet('.').or(TOPLABEL);

    /**
     * BitSet for host.
     * <p>
     * <blockquote>
     *
     * <pre>
     * host = hostname | IPv4address | IPv6reference
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet HOST = bitSet().or(HOSTNAME, IPV6REFERENCE);

    // Static initializer for host
//    static {
//        HOST.or(HOSTNAME);
//        // host.or(IPv4address);
//        HOST.or(IPV6REFERENCE); // IPv4address
//    }
    /**
     * BitSet for hostport.
     * <p>
     * <blockquote>
     *
     * <pre>
     * hostport      = host [ ":" port ]
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet HOSTPORT = bitSet(':').or(HOST, PORT);

    /**
     * Bitset for userinfo.
     * <p>
     * <blockquote>
     *
     * <pre>
     * userinfo      = *( unreserved | escaped |
     *                    ";" | ":" | "&amp;" | "=" | "+" | "$" | "," )
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet USERINFO = bitSet(';', ':', '&', '=', '+', '$', ',').or(UNRESERVED, ESCAPED);

    /**
     * BitSet for within the userinfo component like user and password.
     */
    static final FluentBitSet WITHIN_USERRINFO = bitSet(';', ':', '@', '?', '/').or(USERINFO);

    /**
     * Bitset for server.
     * <p>
     * <blockquote>
     *
     * <pre>
     * server        = [ [ userinfo "@" ] hostport ]
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet SERVER = bitSet('@').or(USERINFO, HOSTPORT);

    /**
     * BitSet for reg_name.
     * <p>
     * <blockquote>
     *
     * <pre>
     * reg_name = 1 * (unreserved | escaped | "$" | "," | ";" | ":" | "@" | "&amp;" | "=" | "+")
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet REG_NAME = bitSet('$', ',', ';', ':', '@', '&', '=', '+').or(UNRESERVED, ESCAPED);

    /**
     * BitSet for authority.
     * <p>
     * <blockquote>
     *
     * <pre>
     * authority = server | reg_name
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet AUTHORITY = bitSet().or(SERVER, REG_NAME);

    /**
     * BitSet for scheme.
     * <p>
     * <blockquote>
     *
     * <pre>
     * scheme = alpha * (alpha | digit | "+" | "-" | ".")
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet SCHEME = bitSet('+', '-', '.').or(ALPHA, DIGIT);

    /**
     * BitSet for rel_segment.
     * <p>
     * <blockquote>
     *
     * <pre>
     * rel_segment = 1 * (unreserved | escaped | ";" | "@" | "&amp;" | "=" | "+" | "$" | ",")
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet REL_SEGMENT = bitSet(';', '@', '&', '=', '+', '$', ',').or(UNRESERVED, ESCAPED);

    /**
     * BitSet for rel_path.
     * <p>
     * <blockquote>
     *
     * <pre>
     * rel_path = rel_segment[abs_path]
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet REL_PATH = bitSet().or(REL_SEGMENT, ABS_PATH);

    /**
     * BitSet for net_path.
     * <p>
     * <blockquote>
     *
     * <pre>
     * net_path      = "//" authority [ abs_path ]
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet NET_PATH = bitSet('/').or(AUTHORITY, ABS_PATH);

    /**
     * BitSet for hier_part.
     * <p>
     * <blockquote>
     *
     * <pre>
     * hier_part     = ( net_path | abs_path ) [ "?" query ]
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    // hier_part.set('?'); already included
    static final FluentBitSet HIER_PART = bitSet().or(NET_PATH, ABS_PATH, QUERY);

    /**
     * BitSet for relativeURI.
     * <p>
     * <blockquote>
     *
     * <pre>
     * relativeURI   = ( net_path | abs_path | rel_path ) [ "?" query ]
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    // relativeURI.set('?'); already included
    static final FluentBitSet RELATIVEURI = bitSet().or(NET_PATH, ABS_PATH, REL_PATH, QUERY);

    /**
     * BitSet for absoluteURI.
     * <p>
     * <blockquote>
     *
     * <pre>
     * absoluteURI   = scheme ":" ( hier_part | opaque_part )
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet ABSOLUTEURI = bitSet(':').or(SCHEME, HIER_PART, OPAQUE_PART);

    /**
     * BitSet for URI-reference.
     * <p>
     * <blockquote>
     *
     * <pre>
     * URI-reference = [ absoluteURI | relativeURI ] [ "#" fragment ]
     * </pre>
     *
     * </blockquote>
     * </p>
     */
    static final FluentBitSet URI_REFERENCE = bitSet('#').or(ABSOLUTEURI, RELATIVEURI, FRAGMENT);

    // Characters disallowed within the URI syntax
    // Excluded US-ASCII Characters are like control, space, delims and unwise

    /**
     * BitSet for space.
     */
    static final FluentBitSet SPACE = bitSet(0x20);

    /**
     * BitSet for delims.
     */
    static final FluentBitSet DELIMS = bitSet('<', '>', '#', '%', '"');

    /**
     * BitSet for unwise.
     */
    static final FluentBitSet UNWISE = bitSet('{', '}', '|', '\\', '^', '[', ']', '`');

    /**
     * Disallowed rel_path before escaping.
     */
    static final FluentBitSet DISALLOWED_REL_PATH = bitSet().or(URIC).andNot(REL_PATH);

    /**
     * Disallowed opaque_part before escaping.
     */
    static final FluentBitSet DISALLOWED_OPAQUE_PART = bitSet().or(URIC).andNot(OPAQUE_PART);

    /**
     * Those characters that are allowed for the authority component.
     */
    static final FluentBitSet ALLOWED_AUTHORITY = bitSet().or(AUTHORITY).clear('%');

    // Characters allowed within and for each component

    /**
     * Those characters that are allowed for the opaque_part.
     */
    static final FluentBitSet ALLOWED_OPAQUE_PART = bitSet().or(OPAQUE_PART).clear('%');

    /**
     * Those characters that are allowed for the reg_name.
     */
    // allowed_reg_name.andNot(percent);
    static final FluentBitSet ALLOWED_REG_NAME = bitSet().or(REG_NAME).clear('%');

    /**
     * Those characters that are allowed for the userinfo component.
     */
    // allowed_userinfo.andNot(percent);
    static final FluentBitSet ALLOWED_USER_INFO = bitSet().or(USERINFO).clear('%');

    /**
     * Those characters that are allowed for within the userinfo component.
     */
    static final FluentBitSet ALLOWED_WITHIN_USERINFO = bitSet().or(WITHIN_USERRINFO).clear('%');

    /**
     * Those characters that are allowed for the IPv6reference component. The characters '[', ']' in IPv6reference should be
     * excluded.
     */
    // allowed_IPv6reference.andNot(unwise);
    static final FluentBitSet ALLOWED_IPV6REFERENCE = bitSet().or(IPV6REFERENCE).clear('[', ']');

    /**
     * Those characters that are allowed for the host component. The characters '[', ']' in IPv6reference should be
     * excluded.
     */
    static final FluentBitSet ALLOWED_HOST = bitSet().or(HOSTNAME, ALLOWED_IPV6REFERENCE);

    /**
     * Those characters that are allowed for the authority component.
     */
    static final FluentBitSet ALLOWED_WITHIN_AUTHORITY = bitSet().or(SERVER, REG_NAME).clear(';', ':', '@', '?', '/');

    /**
     * Those characters that are allowed for the abs_path.
     */
    // allowed_abs_path.set('/'); // already included
    static final FluentBitSet ALLOWED_ABS_PATH = bitSet().or(ABS_PATH).andNot(PERCENT).clear('+');

    /**
     * Those characters that are allowed for the rel_path.
     */
    static final FluentBitSet ALLOWED_REL_PATH = bitSet().or(REL_PATH).clear('%', '+');

    /**
     * Those characters that are allowed within the path.
     */
    static final FluentBitSet ALLOWED_WITHIN_PATH = bitSet().or(ABS_PATH).clear('/', ';', '=', '?');

    /**
     * Those characters that are allowed for the query component.
     */
    static final FluentBitSet ALLOWED_QUERY = bitSet().or(URIC).clear('%');

    /**
     * Those characters that are allowed within the query component.
     */
    // excluded 'reserved'
    static final FluentBitSet ALLOWED_WITHIN_QUERY = bitSet().or(ALLOWED_QUERY).andNot(RESERVED);

    /**
     * Those characters that are allowed for the fragment component.
     */
    static final FluentBitSet ALLOWED_FRAGMENT = bitSet().or(URIC).clear('%');

    /**
     * BitSet for control.
     */
    private static final int CHARACTER_DEL = 0x7F;
    private static final int CHARACTER_US = 0x1F;
    static final FluentBitSet CONTROL = bitSet().setInclusive(0, CHARACTER_US).set(CHARACTER_DEL);

    private static final int NBITS = 256;

    static FluentBitSet bitSet() {
        return new FluentBitSet(NBITS);
    }

    private static FluentBitSet bitSet(final int... bitIndexArray) {
        return bitSet().set(bitIndexArray);
    }

    private URIBitSets() {
    }

}
