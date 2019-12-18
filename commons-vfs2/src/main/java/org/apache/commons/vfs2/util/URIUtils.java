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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.BitSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.provider.GenericURLFileName;

/**
 * The URI escape and character encoding and decoding utility.
 * <p>
 * This was forked from some needed methods such as {@code #encodePath(...)} in {@code org.apache.commons.httpclient.util.URIUtil},
 * in order to not be dependent on HttpClient v3 API, when generating and handling {@link GenericURLFileName}s,
 * but it should work with any different HTTP backend provider implementations.
 * </p>
 */
public class URIUtils {

    private static final Log LOG = LogFactory.getLog(URIUtils.class);

    /**
     * The default charset of the protocol.  RFC 2277, 2396
     */
    private static final String DEFAULT_PROTOCOL_CHARSET = "UTF-8";

    private URIUtils() {
    }

    /**
     * Escape and encode a string regarded as the path component of an URI with
     * the default protocol charset.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     *
     * @throws URISyntaxException if the default protocol charset is not supported
     */
    public static String encodePath(final String unescaped) throws URISyntaxException {
        return encodePath(unescaped, DEFAULT_PROTOCOL_CHARSET);
    }

    /**
     * Escape and encode a string regarded as the path component of an URI with
     * a given charset.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     *
     * @throws URISyntaxException if the charset is not supported
     */
    public static String encodePath(final String unescaped, final String charset) throws URISyntaxException {
        if (unescaped == null) {
            throw new IllegalArgumentException("The string to encode may not be null.");
        }

        return encode(unescaped, URIBitSets.allowed_abs_path, charset);
    }

    private static String encode(final String unescaped, final BitSet allowed, final String charset) throws URISyntaxException {
        final byte[] rawdata = URLCodecUtils.encodeUrl(allowed, EncodingUtils.getBytes(unescaped, charset));
        return EncodingUtils.getAsciiString(rawdata, 0, rawdata.length);
    }

    /**
     * Internal URL codec utilities.
     * <p>
     * This was forked from some needed methods such as {@code #encodeUrl(...)} and {@code #hexDigit(int)}
     * in {@code org.apache.commons.codec.net.URLCodec}, as commons-codec library cannot be pulled in transitively
     * via Http Client v3 library any more.
     * </p>
     */
    private static class URLCodecUtils {

        private static final byte ESCAPE_CHAR = '%';

        private static final BitSet WWW_FORM_URL_SAFE = new BitSet(256);

        // Static initializer for www_form_url
        static {
            // alpha characters
            for (int i = 'a'; i <= 'z'; i++) {
                WWW_FORM_URL_SAFE.set(i);
            }
            for (int i = 'A'; i <= 'Z'; i++) {
                WWW_FORM_URL_SAFE.set(i);
            }
            // numeric characters
            for (int i = '0'; i <= '9'; i++) {
                WWW_FORM_URL_SAFE.set(i);
            }
            // special chars
            WWW_FORM_URL_SAFE.set('-');
            WWW_FORM_URL_SAFE.set('_');
            WWW_FORM_URL_SAFE.set('.');
            WWW_FORM_URL_SAFE.set('*');
            // blank to be replaced with +
            WWW_FORM_URL_SAFE.set(' ');
        }

        /**
         * Radix used in encoding and decoding.
         */
        private static final int RADIX = 16;

        private URLCodecUtils() {
        }

        static final byte[] encodeUrl(BitSet urlsafe, final byte[] bytes) {
            if (bytes == null) {
                return null;
            }
            if (urlsafe == null) {
                urlsafe = WWW_FORM_URL_SAFE;
            }

            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            for (final byte c : bytes) {
                int b = c;
                if (b < 0) {
                    b = 256 + b;
                }
                if (urlsafe.get(b)) {
                    if (b == ' ') {
                        b = '+';
                    }
                    buffer.write(b);
                } else {
                    buffer.write(ESCAPE_CHAR);
                    final char hex1 = hexDigit(b >> 4);
                    final char hex2 = hexDigit(b);
                    buffer.write(hex1);
                    buffer.write(hex2);
                }
            }
            return buffer.toByteArray();
        }

        private static char hexDigit(final int b) {
            return Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
        }
    }

    /**
     * Internal character encoding utilities.
     * <p>
     * This was forked from some needed methods such as {@code #getBytes(...)} and {@code #getAsciiString(...)}
     * in {@code org.apache.commons.httpclient.util.EncodingUtil},
     * in order to not be dependent on HttpClient v3 API, when generating and handling {@link GenericURLFileName}s,
     * but it should work with any different HTTP backend provider implementations.
     * </p>
     */
    private static class EncodingUtils {

        private EncodingUtils() {
        }

        /**
         * Converts the specified string to a byte array.  If the charset is not supported the
         * default system charset is used.
         *
         * @param data the string to be encoded
         * @param charset the desired character encoding
         * @return The resulting byte array.
         */
        static byte[] getBytes(final String data, final String charset) {
            if (data == null) {
                throw new IllegalArgumentException("data may not be null");
            }

            if (charset == null || charset.length() == 0) {
                throw new IllegalArgumentException("charset may not be null or empty");
            }

            try {
                return data.getBytes(charset);
            } catch (final UnsupportedEncodingException e) {

                if (LOG.isWarnEnabled()) {
                    LOG.warn("Unsupported encoding: " + charset + ". System encoding used.");
                }

                return data.getBytes();
            }
        }

        /**
         * Converts the byte array of ASCII characters to a string. This method is
         * to be used when decoding content of HTTP elements (such as response
         * headers)
         *
         * @param data the byte array to be encoded
         * @param offset the index of the first byte to encode
         * @param length the number of bytes to encode
         * @return The string representation of the byte array
         */
        static String getAsciiString(final byte[] data, final int offset, final int length) {
            try {
                return new String(data, offset, length, "US-ASCII");
            } catch (final UnsupportedEncodingException e) {
                throw new RuntimeException("US-ASCII charset is not supported.");
            }
        }
    }

}
