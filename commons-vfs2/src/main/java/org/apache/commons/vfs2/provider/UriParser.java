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
package org.apache.commons.vfs2.provider;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.util.Os;

/**
 * Utilities for dealing with URIs. See RFC 2396 for details.
 *
 * 2005) $
 */
public final class UriParser {
    /**
     * The set of valid separators. These are all converted to the normalized one. Does <i>not</i> contain the
     * normalized separator
     */
    // public static final char[] separators = {'\\'};
    public static final char TRANS_SEPARATOR = '\\';

    /**
     * The normalised separator to use.
     */
    private static final char SEPARATOR_CHAR = FileName.SEPARATOR_CHAR;

    private static final int HEX_BASE = 16;

    private static final int BITS_IN_HALF_BYTE = 4;

    private static final char LOW_MASK = 0x0F;

    private UriParser() {
    }

    /**
     * Extracts the first element of a path.
     *
     * @param name StringBuilder containing the path.
     * @return The first element of the path.
     */
    public static String extractFirstElement(final StringBuilder name) {
        final int len = name.length();
        if (len < 1) {
            return null;
        }
        int startPos = 0;
        if (name.charAt(0) == SEPARATOR_CHAR) {
            startPos = 1;
        }
        for (int pos = startPos; pos < len; pos++) {
            if (name.charAt(pos) == SEPARATOR_CHAR) {
                // Found a separator
                final String elem = name.substring(startPos, pos);
                name.delete(startPos, pos + 1);
                return elem;
            }
        }

        // No separator
        final String elem = name.substring(startPos);
        name.setLength(0);
        return elem;
    }

    /**
     * Normalises a path. Does the following:
     * <ul>
     * <li>Removes empty path elements.
     * <li>Handles '.' and '..' elements.
     * <li>Removes trailing separator.
     * </ul>
     *
     * Its assumed that the separators are already fixed.
     *
     * @param path The path to normalize.
     * @return The FileType.
     * @throws FileSystemException if an error occurs.
     *
     * @see #fixSeparators
     */
    public static FileType normalisePath(final StringBuilder path) throws FileSystemException {
        FileType fileType = FileType.FOLDER;
        if (path.length() == 0) {
            return fileType;
        }

        if (path.charAt(path.length() - 1) != '/') {
            fileType = FileType.FILE;
        }

        // Adjust separators
        // fixSeparators(path);

        // Determine the start of the first element
        int startFirstElem = 0;
        if (path.charAt(0) == SEPARATOR_CHAR) {
            if (path.length() == 1) {
                return fileType;
            }
            startFirstElem = 1;
        }

        // Iterate over each element
        int startElem = startFirstElem;
        int maxlen = path.length();
        while (startElem < maxlen) {
            // Find the end of the element
            int endElem = startElem;
            for (; endElem < maxlen && path.charAt(endElem) != SEPARATOR_CHAR; endElem++) {
            }

            final int elemLen = endElem - startElem;
            if (elemLen == 0) {
                // An empty element - axe it
                path.delete(endElem, endElem + 1);
                maxlen = path.length();
                continue;
            }
            if (elemLen == 1 && path.charAt(startElem) == '.') {
                // A '.' element - axe it
                path.delete(startElem, endElem + 1);
                maxlen = path.length();
                continue;
            }
            if (elemLen == 2 && path.charAt(startElem) == '.' && path.charAt(startElem + 1) == '.') {
                // A '..' element - remove the previous element
                if (startElem == startFirstElem) {
                    // Previous element is missing
                    throw new FileSystemException("vfs.provider/invalid-relative-path.error");
                }

                // Find start of previous element
                int pos = startElem - 2;
                for (; pos >= 0 && path.charAt(pos) != SEPARATOR_CHAR; pos--) {
                }
                startElem = pos + 1;

                path.delete(startElem, endElem + 1);
                maxlen = path.length();
                continue;
            }

            // A regular element
            startElem = endElem + 1;
        }

        // Remove trailing separator
        if (!VFS.isUriStyle() && maxlen > 1 && path.charAt(maxlen - 1) == SEPARATOR_CHAR) {
            path.delete(maxlen - 1, maxlen);
        }

        return fileType;
    }

