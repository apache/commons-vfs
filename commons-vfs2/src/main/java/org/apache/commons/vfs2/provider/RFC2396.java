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

package org.apache.commons.vfs2.provider;

import org.apache.commons.lang3.ArraySorter;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Helps deal with <a href="https://datatracker.ietf.org/doc/html/rfc2396">RFC 2396</a>.
 * <p>
 * The RFC 2396 Collected BNF for URI from <a href="https://datatracker.ietf.org/doc/html/rfc2396#appendix-A">Appendix
 * A</a>:
 * </p>
 *
 * <pre>
      URI-reference = [ absoluteURI | relativeURI ] [ "#" fragment ]
      absoluteURI   = scheme ":" ( hier_part | opaque_part )
      relativeURI   = ( net_path | abs_path | rel_path ) [ "?" query ]

      hier_part     = ( net_path | abs_path ) [ "?" query ]
      opaque_part   = uric_no_slash *uric

      uric_no_slash = unreserved | escaped | ";" | "?" | ":" | "@" |
                      "&" | "=" | "+" | "$" | ","

      net_path      = "//" authority [ abs_path ]
      abs_path      = "/"  path_segments
      rel_path      = rel_segment [ abs_path ]

      rel_segment   = 1*( unreserved | escaped |
                          ";" | "@" | "&" | "=" | "+" | "$" | "," )

      scheme        = alpha *( alpha | digit | "+" | "-" | "." )

      authority     = server | reg_name

      reg_name      = 1*( unreserved | escaped | "$" | "," |
                          ";" | ":" | "@" | "&" | "=" | "+" )

      server        = [ [ userinfo "@" ] hostport ]
      userinfo      = *( unreserved | escaped |
                         ";" | ":" | "&" | "=" | "+" | "$" | "," )

      hostport      = host [ ":" port ]
      host          = hostname | IPv4address
      hostname      = *( domainlabel "." ) toplabel [ "." ]
      domainlabel   = alphanum | alphanum *( alphanum | "-" ) alphanum
      toplabel      = alpha | alpha *( alphanum | "-" ) alphanum
      IPv4address   = 1*digit "." 1*digit "." 1*digit "." 1*digit
      port          = *digit

      path          = [ abs_path | opaque_part ]
      path_segments = segment *( "/" segment )
      segment       = *pchar *( ";" param )
      param         = *pchar
      pchar         = unreserved | escaped |
                      ":" | "@" | "&" | "=" | "+" | "$" | ","

      query         = *uric

      fragment      = *uric

      uric          = reserved | unreserved | escaped
      reserved      = ";" | "/" | "?" | ":" | "@" | "&" | "=" | "+" |
                      "$" | ","
      unreserved    = alphanum | mark
      mark          = "-" | "_" | "." | "!" | "~" | "*" | "'" |
                      "(" | ")"

      escaped       = "%" hex hex
      hex           = digit | "A" | "B" | "C" | "D" | "E" | "F" |
                              "a" | "b" | "c" | "d" | "e" | "f"

      alphanum      = alpha | digit
      alpha         = lowalpha | upalpha

      lowalpha = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" |
                 "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" |
                 "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
      upalpha  = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" |
                 "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" |
                 "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
      digit    = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" |
                 "8" | "9"
 * </pre>
 */
final class RFC2396 {

    // RFC 2396 mark
    static final char[] MARK = {'-', '_', '.', '!', '~', '*', '\'', '(', ')'};

    // RFC 2396 digit
    static final char[] DIGIT = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    // RFC 2396 lowalpha
    static final char[] LOWALPHA = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
        'z'};

    // RFC 2396 upalpha
    static final char[] UPALPHA = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
        'Z'};

    // RFC 2396 alpha
    static final char[] ALPHA = ArrayUtils.addAll(LOWALPHA, UPALPHA);

    // RFC 2396 alphanum
    static final char[] ALPHANUM = ArrayUtils.addAll(ALPHA, DIGIT);

    // RFC 2396 reserved
    static final char[] RESERVED = {';', '/', '?', ':', '@', '&', '=', '+', '$', ','};

    // RFC 2396 unreserved
    static final char[] UNRESERVED = ArrayUtils.addAll(ALPHANUM, MARK);

    // RFC 2396 userinfo chars which are unescaped, here sorted.
    static final char[] USERINFO_UNESCAPED = ArraySorter.sort(ArrayUtils.addAll(UNRESERVED, ';', ':', '&', '=', '+', '$', ','));

    private RFC2396() {
    }
}