    /**
     * Normalises the separators in a name.
     *
     * @param name The StringBuilder containing the name
     * @return true if the StringBuilder was modified.
     */
    public static boolean fixSeparators(final StringBuilder name) {
        boolean changed = false;
        final int maxlen = name.length();
        for (int i = 0; i < maxlen; i++) {
            final char ch = name.charAt(i);
            if (ch == TRANS_SEPARATOR) {
                name.setCharAt(i, SEPARATOR_CHAR);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Extracts the scheme from a URI.
     *
     * @param uri The URI.
     * @return The scheme name. Returns null if there is no scheme.
     */
    public static String extractScheme(final String uri) {
        return extractScheme(uri, null);
    }

    /**
     * Extracts the scheme from a URI. Removes the scheme and ':' delimiter from the front of the URI.
     *
     * @param uri The URI.
     * @param buffer Returns the remainder of the URI.
     * @return The scheme name. Returns null if there is no scheme.
     */
    public static String extractScheme(final String uri, final StringBuilder buffer) {
        if (buffer != null) {
            buffer.setLength(0);
            buffer.append(uri);
        }

        final int maxPos = uri.length();
        for (int pos = 0; pos < maxPos; pos++) {
            final char ch = uri.charAt(pos);

            if (ch == ':') {
                // Found the end of the scheme
                final String scheme = uri.substring(0, pos);
                if (scheme.length() <= 1 && Os.isFamily(Os.OS_FAMILY_WINDOWS)) {
                    // This is not a scheme, but a Windows drive letter
                    return null;
                }
                if (buffer != null) {
                    buffer.delete(0, pos + 1);
                }
                return scheme.intern();
            }

            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                // A scheme character
                continue;
            }
            if (pos > 0 && ((ch >= '0' && ch <= '9') || ch == '+' || ch == '-' || ch == '.')) {
                // A scheme character (these are not allowed as the first
                // character of the scheme, but can be used as subsequent
                // characters.
                continue;
            }

            // Not a scheme character
            break;
        }

        // No scheme in URI
        return null;
    }

    /**
     * Removes %nn encodings from a string.
     *
     * @param encodedStr The encoded String.
     * @return The decoded String.
     * @throws FileSystemException if an error occurs.
     */
    public static String decode(final String encodedStr) throws FileSystemException {
        if (encodedStr == null) {
            return null;
        }
        if (encodedStr.indexOf('%') < 0) {
            return encodedStr;
        }
        final StringBuilder buffer = new StringBuilder(encodedStr);
        decode(buffer, 0, buffer.length());
        return buffer.toString();
    }

    /**
     * Removes %nn encodings from a string.
     *
     * @param buffer StringBuilder containing the string to decode.
     * @param offset The position in the string to start decoding.
     * @param length The number of characters to decode.
     * @throws FileSystemException if an error occurs.
     */
    public static void decode(final StringBuilder buffer, final int offset, final int length)
            throws FileSystemException {
        int index = offset;
        int count = length;
        for (; count > 0; count--, index++) {
            final char ch = buffer.charAt(index);
            if (ch != '%') {
                continue;
            }
            if (count < 3) {
                throw new FileSystemException("vfs.provider/invalid-escape-sequence.error",
                        buffer.substring(index, index + count));
            }

            // Decode
            final int dig1 = Character.digit(buffer.charAt(index + 1), HEX_BASE);
            final int dig2 = Character.digit(buffer.charAt(index + 2), HEX_BASE);
            if (dig1 == -1 || dig2 == -1) {
                throw new FileSystemException("vfs.provider/invalid-escape-sequence.error",
                        buffer.substring(index, index + 3));
            }
            final char value = (char) (dig1 << BITS_IN_HALF_BYTE | dig2);

            // Replace
            buffer.setCharAt(index, value);
            buffer.delete(index + 1, index + 3);
            count -= 2;
        }
    }

    /**
     * Encodes and appends a string to a StringBuilder.
     *
     * @param buffer The StringBuilder to append to.
     * @param unencodedValue The String to encode and append.
     * @param reserved characters to encode.
     */
    public static void appendEncoded(final StringBuilder buffer, final String unencodedValue, final char[] reserved) {
        final int offset = buffer.length();
        buffer.append(unencodedValue);
        encode(buffer, offset, unencodedValue.length(), reserved);
    }

    /**
     * Encodes a set of reserved characters in a StringBuilder, using the URI %nn encoding. Always encodes % characters.
     *
     * @param buffer The StringBuilder to append to.
     * @param offset The position in the buffer to start encoding at.
     * @param length The number of characters to encode.
     * @param reserved characters to encode.
     */
    public static void encode(final StringBuilder buffer, final int offset, final int length, final char[] reserved) {
        int index = offset;
        int count = length;
        for (; count > 0; index++, count--) {
            final char ch = buffer.charAt(index);
            boolean match = ch == '%';
            if (reserved != null) {
                for (int i = 0; !match && i < reserved.length; i++) {
                    if (ch == reserved[i]) {
                        match = true;
                    }
                }
            }
            if (match) {
                // Encode
                final char[] digits = { Character.forDigit((ch >> BITS_IN_HALF_BYTE) & LOW_MASK, HEX_BASE),
                        Character.forDigit(ch & LOW_MASK, HEX_BASE) };
                buffer.setCharAt(index, '%');
                buffer.insert(index + 1, digits);
                index += 2;
            }
        }
    }

    /**
     * Removes %nn encodings from a string.
     *
     * @param decodedStr The decoded String.
     * @return The encoded String.
     */
    public static String encode(final String decodedStr) {
        return encode(decodedStr, null);
    }

    /**
     * Converts "special" characters to their %nn value.
     *
     * @param decodedStr The decoded String.
     * @param reserved Characters to encode.
     * @return The encoded String
     */
    public static String encode(final String decodedStr, final char[] reserved) {
        if (decodedStr == null) {
            return null;
        }
        final StringBuilder buffer = new StringBuilder(decodedStr);
        encode(buffer, 0, buffer.length(), reserved);
        return buffer.toString();
    }

    /**
     * Encode an array of Strings.
     *
     * @param strings The array of Strings to encode.
     * @return An array of encoded Strings.
     */
    public static String[] encode(final String[] strings) {
        if (strings == null) {
            return null;
        }
        for (int i = 0; i < strings.length; i++) {
            strings[i] = encode(strings[i]);
        }
        return strings;
    }

    /**
     * Decodes the String.
     *
     * @param uri The String to decode.
     * @throws FileSystemException if an error occurs.
     */
    public static void checkUriEncoding(final String uri) throws FileSystemException {
        decode(uri);
    }

    public static void canonicalizePath(final StringBuilder buffer, final int offset, final int length,
            final FileNameParser fileNameParser) throws FileSystemException {
        int index = offset;
        int count = length;
        for (; count > 0; count--, index++) {
            final char ch = buffer.charAt(index);
            if (ch == '%') {
                if (count < 3) {
                    throw new FileSystemException("vfs.provider/invalid-escape-sequence.error",
                            buffer.substring(index, index + count));
                }

                // Decode
                final int dig1 = Character.digit(buffer.charAt(index + 1), HEX_BASE);
                final int dig2 = Character.digit(buffer.charAt(index + 2), HEX_BASE);
                if (dig1 == -1 || dig2 == -1) {
                    throw new FileSystemException("vfs.provider/invalid-escape-sequence.error",
                            buffer.substring(index, index + 3));
                }
                final char value = (char) (dig1 << BITS_IN_HALF_BYTE | dig2);

                final boolean match = value == '%' || fileNameParser.encodeCharacter(value);

                if (match) {
                    // this is a reserved character, not allowed to decode
                    index += 2;
                    count -= 2;
                    continue;
                }

                // Replace
                buffer.setCharAt(index, value);
                buffer.delete(index + 1, index + 3);
                count -= 2;
            } else if (fileNameParser.encodeCharacter(ch)) {
                // Encode
                final char[] digits = { Character.forDigit((ch >> BITS_IN_HALF_BYTE) & LOW_MASK, HEX_BASE),
                        Character.forDigit(ch & LOW_MASK, HEX_BASE) };
                buffer.setCharAt(index, '%');
                buffer.insert(index + 1, digits);
                index += 2;
            }
        }
    }

    /**
     * Extract the query String from the URI.
     *
     * @param name StringBuilder containing the URI.
     * @return The query string, if any. null otherwise.
     */
    public static String extractQueryString(final StringBuilder name) {
        for (int pos = 0; pos < name.length(); pos++) {
            if (name.charAt(pos) == '?') {
                final String queryString = name.substring(pos + 1);
                name.delete(pos, name.length());
                return queryString;
            }
        }

        return null;
    }
}
